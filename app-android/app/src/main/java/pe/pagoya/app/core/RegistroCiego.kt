package pe.pagoya.app.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bitácora local de periodos CIEGOS: cada vez que el comercio dejó de escuchar
 * (inicio) y cuándo volvió (fin). Es la base de la futura "Garantía de Aviso" —
 * poder demostrar, con fechas, que PagoYa avisó cuando la caja quedó muda.
 *
 * Persistente y simple (SharedPreferences + JSON), mismo patrón que
 * RegistroPagos. Vive en este teléfono; el respaldo "de verdad" (auditable)
 * será en Firestore más adelante, pero el registro local ya sirve para la UI y
 * para no perder el dato si la app se cierra.
 */
data class PeriodoCiego(
    val inicio: Long,
    /** 0 mientras el periodo sigue abierto (aún ciego). */
    val fin: Long = 0L,
) {
    val abierto: Boolean get() = fin == 0L
    val duracionMs: Long get() = (if (abierto) System.currentTimeMillis() else fin) - inicio
}

object RegistroCiego {

    private const val PREFS = "pagoya_ciego"
    private const val CLAVE = "periodos"
    /** Ventana acotada: los últimos periodos, no historial infinito. */
    private const val MAX_GUARDADOS = 100

    private val _periodos = MutableStateFlow<List<PeriodoCiego>>(emptyList())
    val periodos: StateFlow<List<PeriodoCiego>> = _periodos

    fun cargar(context: Context) {
        _periodos.value = leerDesdePrefs(context)
    }

    private fun leerDesdePrefs(context: Context): List<PeriodoCiego> {
        val crudo = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CLAVE, null) ?: return emptyList()
        return runCatching {
            val arreglo = JSONArray(crudo)
            (0 until arreglo.length()).map { i ->
                val o = arreglo.getJSONObject(i)
                PeriodoCiego(inicio = o.getLong("inicio"), fin = o.optLong("fin", 0L))
            }.sortedByDescending { it.inicio }
        }.getOrDefault(emptyList())
    }

    /** ¿Hay un periodo ciego abierto ahora mismo? */
    fun hayPeriodoAbierto(): Boolean = _periodos.value.any { it.abierto }

    /**
     * Abre un periodo ciego (al ENTRAR en ceguera). Idempotente: si ya hay uno
     * abierto no crea otro, para no duplicar por dos detecciones (local + FCM).
     */
    fun abrir(context: Context, inicio: Long = System.currentTimeMillis()) {
        if (hayPeriodoAbierto()) return
        val actualizados = (listOf(PeriodoCiego(inicio = inicio)) + _periodos.value)
            .take(MAX_GUARDADOS)
        persistir(context, actualizados)
    }

    /**
     * Cierra el periodo abierto (al RECUPERAR). Si no había ninguno abierto, no
     * hace nada. Devuelve el periodo cerrado (para poder anunciar cuánto duró).
     */
    fun cerrar(context: Context, fin: Long = System.currentTimeMillis()): PeriodoCiego? {
        val abierto = _periodos.value.firstOrNull { it.abierto } ?: return null
        val cerrado = abierto.copy(fin = fin.coerceAtLeast(abierto.inicio))
        val actualizados = _periodos.value.map { if (it.abierto) cerrado else it }
        persistir(context, actualizados)
        return cerrado
    }

    private fun persistir(context: Context, periodos: List<PeriodoCiego>) {
        _periodos.value = periodos
        val arreglo = JSONArray()
        periodos.forEach { p ->
            arreglo.put(JSONObject().put("inicio", p.inicio).put("fin", p.fin))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(CLAVE, arreglo.toString()).apply()
    }
}
