package pe.pagoya.app.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Qué billeteras quiere ESCUCHAR el comerciante en ESTE teléfono. Local por
 * equipo (como la voz y la apariencia): un mismo comercio puede tener un
 * teléfono que solo cobra por Yape y otro que además usa Plin.
 *
 * Modelo OPT-OUT a propósito: guardamos el set de ids DESHABILITADAS, no el de
 * habilitadas. Así, cuando Remote Config sume una billetera nueva (o llegue un
 * comercio recién instalado), queda ACTIVA sin tocar nada — la regla es
 * "escuchamos todas salvo las que apagaste". Set vacío por defecto ⇒ todas
 * activas.
 *
 * Se expone como StateFlow para que la pantalla de config reaccione al instante
 * cuando el comerciante prende/apaga una billetera, sin reabrir la app.
 */
object PreferenciasBilleteras {

    // Reusa el mismo archivo de prefs que la voz y la apariencia.
    private const val PREFS = "pagoya_config"
    private const val CLAVE_DESHABILITADAS = "billeteras_deshabilitadas"

    private val _deshabilitadas = MutableStateFlow<Set<String>>(emptySet())
    /** Ids de billeteras que este teléfono NO debe escuchar. Vacío = todas activas. */
    val deshabilitadas: StateFlow<Set<String>> = _deshabilitadas

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Sincroniza el flujo con lo guardado. Llamar al arrancar la app. */
    fun cargar(context: Context) {
        _deshabilitadas.value =
            prefs(context).getStringSet(CLAVE_DESHABILITADAS, emptySet())
                ?.toSet() ?: emptySet()
    }

    /**
     * ¿Se escucha esta billetera en este teléfono? true salvo que el comerciante
     * la haya apagado. Una billetera desconocida (o nueva) siempre da true: el
     * gate nunca silencia por defecto.
     */
    fun estaActiva(billeteraId: String): Boolean =
        billeteraId !in _deshabilitadas.value

    fun activar(context: Context, billeteraId: String) {
        definir(context, _deshabilitadas.value - billeteraId)
    }

    fun desactivar(context: Context, billeteraId: String) {
        definir(context, _deshabilitadas.value + billeteraId)
    }

    fun alternar(context: Context, billeteraId: String) {
        if (estaActiva(billeteraId)) desactivar(context, billeteraId)
        else activar(context, billeteraId)
    }

    private fun definir(context: Context, nuevo: Set<String>) {
        // Copia defensiva: SharedPreferences no debe quedarse con el mismo Set
        // mutable que luego leemos de vuelta.
        prefs(context).edit()
            .putStringSet(CLAVE_DESHABILITADAS, HashSet(nuevo))
            .apply()
        _deshabilitadas.value = nuevo
    }
}
