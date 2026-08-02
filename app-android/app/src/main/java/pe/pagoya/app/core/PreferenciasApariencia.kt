package pe.pagoya.app.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Ajustes de apariencia de la app. Local por teléfono (como la voz): no es una
 * decisión de cuenta sino de gusto de quien mira esta pantalla.
 *
 * Se expone como StateFlow para que la lista de pagos (BilleteraBadge) reaccione
 * al instante cuando el comerciante prende/apaga el ícono, sin reabrir la app.
 */
object PreferenciasApariencia {

    // Reusa el mismo archivo de prefs que la voz.
    private const val PREFS = "pagoya_config"
    private const val CLAVE_ICONO_BILLETERA = "apariencia_icono_billetera"

    private val _mostrarIconoBilletera = MutableStateFlow(true)
    /** ¿Mostrar el logo de la billetera (Yape/Plin) en la lista de pagos? */
    val mostrarIconoBilletera: StateFlow<Boolean> = _mostrarIconoBilletera

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Sincroniza el flujo con lo guardado. Llamar al arrancar la app. */
    fun cargar(context: Context) {
        _mostrarIconoBilletera.value =
            prefs(context).getBoolean(CLAVE_ICONO_BILLETERA, true)
    }

    fun definirMostrarIconoBilletera(context: Context, mostrar: Boolean) {
        prefs(context).edit().putBoolean(CLAVE_ICONO_BILLETERA, mostrar).apply()
        _mostrarIconoBilletera.value = mostrar
    }
}
