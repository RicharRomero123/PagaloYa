package pe.pagoya.app.core.voz

import android.content.Context

/**
 * Cómo quiere el comerciante que suene su caja. Todo se guarda local: la voz es
 * una decisión de ambiente (mercado ruidoso vs. tienda tranquila) y cambia por
 * teléfono, no por cuenta.
 */
object PreferenciasVoz {

    private const val PREFS = "pagoya_config"
    private const val CLAVE_VOZ = "voz_id"
    private const val CLAVE_VELOCIDAD = "voz_velocidad"
    private const val CLAVE_TONO = "voz_tono"
    private const val CLAVE_FUERTE = "voz_fuerte"
    private const val CLAVE_DECIR_NOMBRE = "voz_decir_nombre"

    /** Un pelín lenta: en un mercado, rápido = no se entiende. */
    const val VELOCIDAD_DEFECTO = 0.95f
    const val TONO_DEFECTO = 1.0f

    const val VELOCIDAD_MIN = 0.7f
    const val VELOCIDAD_MAX = 1.3f
    const val TONO_MIN = 0.8f
    const val TONO_MAX = 1.25f

    private fun prefs(contexto: Context) =
        contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** `Voice.name` de la voz elegida. null = que PagoYa elija la mejor. */
    fun vozElegida(contexto: Context): String? =
        prefs(contexto).getString(CLAVE_VOZ, null)

    fun elegirVoz(contexto: Context, id: String?) {
        prefs(contexto).edit().apply {
            if (id == null) remove(CLAVE_VOZ) else putString(CLAVE_VOZ, id)
        }.apply()
    }

    fun velocidad(contexto: Context): Float =
        prefs(contexto).getFloat(CLAVE_VELOCIDAD, VELOCIDAD_DEFECTO)

    fun definirVelocidad(contexto: Context, valor: Float) {
        prefs(contexto).edit()
            .putFloat(CLAVE_VELOCIDAD, valor.coerceIn(VELOCIDAD_MIN, VELOCIDAD_MAX))
            .apply()
    }

    fun tono(contexto: Context): Float =
        prefs(contexto).getFloat(CLAVE_TONO, TONO_DEFECTO)

    fun definirTono(contexto: Context, valor: Float) {
        prefs(contexto).edit()
            .putFloat(CLAVE_TONO, valor.coerceIn(TONO_MIN, TONO_MAX))
            .apply()
    }

    fun vozFuerte(contexto: Context): Boolean =
        prefs(contexto).getBoolean(CLAVE_FUERTE, true)

    fun definirVozFuerte(contexto: Context, activa: Boolean) {
        prefs(contexto).edit().putBoolean(CLAVE_FUERTE, activa).apply()
    }

    /**
     * ¿Decir en voz alta el nombre de quien pagó?
     *
     * **Apagado por defecto, a propósito.** Gritar el nombre de un cliente en
     * un mercado es exponer un dato personal delante de desconocidos (Ley 29733,
     * y la regla 5 de CLAUDE.md). El monto se canta, el nombre se lee en
     * pantalla. Queda como decisión consciente del comerciante, no del código.
     */
    fun decirNombre(contexto: Context): Boolean =
        prefs(contexto).getBoolean(CLAVE_DECIR_NOMBRE, false)

    fun definirDecirNombre(contexto: Context, activo: Boolean) {
        prefs(contexto).edit().putBoolean(CLAVE_DECIR_NOMBRE, activo).apply()
    }
}
