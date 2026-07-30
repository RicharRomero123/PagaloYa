package pe.pagoya.app.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

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

    /** Intent para abrir Yape (revivirla la saca del estado detenido). */
    fun abrirYape(context: Context) {
        context.packageManager.getLaunchIntentForPackage(PAQUETE_YAPE)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }

    /** Alerta hablada. El servicio la usa en su chequeo periódico. */
    fun alertaSiDetenida(context: Context) {
        if (estadoYape(context) == EstadoBilletera.DETENIDA) {
            Anunciador.anunciar(
                context,
                "¡Atención! Tu Yape está apagado. Ábrelo ahora o tus ventas no van a sonar."
            )
        }
    }
}
