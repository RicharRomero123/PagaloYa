package pe.pagoya.app.core.voz

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

/**
 * Catálogo de voces del teléfono.
 *
 * Por qué existe: el "robot de Android" no es una limitación de Android, es el
 * motor eligiendo mal. Los teléfonos suelen traer varias voces en español —
 * desde la vieja sintética hasta las neuronales de Google — y por defecto se
 * quedan con cualquiera. Aquí las listamos, las puntuamos y dejamos que el
 * comerciante escuche y elija.
 *
 * Criterios de puntaje, en orden de importancia para PagoYa:
 *  1. que sea español latino (una voz de España suena ajena en una bodega)
 *  2. que sea de alta calidad (las neuronales son las que no suenan a robot)
 *  3. que funcione SIN internet (en el mercado el dato se cae y la venta igual
 *     tiene que sonar) — por eso una voz de red pierde puntos aunque suene mejor
 */
object CatalogoVoces {

    const val MOTOR_GOOGLE = "com.google.android.tts"

    data class VozDisponible(
        /** `Voice.name`, el identificador que guardamos. */
        val id: String,
        val etiqueta: String,
        val descripcion: String,
        val necesitaInternet: Boolean,
        val puntaje: Int,
    )

    /** Voces en español utilizables, de la mejor a la peor. */
    fun disponibles(tts: TextToSpeech): List<VozDisponible> {
        val voces = runCatching { tts.voices }.getOrNull().orEmpty()
        return voces
            .filterNotNull()
            .filter { usable(it) }
            .map { it to puntuar(it) }
            .sortedByDescending { it.second }
            .mapIndexed { indice, (voz, puntaje) ->
                VozDisponible(
                    id = voz.name,
                    etiqueta = "Voz ${indice + 1}",
                    descripcion = describir(voz),
                    necesitaInternet = voz.isNetworkConnectionRequired,
                    puntaje = puntaje,
                )
            }
    }

    /** La que PagoYa elige sola si el usuario no ha tocado nada. */
    fun mejor(tts: TextToSpeech): Voice? =
        runCatching { tts.voices }.getOrNull().orEmpty()
            .filterNotNull()
            .filter { usable(it) }
            .maxByOrNull { puntuar(it) }

    fun porId(tts: TextToSpeech, id: String?): Voice? {
        if (id == null) return null
        return runCatching { tts.voices }.getOrNull().orEmpty()
            .filterNotNull()
            .firstOrNull { it.name == id }
    }

    /**
     * El motor de Google es el que trae las voces neuronales. Si el teléfono usa
     * otro (Samsung TTS, Pico…), lo más probable es que suene a robot por más
     * que el usuario cambie de voz.
     */
    fun usaMotorGoogle(tts: TextToSpeech): Boolean =
        runCatching { tts.defaultEngine }.getOrNull() == MOTOR_GOOGLE

    fun instalarMotorGoogle(contexto: Context) {
        val tienda = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$MOTOR_GOOGLE")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            contexto.startActivity(tienda)
        } catch (e: ActivityNotFoundException) {
            contexto.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$MOTOR_GOOGLE")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** Ajustes → Texto a voz: ahí se cambia el motor y se descargan más voces. */
    fun abrirAjustesDeVoz(contexto: Context): Boolean = runCatching {
        contexto.startActivity(
            Intent("com.android.settings.TTS_SETTINGS")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    }.getOrDefault(false)

    // ── Puntaje ───────────────────────────────────────────────────────────

    private fun usable(voz: Voice): Boolean {
        if (!esEspanol(voz.locale)) return false
        val rasgos = voz.features.orEmpty()
        if (TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED in rasgos) return false
        return true
    }

    private fun puntuar(voz: Voice): Int {
        var puntos = 0
        puntos += when (pais(voz.locale)) {
            "PE" -> 300                                     // peruana: lo ideal
            "US", "MX", "CO", "AR", "CL", "419" -> 200      // latina: suena de casa
            "ES" -> 60                                      // española: se entiende, pero es ajena
            else -> 120
        }
        puntos += when (voz.quality) {
            Voice.QUALITY_VERY_HIGH -> 200
            Voice.QUALITY_HIGH -> 150
            Voice.QUALITY_NORMAL -> 80
            Voice.QUALITY_LOW -> 30
            else -> 0
        }
        // Si el dato se cae y la voz necesitaba internet, la venta no suena.
        // Ese riesgo pesa más que sonar un poco mejor.
        if (voz.isNetworkConnectionRequired) puntos -= 120
        return puntos
    }

    private fun describir(voz: Voice): String {
        val acento = when (pais(voz.locale)) {
            "PE" -> "Peruana"
            "US", "MX", "CO", "AR", "CL", "419" -> "Latina"
            "ES" -> "De España"
            else -> "Español"
        }
        val calidad = when (voz.quality) {
            Voice.QUALITY_VERY_HIGH -> "muy nítida"
            Voice.QUALITY_HIGH -> "nítida"
            Voice.QUALITY_NORMAL -> "normal"
            else -> "básica"
        }
        val red = if (voz.isNetworkConnectionRequired) "necesita internet" else "sin internet"
        return "$acento · $calidad · $red"
    }

    /**
     * Ojo: algunos motores devuelven el locale en ISO3 (`spa-PER`) y otros en
     * ISO2 (`es-PE`). Hay que aguantar los dos o el filtro deja fuera voces
     * buenas en teléfonos reales.
     */
    private fun esEspanol(locale: Locale): Boolean {
        val idioma = locale.language.lowercase(Locale.ROOT)
        if (idioma == "es" || idioma == "spa") return true
        return runCatching { locale.isO3Language.lowercase(Locale.ROOT) == "spa" }
            .getOrDefault(false)
    }

    private fun pais(locale: Locale): String = when (locale.country.uppercase(Locale.ROOT)) {
        "PER", "PE" -> "PE"
        "USA", "US" -> "US"
        "MEX", "MX" -> "MX"
        "COL", "CO" -> "CO"
        "ARG", "AR" -> "AR"
        "CHL", "CL" -> "CL"
        "ESP", "ES" -> "ES"
        else -> locale.country.uppercase(Locale.ROOT)
    }
}
