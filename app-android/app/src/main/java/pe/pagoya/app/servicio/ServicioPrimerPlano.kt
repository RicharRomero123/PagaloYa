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
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.Sesion
import pe.pagoya.app.nube.TelemetriaRepo

/**
 * Servicio de primer plano: mantiene vivo el proceso para que el listener y el
 * TTS nunca sean sacrificados por el ahorro de batería (crítico en gama media).
 */
class ServicioPrimerPlano : Service() {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var guardianActivo = false

    /**
     * La rehidratación de la Caja se hace una sola vez por vida del servicio
     * (equivale a "una vez por sesión/arranque"): onStartCommand se re-ejecuta
     * en cada onResume de la app y no queremos gastar una lectura cada vez.
     */
    private var cajaRehidratada = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Si el sistema soltó el listener mientras estábamos muertos, re-vincularlo
        EscuchaNotificaciones.reconectar(this)
        prepararModoEscucha()
        vigilarYape()
        // Offline Guard: vigila la conexión y avisa cuando se cae la red.
        RedGuardia.iniciar(this)
        crearCanal()
        val abrirApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = Notification.Builder(this, CANAL_ID)
            .setContentTitle(getString(R.string.notif_servicio_titulo))
            .setContentText(getString(R.string.notif_servicio_texto))
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(0xFFFF6B1A.toInt())
            .setContentIntent(abrirApp)
            .setOngoing(true)
            .build()
        startForeground(NOTIF_ID, notificacion)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Modo escucha: este teléfono anuncia los pagos que capturan OTROS teléfonos
     * del comercio (trabajadores/dueño remoto oyen lo que captura el teléfono
     * del negocio).
     *
     * Ya NO abrimos un listener de Firestore: el anuncio llega por PUSH FCM
     * (Cloud Function → MensajesPagoYa) aunque la app esté en segundo plano o
     * cerrada. Aquí solo dejamos listo el comercio en memoria (para que el push
     * sepa cuál es el actual) y las suscripciones a las campañas del operador.
     */
    private fun prepararModoEscucha() {
        if (!Sesion.conectado()) return
        alcance.launch {
            runCatching {
                val comercio = ComercioRepo.cargar() ?: return@launch
                // Campañas del operador: suscribir a los topics de este teléfono
                // según su plan (todos + plan_x). Idempotente.
                MensajesPagoYa.suscribirTopics(applicationContext, comercio.plan)
                // El token FCM viaja en el latido; forzamos uno al arrancar para
                // que la Function tenga a dónde empujar cuanto antes.
                TelemetriaRepo.subirLatido(applicationContext)
                // Rehidratar la Caja desde Firestore UNA vez por arranque, para
                // que el historial no se sienta perdido tras reinstalar/cambiar
                // de teléfono. Silenciosa: no anuncia. Solo si el usuario va a
                // ver la caja (dueño o trabajador con caja visible); si la caja
                // está oculta, no gastamos lecturas.
                if (!cajaRehidratada &&
                    (comercio.rol == "dueno" || comercio.trabajadorVeCaja)
                ) {
                    cajaRehidratada = true
                    ComercioRepo.rehidratarPagos(applicationContext)
                }
            }
        }
    }

    /**
     * Guardián de Yape: cada 5 min verifica que Yape no esté detenida y, si lo
     * está, la revive solo (autoCurar). El chequeo es barato (un flag del
     * PackageManager), así la ventana en que Yape puede estar muerta es corta.
     */
    private fun vigilarYape() {
        if (guardianActivo) return
        guardianActivo = true
        alcance.launch {
            while (isActive) {
                Guardian.autoCurar(applicationContext)
                // El latido tiene su propio freno interno: no satura escrituras
                TelemetriaRepo.subirLatido(applicationContext)
                delay(5 * 60 * 1000L)
            }
        }
    }

    override fun onDestroy() {
        RedGuardia.detener(this)
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
