package pe.pagoya.app.core

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.util.Calendar

/**
 * Umbrales del Modo ciego. Viven en Firebase Remote Config para poder ajustarlos
 * sin republicar el APK (igual que los patrones de billeteras), con defaults en
 * código por si aún no se han fetcheado o no hay internet.
 *
 * Regla de oro del negocio: "si no suena, no te pagaron". El Modo ciego es la
 * red de seguridad de esa promesa — si el teléfono de la caja deja de escuchar,
 * TODOS los que oyen ese comercio se enteran al toque.
 *
 * Solo se vigila EN HORARIO DE NEGOCIO: de madrugada nadie vende y no tiene
 * sentido gritar "ciego" (ni gastar batería/escrituras escribiendo presencia).
 */
object ConfigCiego {

    // Claves de Remote Config (mismas que definirá el backend en la consola).
    private const val CLAVE_CADENCIA = "ciego_cadencia_seg"
    private const val CLAVE_UMBRAL = "ciego_umbral_seg"
    private const val CLAVE_HORARIO_INICIO = "ciego_horario_inicio"
    private const val CLAVE_HORARIO_FIN = "ciego_horario_fin"

    // Defaults en código (segundos / hora local 0-23).
    private const val CADENCIA_DEFECTO = 90L
    private const val UMBRAL_DEFECTO = 240L
    private const val HORARIO_INICIO_DEFECTO = 7L
    private const val HORARIO_FIN_DEFECTO = 22L

    /** Cada cuánto el capturador escribe su latido de presencia (ms). */
    fun cadenciaMs(): Long = leer(CLAVE_CADENCIA, CADENCIA_DEFECTO) * 1000L

    /** Sin presencia por más de esto ⇒ CIEGO (ms). */
    fun umbralMs(): Long = leer(CLAVE_UMBRAL, UMBRAL_DEFECTO) * 1000L

    /** Hora (0-23) en que abre la vigilancia del negocio. */
    fun horarioInicio(): Int = leer(CLAVE_HORARIO_INICIO, HORARIO_INICIO_DEFECTO).toInt()

    /** Hora (0-23) en que cierra la vigilancia del negocio. */
    fun horarioFin(): Int = leer(CLAVE_HORARIO_FIN, HORARIO_FIN_DEFECTO).toInt()

    /**
     * ¿Estamos en horario de negocio ahora mismo (hora local)? Ventana
     * [inicio, fin): a las 22:00 ya está cerrado. Fuera de ella NO escribimos
     * presencia ni gritamos ciego.
     */
    fun enHorarioNegocio(ahoraMillis: Long = System.currentTimeMillis()): Boolean {
        val hora = Calendar.getInstance().apply { timeInMillis = ahoraMillis }
            .get(Calendar.HOUR_OF_DAY)
        return hora in horarioInicio() until horarioFin()
    }

    /**
     * Lee un entero de Remote Config con respaldo. Remote Config ya viene
     * "activado" por PagoYaApp (fetchAndActivate al arrancar); si el valor no
     * está o es 0 (default de origen "static" cuando no hay clave), usamos el
     * default de código para no dejar la vigilancia en cero.
     */
    private fun leer(clave: String, defecto: Long): Long = runCatching {
        val valor = FirebaseRemoteConfig.getInstance().getLong(clave)
        if (valor > 0) valor else defecto
    }.getOrDefault(defecto)
}
