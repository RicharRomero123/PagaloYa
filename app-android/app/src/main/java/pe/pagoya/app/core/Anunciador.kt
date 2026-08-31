package pe.pagoya.app.core

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pe.pagoya.app.core.voz.CatalogoVoces
import pe.pagoya.app.core.voz.PreferenciasVoz
import pe.pagoya.app.core.voz.SelloSonoro
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * La voz de PagoYa. Es la fachada: todo el resto de la app anuncia por aquí.
 *
 * Un anuncio son dos piezas:
 *   1. el sello sonoro "¡PagoYa!" — grabado, si está disponible (ver SelloSonoro)
 *   2. la frase con el pagador y el monto — sintetizada con la voz elegida
 *
 * "Voz fuerte" sube el volumen multimedia al máximo mientras habla y lo
 * restaura al terminar: en el mostrador, volumen bajo = venta perdida.
 */
object Anunciador {

    private var tts: TextToSpeech? = null
    private val contador = AtomicInteger(0)
    private var volumenPrevio: Int? = null

    private val _listo = MutableStateFlow(false)

    /** Para que la pantalla de voces sepa cuándo puede pedir el catálogo. */
    val listo: StateFlow<Boolean> = _listo

    fun inicializar(context: Context) {
        if (tts != null) return
        val appContext = context.applicationContext
        tts = TextToSpeech(appContext) { estado ->
            if (estado != TextToSpeech.SUCCESS) return@TextToSpeech
            val motor = tts ?: return@TextToSpeech

            val peruano = Locale("es", "PE")
            val resultado = motor.setLanguage(peruano)
            if (resultado == TextToSpeech.LANG_MISSING_DATA ||
                resultado == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                motor.setLanguage(Locale("es", "US"))
            }

            motor.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onError(id: String?) = restaurarVolumen(appContext)
                override fun onDone(id: String?) = restaurarVolumen(appContext)
            })

            aplicarPreferencias(appContext)
            _listo.value = true
        }
    }

    /**
     * Aplica la voz, velocidad y tono guardados. Si el usuario no eligió voz,
     * PagoYa toma la mejor del teléfono (latina, nítida y que ande sin internet).
     */
    fun aplicarPreferencias(context: Context) {
        val motor = tts ?: return
        val elegida = PreferenciasVoz.vozElegida(context)
        val voz = CatalogoVoces.porId(motor, elegida) ?: CatalogoVoces.mejor(motor)
        if (voz != null) runCatching { motor.setVoice(voz) }
        motor.setSpeechRate(PreferenciasVoz.velocidad(context))
        motor.setPitch(PreferenciasVoz.tono(context))
    }

    /**
     * Reinicia el motor. Hace falta cuando el usuario instala Google TTS o
     * cambia el motor del sistema: las voces nuevas no aparecen sin esto.
     */
    fun reiniciar(context: Context) {
        _listo.value = false
        runCatching { tts?.shutdown() }
        tts = null
        inicializar(context)
    }

    fun catalogo(context: Context): List<CatalogoVoces.VozDisponible> {
        val motor = tts ?: return emptyList()
        return CatalogoVoces.disponibles(motor)
    }

    fun usaMotorGoogle(): Boolean {
        val motor = tts ?: return true // sin motor todavía, no alarmamos
        return CatalogoVoces.usaMotorGoogle(motor)
    }

    /**
     * Anuncia un pago. Es la puerta que usa toda la app: aquí se decide, en un
     * solo lugar, si se dice el nombre del pagador (apagado por defecto).
     */
    fun anunciarPago(context: Context, pago: Pago) {
        val appContext = context.applicationContext
        // Único gate del silencio individual / horario de anuncios. Si este
        // teléfono está silenciado o fuera de su franja, la VOZ se calla y ya.
        // El historial YA lo guardó quien nos llamó (captura o FCM), y el
        // reenvío al backend (subirPago) es independiente de esto: no anunciar
        // nunca deja de capturar ni de repartir. Anti-fake intacto.
        if (!PreferenciasVoz.deboHablarAhora(appContext)) return
        val conNombre = PreferenciasVoz.decirNombre(appContext)
        anunciar(appContext, BilleteraParser.fraseDeVoz(pago, conNombre))
    }

    /** Anuncio completo: sello grabado (si existe) + frase hablada. */
    fun anunciar(context: Context, frase: String) {
        val appContext = context.applicationContext
        if (tts == null || !_listo.value) {
            inicializar(appContext)
            return
        }
        if (PreferenciasVoz.vozFuerte(appContext)) subirVolumen(appContext)

        if (SelloSonoro.existe(appContext)) {
            // El sello ya dice "¡PagoYa!": que el TTS no lo repita.
            val resto = sinSello(frase)
            SelloSonoro.reproducir(appContext) { hablar(resto) }
        } else {
            hablar(frase)
        }
    }

    private fun hablar(frase: String) {
        if (frase.isBlank()) return
        tts?.speak(
            frase,
            TextToSpeech.QUEUE_ADD,
            null,
            "pagoya-${contador.incrementAndGet()}",
        )
    }

    /**
     * Alerta de PÁNICO: el comerciante la dispara a mano delante de un cliente
     * que muestra un "pantallazo falso" para presionarlo. Suena SIEMPRE a
     * volumen máximo (ignora la preferencia "Voz fuerte": es disuasiva, tiene
     * que oírse) y va sin el sello "¡PagoYa!" — no es un pago, es un aviso.
     *
     * Debounce simple: si ya está sonando una alerta, no se vuelve a disparar,
     * así un doble toque no la encima consigo misma.
     */
    fun anunciarPanico(context: Context, frase: String) {
        val appContext = context.applicationContext
        if (tts == null || !_listo.value) {
            inicializar(appContext)
            return
        }
        // Debounce: mientras habla, ignorar toques repetidos.
        if (tts?.isSpeaking == true) return
        subirVolumen(appContext)
        // QUEUE_FLUSH: la alerta manda, corta cualquier cosa en cola.
        tts?.speak(
            frase,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "pagoya-panico-${contador.incrementAndGet()}",
        )
    }

    /** true si el motor está anunciando algo ahora mismo (para el debounce). */
    fun estaHablando(): Boolean = tts?.isSpeaking == true

    /**
     * Quita el "¡Pago Ya!" del inicio de la plantilla cuando el sello grabado
     * ya lo dijo. Las plantillas viven en Remote Config, así que el recorte se
     * hace aquí y no en el parser.
     */
    private fun sinSello(frase: String): String =
        frase.replaceFirst(Regex("^\\s*[¡!]*\\s*pago\\s*ya\\s*[!¡]*\\s*", RegexOption.IGNORE_CASE), "")
            .trim()

    // ── Preferencias que ya usaba la UI (se mantienen por compatibilidad) ──

    fun vozFuerte(context: Context): Boolean = PreferenciasVoz.vozFuerte(context)

    fun definirVozFuerte(context: Context, activa: Boolean) =
        PreferenciasVoz.definirVozFuerte(context, activa)

    // ── Volumen ───────────────────────────────────────────────────────────

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
