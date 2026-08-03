package pe.pagoya.app.servicio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pe.pagoya.app.MainActivity
import pe.pagoya.app.R
import pe.pagoya.app.core.BandejaNotificaciones
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.Plan
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.TelemetriaRepo

/**
 * Único FirebaseMessagingService de la app (Android entrega todo el FCM a UNO
 * solo). Atiende dos cosas por el campo `data["tipo"]`:
 *
 *  - "pago": el corazón del MODO ESCUCHA. Una Cloud Function empuja cada pago
 *    capturado a los demás teléfonos del comercio (reemplaza al viejo listener
 *    de Firestore, que exigía la app viva y gastaba lecturas). Reconstruye el
 *    Pago y lo entrega a ComercioRepo, que lo anuncia por VOZ igual que antes.
 *    El foreground service mantiene vivo el proceso para que el push despierte
 *    aunque la app esté en segundo plano o cerrada.
 *
 *  - CAMPAÑAS del operador (ofertas, recordatorio de cierre): notificación
 *    normal en su propio canal, sin voz.
 */
class MensajesPagoYa : FirebaseMessagingService() {

    override fun onMessageReceived(mensaje: RemoteMessage) {
        val datos = mensaje.data
        if (datos["tipo"] == "pago") {
            anunciarPagoRemoto(datos)
            return
        }
        val notif = mensaje.notification
        val titulo = notif?.title ?: datos["titulo"] ?: "PagoYa"
        val cuerpo = notif?.body ?: datos["cuerpo"] ?: return
        // Guarda el aviso en el buzón (campanita) para que se pueda repasar
        // después, aunque el comerciante barra la notificación del sistema.
        BandejaNotificaciones.agregar(
            applicationContext, titulo, cuerpo, System.currentTimeMillis(),
        )
        mostrarCampana(this, titulo, cuerpo)
    }

    /**
     * Push de pago del modo escucha. Contrato data-only de la Function (todo
     * String): tipo, comercioId, pagoId, billeteraId, billeteraNombre, pagador,
     * monto (decimal en string, ej. "25.5"), timestamp (millis en string),
     * origenUid. ComercioRepo hace el anti-fake (comercio actual, no propio) y
     * el anti-duplicado por pagoId, y anuncia por voz.
     */
    private fun anunciarPagoRemoto(datos: Map<String, String>) {
        val comercioId = datos["comercioId"] ?: return
        val pagoId = datos["pagoId"] ?: return
        val origenUid = datos["origenUid"] ?: return
        val monto = datos["monto"]?.toDoubleOrNull() ?: return
        val pago = Pago(
            billeteraId = datos["billeteraId"] ?: "?",
            billeteraNombre = datos["billeteraNombre"] ?: "Pago",
            pagador = datos["pagador"] ?: "Alguien",
            monto = monto,
            timestamp = datos["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
        )
        ComercioRepo.recibirPagoRemoto(
            context = applicationContext,
            pagoId = pagoId,
            comercioId = comercioId,
            origenUid = origenUid,
            pago = pago,
        )
    }

    /**
     * Google rotó el token: re-subimos el latido con el token nuevo para que la
     * Function no siga empujando a un token muerto. Además re-suscribimos a los
     * topics de campaña (idempotente, no hace daño repetirlo).
     */
    override fun onNewToken(token: String) {
        TelemetriaRepo.reenviarTokenFcm(applicationContext, token)
        suscribirTopics(this)
    }

    companion object {
        const val CANAL_AVISOS = "pagoya_avisos"

        /** Topics: todos + el del plan, para poder segmentar campañas. */
        private const val TOPIC_TODOS = "todos"

        private fun topicPlan(plan: Plan) = "plan_${plan.id}"

        /**
         * Suscribe este teléfono a las campañas: al topic general y al de su
         * plan actual. Idempotente — FCM ignora suscripciones repetidas.
         */
        fun suscribirTopics(context: Context, plan: Plan = Plan.GRATIS) {
            val fm = FirebaseMessaging.getInstance()
            fm.subscribeToTopic(TOPIC_TODOS)
            fm.subscribeToTopic(topicPlan(plan))
            crearCanal(context)
        }

        /**
         * Al cambiar de plan: sale del topic viejo y entra al nuevo, para que
         * las campañas segmentadas por plan le lleguen al segmento correcto.
         */
        fun resuscribirPlan(anterior: Plan?, nuevo: Plan) {
            val fm = FirebaseMessaging.getInstance()
            if (anterior != null && anterior != nuevo) {
                fm.unsubscribeFromTopic(topicPlan(anterior))
            }
            fm.subscribeToTopic(topicPlan(nuevo))
        }

        private fun crearCanal(context: Context) {
            val canal = NotificationChannel(
                CANAL_AVISOS,
                context.getString(R.string.canal_avisos),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.canal_avisos_desc)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }

        private fun mostrarCampana(context: Context, titulo: String, cuerpo: String) {
            crearCanal(context)
            val abrir = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
            val notif = NotificationCompat.Builder(context, CANAL_AVISOS)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setColor(0xFFFF6B1A.toInt())
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
                .setContentIntent(abrir)
                .setAutoCancel(true)
                .build()
            runCatching {
                NotificationManagerCompat.from(context)
                    .notify(System.currentTimeMillis().toInt(), notif)
            }
        }
    }
}
