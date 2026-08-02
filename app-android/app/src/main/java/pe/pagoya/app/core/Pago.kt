package pe.pagoya.app.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class Pago(
    val billeteraId: String,
    val billeteraNombre: String,
    val pagador: String,
    val monto: Double,
    val timestamp: Long,
)

/**
 * Registro local de pagos capturados. Fuente única para la UI.
 * Cuando entre Firebase, este registro además subirá cada pago al backend.
 */
object RegistroPagos {

    private const val PREFS = "pagoya_registro"
    private const val CLAVE = "pagos"
    // Tope ACOTADO: no es historial infinito (eso vive en Firestore y en el
    // panel web), es la ventana reciente que alimenta Inicio y Caja en este
    // teléfono. Se subió de 100 a 300 para que la rehidratación desde la nube
    // (ver ComercioRepo.rehidratarPagos, que trae hasta 300 docs) no se trunque
    // absurdamente al fusionar con lo local.
    private const val MAX_GUARDADOS = 300

    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos

    /**
     * Huella estable de un pago, para no duplicarlo entre las tres fuentes que
     * lo pueden traer: captura local, push FCM (modo escucha) y rehidratación
     * desde Firestore. Son los mismos campos que componen el id determinista de
     * la nube (`origenUid-timestamp-centavos`) más lo identificatorio, así que
     * el mismo pago cae en la misma huella venga de donde venga. No dependemos
     * de un `id` en `Pago` (rompería el JSON ya guardado y el contrato del push).
     */
    private fun clave(p: Pago): String =
        "${p.timestamp}-${Math.round(p.monto * 100)}-${p.billeteraId}-${p.pagador}"

    fun cargar(context: Context) {
        val crudo = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CLAVE, null) ?: return
        runCatching {
            val arreglo = JSONArray(crudo)
            _pagos.value = (0 until arreglo.length()).map { i ->
                val o = arreglo.getJSONObject(i)
                Pago(
                    billeteraId = o.getString("billeteraId"),
                    billeteraNombre = o.getString("billeteraNombre"),
                    pagador = o.getString("pagador"),
                    monto = o.getDouble("monto"),
                    timestamp = o.getLong("timestamp"),
                )
            }
        }
    }

    fun agregar(context: Context, pago: Pago) {
        val actualizados = (listOf(pago) + _pagos.value).take(MAX_GUARDADOS)
        persistir(context, actualizados)
    }

    /**
     * Carga masiva silenciosa (rehidratación desde Firestore). Fusiona una tanda
     * de pagos con lo que ya hay SIN duplicar (por huella) y escribe el JSON UNA
     * sola vez — no una vez por pago. NO anuncia ni vibra: solo llena el registro
     * para que la Caja y el Inicio vuelvan a poblarse tras reinstalar o cambiar
     * de teléfono. Ordena por timestamp desc y respeta el tope.
     */
    fun fusionar(context: Context, entrantes: List<Pago>) {
        if (entrantes.isEmpty()) return
        val vistos = HashSet<String>()
        val fusionados = (_pagos.value + entrantes)
            .filter { vistos.add(clave(it)) }
            .sortedByDescending { it.timestamp }
            .take(MAX_GUARDADOS)
        persistir(context, fusionados)
    }

    /** Fija el estado y escribe el JSON una vez. */
    private fun persistir(context: Context, pagos: List<Pago>) {
        _pagos.value = pagos
        val arreglo = JSONArray()
        pagos.forEach { p ->
            arreglo.put(
                JSONObject()
                    .put("billeteraId", p.billeteraId)
                    .put("billeteraNombre", p.billeteraNombre)
                    .put("pagador", p.pagador)
                    .put("monto", p.monto)
                    .put("timestamp", p.timestamp)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(CLAVE, arreglo.toString()).apply()
    }

    fun totalDeHoy(): Double {
        val inicioDia = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return _pagos.value.filter { it.timestamp >= inicioDia }.sumOf { it.monto }
    }
}
