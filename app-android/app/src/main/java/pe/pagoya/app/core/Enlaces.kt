package pe.pagoya.app.core

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Enlaces del producto. El dominio web y las rutas son fijos (cambian con la
 * app). En cambio el WhatsApp de ventas y las redes son CONFIGURABLES en
 * caliente: viven en Firestore (config/enlaces) y el operador los edita desde
 * su panel sin republicar la app. Los valores de abajo son solo el FALLBACK
 * por si aún no se ha leído la nube (primer arranque sin internet).
 */
object Enlaces {

    private const val TAG = "PagoYa"

    /** Dominio base de la web de PagoYa. Sin barra al final. */
    const val WEB = "https://pagalo-ya.vercel.app"

    /** Centro de ayuda / soporte al que va el botón de Ajustes. */
    const val AYUDA = "$WEB/ayuda"

    /** Preguntas frecuentes. */
    const val FAQ = "$WEB/preguntas-frecuentes"

    /** Formulario / canal de consultas. */
    const val CONSULTAS = "$WEB/consultas"

    /** Política de privacidad (Play Store la exige en la ficha). */
    const val PRIVACIDAD = "$WEB/privacidad"

    /** Términos y condiciones. */
    const val TERMINOS = "$WEB/terminos"

    // ── Configurable desde el panel (Firestore config/enlaces) ──────────────
    // Se sobrescriben al leer la nube; aquí están los valores por defecto.
    // whatsappVentas en formato internacional sin + (ej. 51987654321).

    /**
     * Número de WhatsApp de ventas: el ÚNICO canal para subir de plan. El cobro
     * ocurre 100% fuera de la app (por conversación), nunca con un botón de pago
     * dentro — así se cumple la política de Play sobre bienes digitales.
     */
    @Volatile var whatsappVentas: String = "51999999999"
        private set

    @Volatile var instagram: String = ""
        private set

    @Volatile var tiktok: String = ""
        private set

    @Volatile var facebook: String = ""
        private set

    /** Enlace de WhatsApp de ventas con un mensaje ya redactado. */
    fun whatsappVentas(mensaje: String): String =
        "https://wa.me/$whatsappVentas?text=" + Uri.encode(mensaje)

    /**
     * Lee config/enlaces de Firestore y actualiza los valores en memoria. Se
     * llama al iniciar la app; si falla (sin red, doc no existe), se quedan los
     * valores por defecto — la app nunca se rompe por esto. Con caché offline
     * de Firestore, tras la primera lectura funciona aunque no haya internet.
     */
    fun cargarDesdeNube() {
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("config").document("enlaces")
                .get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) return@addOnSuccessListener
                    doc.getString("whatsappVentas")?.takeIf { it.isNotBlank() }
                        ?.let { whatsappVentas = it.filter(Char::isDigit) }
                    doc.getString("instagram")?.let { instagram = it }
                    doc.getString("tiktok")?.let { tiktok = it }
                    doc.getString("facebook")?.let { facebook = it }
                }
                .addOnFailureListener {
                    Log.w(TAG, "No se pudo leer config/enlaces: ${it.message}")
                }
        }
    }
}
