package pe.pagoya.app.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Una notificación recibida (campaña, promo, aviso de plan). */
data class Notificacion(
    val id: String,
    val titulo: String,
    val cuerpo: String,
    val timestamp: Long,
    val leida: Boolean,
)

/**
 * Buzón de avisos de PagoYa. Guarda las notificaciones push que NO son pagos
 * (campañas, promociones, avisos de plan) para que el comerciante las repase
 * cuando quiera desde la campanita, aunque haya barrido la notificación del
 * sistema. Local por teléfono, como el registro de pagos.
 *
 * Reactivo (StateFlow) para que la campanita y su contador de no leídas se
 * actualicen al instante cuando llega un push o cuando se marcan como leídas.
 */
object BandejaNotificaciones {

    private const val PREFS = "pagoya_bandeja"
    private const val CLAVE = "avisos"
    private const val MAX_GUARDADOS = 50

    private val _avisos = MutableStateFlow<List<Notificacion>>(emptyList())
    val avisos: StateFlow<List<Notificacion>> = _avisos

    /** Cuántas sin leer — para el puntito rojo de la campanita. */
    private val _noLeidas = MutableStateFlow(0)
    val noLeidas: StateFlow<Int> = _noLeidas

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun cargar(context: Context) {
        val crudo = prefs(context).getString(CLAVE, null) ?: return
        runCatching {
            val arreglo = JSONArray(crudo)
            _avisos.value = (0 until arreglo.length()).map { i ->
                val o = arreglo.getJSONObject(i)
                Notificacion(
                    id = o.getString("id"),
                    titulo = o.getString("titulo"),
                    cuerpo = o.getString("cuerpo"),
                    timestamp = o.getLong("timestamp"),
                    leida = o.optBoolean("leida", false),
                )
            }
        }
        refrescarContador()
    }

    /** Suma un aviso nuevo (llega sin leer). Lo llama MensajesPagoYa. */
    fun agregar(context: Context, titulo: String, cuerpo: String, timestamp: Long) {
        val nuevo = Notificacion(
            id = "$timestamp-${titulo.hashCode()}",
            titulo = titulo,
            cuerpo = cuerpo,
            timestamp = timestamp,
            leida = false,
        )
        // Dedup por id (mismo push reprocesado no se duplica).
        val sinRepetir = _avisos.value.filterNot { it.id == nuevo.id }
        persistir(context, (listOf(nuevo) + sinRepetir).take(MAX_GUARDADOS))
    }

    /** Al abrir el buzón: todo queda leído (apaga el puntito). */
    fun marcarTodasLeidas(context: Context) {
        if (_avisos.value.none { !it.leida }) return
        persistir(context, _avisos.value.map { it.copy(leida = true) })
    }

    fun limpiar(context: Context) {
        persistir(context, emptyList())
    }

    private fun persistir(context: Context, lista: List<Notificacion>) {
        _avisos.value = lista
        val arreglo = JSONArray()
        lista.forEach { n ->
            arreglo.put(
                JSONObject()
                    .put("id", n.id)
                    .put("titulo", n.titulo)
                    .put("cuerpo", n.cuerpo)
                    .put("timestamp", n.timestamp)
                    .put("leida", n.leida)
            )
        }
        prefs(context).edit().putString(CLAVE, arreglo.toString()).apply()
        refrescarContador()
    }

    private fun refrescarContador() {
        _noLeidas.value = _avisos.value.count { !it.leida }
    }
}
