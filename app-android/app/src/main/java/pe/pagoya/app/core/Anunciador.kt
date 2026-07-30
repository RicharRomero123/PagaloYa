package pe.pagoya.app.core

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * La voz de PagoYa. Anuncia los pagos con TTS en español.
 * "Voz fuerte": sube el volumen multimedia al máximo durante el anuncio y
 * lo restaura al terminar (en el mostrador el volumen bajo = venta perdida).
 */
object Anunciador {

    private const val PREFS = "pagoya_config"
    private const val CLAVE_VOZ_FUERTE = "voz_fuerte"

    private var tts: TextToSpeech? = null
    private var listo = false
    private val contador = AtomicInteger(0)
    private var volumenPrevio: Int? = null

    fun inicializar(context: Context) {
        if (tts != null) return
        val appContext = context.applicationContext
        tts = TextToSpeech(appContext) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                val motor = tts ?: return@TextToSpeech
                val peruano = Locale("es", "PE")
                val resultado = motor.setLanguage(peruano)
                if (resultado == TextToSpeech.LANG_MISSING_DATA ||
                    resultado == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    motor.setLanguage(Locale("es", "US"))
                }
                motor.setSpeechRate(0.95f)
                motor.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onError(id: String?) = restaurarVolumen(appContext)
                    override fun onDone(id: String?) = restaurarVolumen(appContext)
                })
                listo = true
            }
        }
    }

    fun anunciar(context: Context, frase: String) {
        val motor = tts
        if (motor == null || !listo) {
            inicializar(context)
            return
        }
        if (vozFuerte(context)) subirVolumen(context)
        motor.speak(frase, TextToSpeech.QUEUE_ADD, null, "pagoya-${contador.incrementAndGet()}")
    }

    fun vozFuerte(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(CLAVE_VOZ_FUERTE, true)

    fun definirVozFuerte(context: Context, activa: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(CLAVE_VOZ_FUERTE, activa).apply()
    }

    private fun subirVolumen(context: Context) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (volumenPrevio == null) {
            volumenPrevio = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        }
        audio.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }

    private fun restaurarVolumen(context: Context) {
        val previo = volumenPrevio ?: return
        // Solo restaurar cuando ya no queden anuncios en cola
        if (tts?.isSpeaking == true) return
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, previo, 0)
        volumenPrevio = null
    }
}
