package pe.pagoya.app.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Vibración de refuerzo para los avisos que tienen que sentirse aunque el
 * mostrador esté con bulla: el botón de pánico y el aviso de "modo sin red".
 *
 * Todo va envuelto en runCatching: un teléfono sin vibrador (o con el permiso
 * negado por el fabricante) nunca debe tumbar el aviso hablado, que es lo que
 * de verdad importa.
 */
object Vibraciones {

    /** Golpe fuerte y largo: acompaña la alerta de pánico. */
    fun fuerte(context: Context) {
        vibrar(context, longArrayOf(0, 400, 150, 400))
    }

    /** Doble toque corto: avisa que se cayó el internet. */
    fun aviso(context: Context) {
        vibrar(context, longArrayOf(0, 200, 120, 200))
    }

    private fun vibrar(context: Context, patron: LongArray) {
        runCatching {
            val vibrador = obtener(context) ?: return
            if (!vibrador.hasVibrator()) return
            vibrador.vibrate(VibrationEffect.createWaveform(patron, -1))
        }
    }

    private fun obtener(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val gestor = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            gestor?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}
