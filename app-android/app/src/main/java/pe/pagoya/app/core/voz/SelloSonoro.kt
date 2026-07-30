package pe.pagoya.app.core.voz

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/**
 * El sello sonoro de PagoYa: el "¡PagoYa!" que abre cada anuncio.
 *
 * BRAND.md lo define como la marca sonora — lo que hace que el puesto vecino
 * reconozca el sonido. Un TTS nunca va a sonar igual de bien que una grabación
 * real, y además el sello es SIEMPRE la misma frase: no hay razón para
 * sintetizarla en cada venta.
 *
 * Cómo activarlo: dejar el audio en `res/raw/sello_pagoya.ogg` (o .mp3/.wav).
 * No hace falta tocar código — se busca por nombre en tiempo de ejecución, así
 * que la app compila y funciona igual sin el archivo, usando solo TTS.
 *
 * Recomendación: grabarlo con voz peruana femenina o generarlo con un TTS
 * neuronal de escritorio (ver README). Menos de 1 segundo, alegre, con punch.
 */
object SelloSonoro {

    private const val NOMBRE = "sello_pagoya"
    private const val TAG = "PagoYa"

    private fun idRecurso(contexto: Context): Int =
        contexto.resources.getIdentifier(NOMBRE, "raw", contexto.packageName)

    fun existe(contexto: Context): Boolean = idRecurso(contexto) != 0

    /**
     * Reproduce el sello y llama a [alTerminar] al acabar. Si no hay archivo o
     * algo falla, llama a [alTerminar] de inmediato: el anuncio nunca se pierde
     * por culpa del sello.
     */
    fun reproducir(contexto: Context, alTerminar: () -> Unit) {
        val id = idRecurso(contexto)
        if (id == 0) {
            alTerminar()
            return
        }
        val reproductor = runCatching {
            MediaPlayer.create(contexto.applicationContext, id)
        }.getOrNull()
        if (reproductor == null) {
            Log.w(TAG, "No se pudo cargar el sello sonoro")
            alTerminar()
            return
        }
        reproductor.setOnCompletionListener {
            it.release()
            alTerminar()
        }
        reproductor.setOnErrorListener { mp, _, _ ->
            mp.release()
            alTerminar()
            true
        }
        runCatching { reproductor.start() }.onFailure {
            reproductor.release()
            alTerminar()
        }
    }
}
