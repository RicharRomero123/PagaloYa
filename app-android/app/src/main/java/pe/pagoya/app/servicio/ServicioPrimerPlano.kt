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
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.pagoya.app.core.Guardian
import pe.pagoya.app.MainActivity
import pe.pagoya.app.R
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.Sesion

/**
 * Servicio de primer plano: mantiene vivo el proceso para que el listener y el
 * TTS nunca sean sacrificados por el ahorro de batería (crítico en gama media).
 */
class ServicioPrimerPlano : Service() {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var oidoNube: ListenerRegistration? = null

    private var guardianActivo = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        conectarOidoNube()
        vigilarYape()
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

    /**
     * Modo escucha: este teléfono anuncia los pagos que capturan OTROS teléfonos
     * del comercio (trabajadores oyen lo que captura el teléfono del dueño).
     */
    private fun conectarOidoNube() {
        if (oidoNube != null || !Sesion.conectado()) return
        alcance.launch {
            runCatching {
                ComercioRepo.cargar() ?: return@launch
                oidoNube = ComercioRepo.escucharPagos { pago ->
                    RegistroPagos.agregar(applicationContext, pago)
                    Anunciador.anunciarPago(applicationContext, pago)
                }
            }
        }
    }

    /** Guardián de Yape: cada 30 min verifica que Yape no esté detenida. */
    private fun vigilarYape() {
        if (guardianActivo) return
        guardianActivo = true
        alcance.launch {
            while (isActive) {
                Guardian.alertaSiDetenida(applicationContext)
                delay(30 * 60 * 1000L)
            }
        }
    }

    override fun onDestroy() {
        oidoNube?.remove()
        oidoNube = null
        alcance.cancel()
        super.onDestroy()
    }

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
