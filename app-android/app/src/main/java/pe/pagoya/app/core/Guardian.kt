package pe.pagoya.app.core

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.delay

/**
 * Guardián de Yape: vigila que la app de la billetera no esté "detenida"
 * (force-stop o asesinada por el ahorro de batería). Si Yape está detenida,
 * sus notificaciones NO llegan y las ventas no suenan — hay que avisar fuerte.
 */
object Guardian {

    const val PAQUETE_YAPE = "com.bcp.innovacxion.yapeapp"

    enum class EstadoBilletera { OK, DETENIDA, NO_INSTALADA }

    fun estadoDe(context: Context, paquete: String): EstadoBilletera = try {
        val info = context.packageManager.getApplicationInfo(paquete, 0)
        if (info.flags and ApplicationInfo.FLAG_STOPPED != 0) {
            EstadoBilletera.DETENIDA
        } else {
            EstadoBilletera.OK
        }
    } catch (e: PackageManager.NameNotFoundException) {
        EstadoBilletera.NO_INSTALADA
    }

    fun estadoYape(context: Context): EstadoBilletera = estadoDe(context, PAQUETE_YAPE)

    /**
     * Abre Yape. La usa el usuario tocando el botón de alerta (foreground), y
     * también el auto-revivir desde segundo plano.
     *
     * @param desdeFondo true cuando se lanza sin que haya una pantalla nuestra
     *        al frente (worker/servicio). Desde Android 10 lanzar una activity
     *        en segundo plano está bloqueado salvo con permiso de "aparecer
     *        encima" (overlay) Y pasando ActivityOptions que autorice el
     *        background launch. Sin eso el sistema descarta el startActivity en
     *        silencio y Yape NUNCA revive.
     */
    fun abrirYape(context: Context, desdeFondo: Boolean = false): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(PAQUETE_YAPE)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            if (desdeFondo && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: hay que declarar explícitamente que permitimos el
                // arranque en segundo plano (lo respalda el permiso de overlay).
                val opciones = ActivityOptions.makeBasic().apply {
                    pendingIntentBackgroundActivityStartMode =
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
                context.startActivity(intent, opciones.toBundle())
            } else {
                context.startActivity(intent)
            }
            true
        }.getOrDefault(false)
    }

    /** Con "aparecer encima" concedido podemos lanzar Yape desde segundo plano. */
    fun puedeRevivir(context: Context): Boolean = Settings.canDrawOverlays(context)

    /**
     * Auto-curación, cero manos: si Yape está detenida, la revivimos nosotros
     * lanzándola desde segundo plano (necesita "aparecer encima"). Al arrancar
     * su proceso, Yape vuelve a recibir sus push y las ventas suenan de nuevo.
     * Solo si no podemos revivirla (o falló), alertamos por voz al comerciante.
     *
     * Es `suspend` para no bloquear el hilo mientras espera la resurrección, y
     * reintenta un par de veces porque el proceso de Yape puede tardar en
     * arrancar en gama media.
     */
    suspend fun autoCurar(context: Context) {
        if (estadoYape(context) != EstadoBilletera.DETENIDA) return

        if (puedeRevivir(context)) {
            // Hasta 3 intentos: revivir y comprobar que de verdad arrancó.
            repeat(INTENTOS_RESURRECCION) { intento ->
                val lanzada = abrirYape(context, desdeFondo = true)
                delay(ESPERA_RESURRECCION)
                if (estadoYape(context) != EstadoBilletera.DETENIDA) {
                    anunciarConFreno(
                        context,
                        "Ojo: tu Yape se había apagado. Ya lo prendí de nuevo, sigue tranquilo."
                    )
                    return
                }
                if (!lanzada) return@repeat
            }
        }

        // No pudimos revivirla sola: que el comerciante la abra a mano.
        anunciarConFreno(
            context,
            "¡Atención! Tu Yape está apagado. Ábrelo ahora o tus ventas no van a sonar."
        )
    }

    // La curación la disparan dos vigías (servicio y worker): frenar la repetición
    private var ultimaAlerta = 0L
    private const val SILENCIO_ENTRE_ALERTAS = 25 * 60 * 1000L
    private const val ESPERA_RESURRECCION = 5_000L
    private const val INTENTOS_RESURRECCION = 3

    private fun anunciarConFreno(context: Context, mensaje: String) {
        val ahora = System.currentTimeMillis()
        if (ahora - ultimaAlerta < SILENCIO_ENTRE_ALERTAS) return
        ultimaAlerta = ahora
        Anunciador.anunciar(context, mensaje)
    }
}
