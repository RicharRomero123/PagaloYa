package pe.pagoya.app.core.voz

import android.content.Context
import java.util.Calendar

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
    private const val CLAVE_SILENCIADO = "voz_silenciado"
    private const val CLAVE_HORARIO_ACTIVO = "voz_horario_activo"
    private const val CLAVE_HORARIO_INICIO = "voz_horario_inicio"
    private const val CLAVE_HORARIO_FIN = "voz_horario_fin"

    /** Franja por defecto de un negocio de barrio: 08:00 a 22:00 (en minutos del día). */
    const val HORARIO_INICIO_DEFECTO = 8 * 60   // 480
    const val HORARIO_FIN_DEFECTO = 22 * 60      // 1320

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

    // ── Silencio individual y horario de anuncios ──────────────────────────
    //
    // REGLA ANTI-FAKE (la de oro): esto NO toca el historial ni el reenvío.
    // Silenciar o estar fuera de horario solo CALLA la voz local de ESTE
    // teléfono. El pago igual se captura, se guarda en el registro y (en modo
    // captura) se sube al backend y se reparte por FCM a los demás equipos. Es
    // una decisión de ambiente por teléfono, no de la cuenta: el trabajador que
    // hoy no está silencia su equipo sin apagar nada del negocio.

    /** ¿Este teléfono está silenciado del todo? Default false (habla). */
    fun silenciado(contexto: Context): Boolean =
        prefs(contexto).getBoolean(CLAVE_SILENCIADO, false)

    fun definirSilenciado(contexto: Context, activo: Boolean) {
        prefs(contexto).edit().putBoolean(CLAVE_SILENCIADO, activo).apply()
    }

    /** ¿Anunciar solo dentro de una franja horaria? Default false (a toda hora). */
    fun horarioActivo(contexto: Context): Boolean =
        prefs(contexto).getBoolean(CLAVE_HORARIO_ACTIVO, false)

    fun definirHorarioActivo(contexto: Context, activo: Boolean) {
        prefs(contexto).edit().putBoolean(CLAVE_HORARIO_ACTIVO, activo).apply()
    }

    /** Inicio de la franja, en minutos del día (0..1439). Default 08:00. */
    fun horaInicio(contexto: Context): Int =
        prefs(contexto).getInt(CLAVE_HORARIO_INICIO, HORARIO_INICIO_DEFECTO)

    fun definirHoraInicio(contexto: Context, minutos: Int) {
        prefs(contexto).edit()
            .putInt(CLAVE_HORARIO_INICIO, minutos.coerceIn(0, 1439))
            .apply()
    }

    /** Fin de la franja, en minutos del día (0..1439). Default 22:00. */
    fun horaFin(contexto: Context): Int =
        prefs(contexto).getInt(CLAVE_HORARIO_FIN, HORARIO_FIN_DEFECTO)

    fun definirHoraFin(contexto: Context, minutos: Int) {
        prefs(contexto).edit()
            .putInt(CLAVE_HORARIO_FIN, minutos.coerceIn(0, 1439))
            .apply()
    }

    /**
     * ¿La voz LOCAL debe hablar AHORA? Único juez del silencio.
     *
     * Solo calla la voz; el historial ya quedó guardado por quien llama y el
     * reenvío FCM es independiente (ver comentario de arriba y Anunciador).
     */
    fun deboHablarAhora(contexto: Context): Boolean {
        if (silenciado(contexto)) return false
        if (!horarioActivo(contexto)) return true
        // Dentro de la franja; soporta cruce de medianoche (ej. 22:00 → 06:00).
        val cal = Calendar.getInstance()
        val ahora = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val ini = horaInicio(contexto)
        val fin = horaFin(contexto)
        return if (ini <= fin) ahora in ini until fin
        else ahora >= ini || ahora < fin
    }
}
