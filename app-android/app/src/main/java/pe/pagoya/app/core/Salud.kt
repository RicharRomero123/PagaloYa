package pe.pagoya.app.core

import android.content.Context

/**
 * Latido de salud de la captura. Registra cuándo el listener de notificaciones
 * estuvo conectado por última vez y cuándo vimos la última notificación de una
 * billetera. Con esto el Vigilante detecta que la app quedó "sorda" (Android
 * desvinculó el listener en silencio) y la UI puede mostrar un semáforo.
 */
object Salud {

    private const val PREFS = "pagoya_salud"
    private const val CLAVE_CONECTADO = "listener_conectado"
    private const val CLAVE_ULTIMA_CONEXION = "ultima_conexion"
    private const val CLAVE_ULTIMA_DESCONEXION = "ultima_desconexion"
    private const val CLAVE_ULTIMA_NOTIF = "ultima_notif_billetera"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun marcarConectado(context: Context) {
        prefs(context).edit()
            .putBoolean(CLAVE_CONECTADO, true)
            .putLong(CLAVE_ULTIMA_CONEXION, System.currentTimeMillis())
            .apply()
    }

    fun marcarDesconectado(context: Context) {
        prefs(context).edit()
            .putBoolean(CLAVE_CONECTADO, false)
            .putLong(CLAVE_ULTIMA_DESCONEXION, System.currentTimeMillis())
            .apply()
    }

    fun marcarNotificacionVista(context: Context) {
        prefs(context).edit()
            .putLong(CLAVE_ULTIMA_NOTIF, System.currentTimeMillis())
            .apply()
    }

    /** Última vez que el listener dio señales de vida (conexión o notificación). */
    fun listenerConectado(context: Context): Boolean =
        prefs(context).getBoolean(CLAVE_CONECTADO, false)

    fun ultimaConexion(context: Context): Long =
        prefs(context).getLong(CLAVE_ULTIMA_CONEXION, 0L)

    fun ultimaNotificacionBilletera(context: Context): Long =
        prefs(context).getLong(CLAVE_ULTIMA_NOTIF, 0L)
}
