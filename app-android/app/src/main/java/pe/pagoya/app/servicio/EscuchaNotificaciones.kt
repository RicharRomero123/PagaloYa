package pe.pagoya.app.servicio

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Aprendizaje
import pe.pagoya.app.core.BilleteraParser
import pe.pagoya.app.core.RegistroPagos

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
            RegistroPagos.agregar(applicationContext, pago)
            Anunciador.anunciar(applicationContext, BilleteraParser.fraseDeVoz(pago))
        } else {
            // Notificación de billetera que no matcheó: alimenta el modo aprendizaje
            Aprendizaje.registrar(applicationContext, paquete, textoCompleto)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ServicioPrimerPlano.arrancar(this)
    }
}
