package pe.pagoya.app.core

import android.content.Context
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Parser modular de billeteras. Los patrones viven en assets/billeteras.json
 * (mismo esquema que tendrá Firebase Remote Config: al integrar Firebase, solo
 * cambia la fuente del JSON — la lógica queda idéntica).
 *
 * Convención de grupos regex: grupo 1 = nombre del pagador, grupo 2 = monto.
 */
object BilleteraParser {

    data class Billetera(
        val id: String,
        val nombre: String,
        val paquetes: List<String>,
        val patrones: List<Pattern>,
        val vozPlantilla: String,
    )

    private var billeteras: List<Billetera> = emptyList()

    fun cargar(context: Context) {
        val json = context.assets.open("billeteras.json").bufferedReader().use { it.readText() }
        cargarDesdeJson(json)
    }

    fun cargarDesdeJson(json: String) {
        val raiz = JSONObject(json)
        val arreglo = raiz.getJSONArray("billeteras")
        billeteras = (0 until arreglo.length()).map { i ->
            val o = arreglo.getJSONObject(i)
            Billetera(
                id = o.getString("id"),
                nombre = o.getString("nombre"),
                paquetes = o.getJSONArray("paquetes").let { p ->
                    (0 until p.length()).map { p.getString(it) }
                },
                patrones = o.getJSONArray("patrones").let { p ->
                    (0 until p.length()).mapNotNull {
                        runCatching { Pattern.compile(p.getString(it)) }.getOrNull()
                    }
                },
                vozPlantilla = o.getString("vozPlantilla"),
            )
        }
    }

    /** ¿Este paquete pertenece a una billetera vigilada? */
    fun billeteraDe(paquete: String): Billetera? =
        billeteras.find { paquete in it.paquetes }

    /**
     * Intenta extraer un pago del texto de la notificación.
     * Se prueba contra el texto completo (título + texto + bigText concatenados).
     */
    fun parsear(paquete: String, textoCompleto: String, timestamp: Long): Pago? {
        val billetera = billeteraDe(paquete) ?: return null
        for (patron in billetera.patrones) {
            val m = patron.matcher(textoCompleto)
            if (m.find() && m.groupCount() >= 2) {
                val nombre = m.group(1)?.trim().orEmpty().ifEmpty { "Alguien" }
                val monto = m.group(2)?.replace(',', '.')?.toDoubleOrNull() ?: continue
                return Pago(
                    billeteraId = billetera.id,
                    billeteraNombre = billetera.nombre,
                    pagador = limpiarNombre(nombre),
                    monto = monto,
                    timestamp = timestamp,
                )
            }
        }
        return null
    }

    /** Frase que dirá el parlante: "¡Pago Ya! Juan te yapeó 25 soles con 50" */
    fun fraseDeVoz(pago: Pago): String {
        val plantilla = billeteras.find { it.id == pago.billeteraId }?.vozPlantilla
            ?: "¡Pago Ya! {nombre} te pagó {monto}"
        return plantilla
            .replace("{nombre}", pago.pagador)
            .replace("{monto}", montoHablado(pago.monto))
    }

    /** 25.0 → "25 soles" · 25.5 → "25 soles con 50" · 1.0 → "1 sol" */
    fun montoHablado(monto: Double): String {
        val enteros = monto.toLong()
        val centimos = Math.round((monto - enteros) * 100).toInt()
        val soles = if (enteros == 1L) "1 sol" else "$enteros soles"
        return if (centimos > 0) "$soles con $centimos" else soles
    }

    private fun limpiarNombre(nombre: String): String =
        nombre.replace(Regex("[!¡*]"), "").trim().take(40)
}
