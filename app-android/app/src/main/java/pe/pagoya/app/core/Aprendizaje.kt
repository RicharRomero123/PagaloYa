package pe.pagoya.app.core

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Modo aprendizaje: notificaciones de billeteras vigiladas que NINGÚN patrón
 * reconoció. Se guardan localmente para afinar los regex; cuando entre el
 * backend, se subirán (con consentimiento) para actualizar Remote Config.
 */
object Aprendizaje {

    private const val ARCHIVO = "aprendizaje.jsonl"
    private const val MAX_BYTES = 256 * 1024L

    fun registrar(context: Context, paquete: String, textoCompleto: String) {
        runCatching {
            val archivo = File(context.filesDir, ARCHIVO)
            if (archivo.exists() && archivo.length() > MAX_BYTES) {
                archivo.delete() // registro simple: si crece mucho, se reinicia
            }
            val linea = JSONObject()
                .put("paquete", paquete)
                .put("texto", textoCompleto.take(300))
                .put("ts", System.currentTimeMillis())
                .toString()
            archivo.appendText(linea + "\n")
        }
    }

    fun leerTodo(context: Context): String {
        val archivo = File(context.filesDir, ARCHIVO)
        return if (archivo.exists()) archivo.readText() else ""
    }
}
