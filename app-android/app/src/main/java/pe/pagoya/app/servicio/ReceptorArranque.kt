package pe.pagoya.app.servicio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Si el teléfono se reinicia, PagoYa vuelve a escuchar solo, sin que nadie lo abra. */
class ReceptorArranque : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ServicioPrimerPlano.arrancar(context)
        }
    }
}
