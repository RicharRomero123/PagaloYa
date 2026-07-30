package pe.pagoya.app.servicio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import pe.pagoya.app.MainActivity
import pe.pagoya.app.R

/**
 * Servicio de primer plano: mantiene vivo el proceso para que el listener y el
 * TTS nunca sean sacrificados por el ahorro de batería (crítico en gama media).
 */
class ServicioPrimerPlano : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanal()
        val abrirApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = Notification.Builder(this, CANAL_ID)
            .setContentTitle(getString(R.string.notif_servicio_titulo))
            .setContentText(getString(R.string.notif_servicio_texto))
            .setSmallIcon(R.drawable.ic_altavoz)
            .setContentIntent(abrirApp)
            .setOngoing(true)
            .build()
        startForeground(NOTIF_ID, notificacion)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun crearCanal() {
        val gestor = getSystemService(NotificationManager::class.java)
        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_ID,
                getString(R.string.canal_servicio),
                NotificationManager.IMPORTANCE_MIN
            )
        )
    }

    companion object {
        private const val CANAL_ID = "pagoya_activo"
        private const val NOTIF_ID = 1

        fun arrancar(context: Context) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, ServicioPrimerPlano::class.java)
                )
            }
        }
    }
}
