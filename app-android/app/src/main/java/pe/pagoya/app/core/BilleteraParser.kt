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
        /** Con el nombre del pagador. Solo se usa si el comercio lo activó. */
        val vozPlantilla: String,
        /** Sin nombre: la de por defecto. Ver PreferenciasVoz.decirNombre. */
        val vozPlantillaSinNombre: String,
    )

    /** Respaldo si una billetera nueva no trae plantilla sin nombre. */
    private const val SIN_NOMBRE_DEFECTO = "¡Pago Ya! Entraron {monto}"

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
                vozPlantillaSinNombre = o.optString("vozPlantillaSinNombre")
                    .ifBlank { SIN_NOMBRE_DEFECTO },
            )
        }
    }

    /** ¿Este paquete pertenece a una billetera vigilada? */
    fun billeteraDe(paquete: String): Billetera? =
        billeteras.find { paquete in it.paquetes }

    /**
     * Catálogo de billeteras cargadas (assets o Remote Config), para que la UI
     * de config las liste. Es una copia de solo lectura: la lista interna sigue
     * siendo privada.
     */
    fun catalogo(): List<Billetera> = billeteras.toList()

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

    /**
     * Frase que dirá el parlante.
     *  - sin nombre (por defecto): "¡Pago Ya! Te yapearon 25 soles con 50"
     *  - con nombre (si el comercio lo activó): "…Juan te yapeó 25 soles con 50"
     *
     * Las plantillas viven en Remote Config, así que en el camino sin nombre se
     * borra cualquier `{nombre}` que se haya colado: una plantilla mal escrita
     * en la consola no puede terminar gritando el dato personal de un cliente.
     */
    fun fraseDeVoz(pago: Pago, conNombre: Boolean): String {
        val billetera = billeteras.find { it.id == pago.billeteraId }
        val plantilla = if (conNombre) {
            billetera?.vozPlantilla ?: "¡Pago Ya! {nombre} te pagó {monto}"
        } else {
            billetera?.vozPlantillaSinNombre ?: SIN_NOMBRE_DEFECTO
        }
        return plantilla
            .replace("{nombre}", if (conNombre) pago.pagador else "")
            .replace("{monto}", montoHablado(pago.monto))
            .replace(Regex("\\s{2,}"), " ")
            .trim()
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
