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
    private const val MAX_GUARDADOS = 100

    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos

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
        _pagos.value = actualizados
        val arreglo = JSONArray()
        actualizados.forEach { p ->
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
