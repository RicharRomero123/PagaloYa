package pe.pagoya.app.servicio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Si el teléfono se reinicia o PagoYa se actualiza, vuelve a escuchar solo,
 * sin que nadie lo abra. Tras una actualización Android suelta el listener
 * y no siempre lo reconecta: hay que pedirlo.
 */
class ReceptorArranque : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                ServicioPrimerPlano.arrancar(context)
                EscuchaNotificaciones.reconectar(context)
            }
        }
    }
}
