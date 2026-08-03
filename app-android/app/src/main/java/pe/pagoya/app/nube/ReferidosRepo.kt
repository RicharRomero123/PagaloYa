package pe.pagoya.app.nube

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

/**
 * "Casero trae Casero": el dueño canjea el código de otro comercio que lo
 * refirió y ambos ganan días de plan.
 *
 * Toda la lógica (validar el código, sumar los días, marcar el canje) vive en
 * la Cloud Function callable `canjearReferido` (región us-central1). La app solo
 * la invoca: no crea nada por su cuenta. Los mensajes ya vienen en criollo desde
 * el backend, así que se muestran tal cual.
 */
object ReferidosRepo {

    // La Function está desplegada en us-central1: hay que apuntar a esa región,
    // si no el SDK llama a otra y responde "not-found".
    private val funciones get() = FirebaseFunctions.getInstance("us-central1")

    /**
     * Canjea un código de referido de 6 dígitos.
     *
     * @return Result con el `mensaje` de éxito de la Function (ej. "¡Ganaste 15
     *   días, casero!"). En error, el mensaje del backend ya viene en criollo
     *   ("No puedes referirte a ti mismo, vivo", "Ya usaste un código antes,
     *   casero", "Ese código no existe"…) y se devuelve tal cual.
     */
    suspend fun canjearReferido(codigo: String): Result<String> = runCatching {
        val respuesta = funciones
            .getHttpsCallable("canjearReferido")
            .call(mapOf("codigo" to codigo.trim()))
            .await()

        @Suppress("UNCHECKED_CAST")
        val data = respuesta.data as? Map<String, Any?>
        (data?.get("mensaje") as? String)?.takeIf { it.isNotBlank() }
            ?: "¡Listo, casero! Ya se aplicó tu código."
    }.recoverCatching { error ->
        // Los HttpsError del backend traen su mensaje en criollo: mostrarlo tal
        // cual. Si por alguna razón viene vacío, caemos a un aviso genérico.
        val mensaje = (error as? FirebaseFunctionsException)?.message?.takeIf { it.isNotBlank() }
            ?: error.message?.takeIf { it.isNotBlank() }
            ?: "No se pudo canjear el código. Revisa tu internet e inténtalo de nuevo."
        throw ReferidoError(mensaje)
    }

    /** Error de canje con el mensaje ya listo para pantalla (criollo). */
    class ReferidoError(mensaje: String) : Exception(mensaje)
}
