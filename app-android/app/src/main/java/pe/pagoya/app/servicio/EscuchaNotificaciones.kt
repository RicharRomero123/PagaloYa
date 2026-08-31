package pe.pagoya.app.servicio

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Aprendizaje
import pe.pagoya.app.core.BilleteraParser
import pe.pagoya.app.core.PreferenciasBilleteras
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.core.Salud
import pe.pagoya.app.nube.ComercioRepo

/**
 * El corazón de PagoYa. Recibe TODAS las notificaciones del teléfono y solo
 * procesa las de billeteras vigiladas (allowlist en billeteras.json).
 *
 * Regla de oro anti-fake: un pago solo existe si llegó como notificación REAL
 * del sistema. Nada más crea pagos.
 */
class EscuchaNotificaciones : NotificationListenerService() {

    // Anti-duplicado: las billeteras a veces re-emiten la misma notificación
    private val vistos = ArrayDeque<Int>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val paquete = sbn.packageName ?: return
        if (BilleteraParser.billeteraDe(paquete) == null) return
        Salud.marcarNotificacionVista(this)

        val extras = sbn.notification?.extras ?: return
        val titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val textoGrande = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val completo = listOf(titulo, texto, textoGrande)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" · ")
        if (completo.isBlank()) return

        // Ventana de 30 s para deduplicar la misma notificación re-emitida
        val huella = (paquete + completo + (sbn.postTime / 30_000)).hashCode()
        if (huella in vistos) return
        vistos.addLast(huella)
        while (vistos.size > 50) vistos.removeFirst()

        procesar(paquete, completo, sbn.postTime)
    }

    private fun procesar(paquete: String, textoCompleto: String, timestamp: Long) {
        val pago = BilleteraParser.parsear(paquete, textoCompleto, timestamp)
        if (pago != null) {
            // Gate de billeteras: si el comerciante apagó esta billetera en ESTE
            // teléfono, la ignoramos por completo — no suena, no se guarda en el
            // historial y no se sube a la nube (nada de fan-out a trabajadores).
            // Va lo más temprano posible, apenas hay Pago.
            if (!PreferenciasBilleteras.estaActiva(pago.billeteraId)) {
                Log.d(TAG, "Pago descartado: billetera ${pago.billeteraId} apagada en este equipo")
                return
            }
            RegistroPagos.agregar(applicationContext, pago)
            Anunciador.anunciarPago(applicationContext, pago)
            // Y a la nube: para que suene en los teléfonos de los trabajadores
            ComercioRepo.subirPago(pago)
        } else {
            // Notificación de billetera que no matcheó: alimenta el modo aprendizaje
            Aprendizaje.registrar(applicationContext, paquete, textoCompleto)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Salud.marcarConectado(this)
        ServicioPrimerPlano.arrancar(this)
    }

    /**
     * Android a veces desvincula el listener (ahorro de batería agresivo, crash
     * del proceso, actualización) y NO lo reconecta solo: la app queda "sorda"
     * aunque el permiso siga dado. Pedimos la reconexión al toque.
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Salud.marcarDesconectado(this)
        reconectar(this)
    }

    companion object {
        private const val TAG = "PagoYa"

        /** Pide al sistema volver a vincular el listener. Inofensivo si ya está vivo. */
        fun reconectar(context: Context) {
            runCatching {
                requestRebind(ComponentName(context, EscuchaNotificaciones::class.java))
            }
        }
    }
}
