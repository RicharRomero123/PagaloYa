package pe.pagoya.app.nube

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import pe.pagoya.app.core.Guardian
import pe.pagoya.app.core.Salud
import pe.pagoya.app.ui.onboarding.Permisos

/**
 * Latido de telemetría: cada teléfono reporta su salud a
 * comercios/{id}/dispositivos/{uid} para que el panel de operador vea en rojo
 * los teléfonos sordos ANTES de que el cliente reclame. Nadie vigila mil
 * teléfonos a mano: el panel los vigila solo.
 *
 * Freno de 10 min entre envíos: con el vigilante corriendo cada 5-15 min,
 * salen ~100 escrituras/día por teléfono — holgado para la cuota gratis de
 * Firestore (20 000/día) hasta bien pasados los 100 equipos.
 */
object TelemetriaRepo {

    private const val TAG = "PagoYa"
    private const val PREFS = "pagoya_telemetria"
    private const val CLAVE_ULTIMO_ENVIO = "ultimo_envio"
    private const val ENTRE_ENVIOS = 10 * 60 * 1000L

    /**
     * Fire-and-forget: si no hay internet, el próximo latido lo cuenta.
     *
     * El `fcmToken` viaja en el MISMO latido: es lo que la Cloud Function lee
     * para empujar los pagos al modo escucha (reemplaza al listener de
     * Firestore). Como obtener el token es asíncrono, primero lo pedimos y
     * luego escribimos; si falla (sin Play Services, etc.) el latido igual
     * sube, solo que sin token esta vez.
     */
    fun subirLatido(context: Context) {
        val uid = Sesion.uid ?: return
        val comercio = ComercioRepo.comercio.value ?: return

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ahora = System.currentTimeMillis()
        if (ahora - prefs.getLong(CLAVE_ULTIMO_ENVIO, 0L) < ENTRE_ENVIOS) return
        prefs.edit().putLong(CLAVE_ULTIMO_ENVIO, ahora).apply()

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> escribir(context, uid, comercio.id, token) }
            .addOnFailureListener {
                Log.w(TAG, "Token FCM no disponible para el latido: ${it.message}")
                escribir(context, uid, comercio.id, token = null)
            }
    }

    /**
     * Google rotó el token FCM: lo re-subimos al toque (sin esperar al freno de
     * 10 min) para que la Function no siga empujando a un token muerto. Escribe
     * el latido completo con el token nuevo.
     */
    fun reenviarTokenFcm(context: Context, token: String) {
        val uid = Sesion.uid ?: return
        val comercio = ComercioRepo.comercio.value ?: return
        // El latido con el token nuevo cuenta como envío: reinicia el freno.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(CLAVE_ULTIMO_ENVIO, System.currentTimeMillis()).apply()
        escribir(context, uid, comercio.id, token)
    }

    /**
     * Latido de presencia RÁPIDO del Modo ciego (independiente del latido de
     * salud de 10 min). Escribe SOLO el campo `ultimaPresencia` con merge, para
     * no chocar con la regla `hasOnly` del latido completo: es una escritura de
     * un solo campo, mucho más liviana. La cadencia (cada `ciego_cadencia_seg`),
     * el horario de negocio y el "solo si capturo" los decide quien llama
     * (VigiaCiego); aquí solo se hace la escritura fire-and-forget.
     *
     * Con este latido, TODOS los teléfonos del comercio pueden ver hace cuánto
     * el capturador dio señales de vida y gritar CIEGO si se pasa del umbral.
     */
    fun escribirPresencia(context: Context) {
        val uid = Sesion.uid ?: return
        val comercio = ComercioRepo.comercio.value ?: return
        FirebaseFirestore.getInstance()
            .collection("comercios").document(comercio.id)
            .collection("dispositivos").document(uid)
            .set(mapOf("ultimaPresencia" to FieldValue.serverTimestamp()), SetOptions.merge())
            .addOnFailureListener {
                Log.w(TAG, "Presencia no subida: ${it.message}")
            }
    }

    private fun escribir(context: Context, uid: String, comercioId: String, token: String?) {
        val comercio = ComercioRepo.comercio.value ?: return
        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "?"

        // hasOnly estricto en firestore.rules (latidoValido): mandamos fcmToken
        // solo cuando lo tenemos y NO agregamos otros campos nuevos.
        val datos = buildMap<String, Any> {
            put("nombre", Sesion.nombreUsuario().take(60))
            put("capturando", comercio.puedeCapturar)
            put("listenerConectado", Salud.listenerConectado(context))
            put("permisoEscucha", Permisos.escuchaNotificaciones(context))
            put("ultimaNotifBilletera", Salud.ultimaNotificacionBilletera(context))
            put("yape", Guardian.estadoYape(context).name.lowercase())
            put("puedeRevivir", Guardian.puedeRevivir(context))
            put("bateriaLibre", Permisos.bateriaLibre(context))
            put("marca", Build.MANUFACTURER.take(40))
            put("modelo", Build.MODEL.take(60))
            put("versionApp", version.take(20))
            put("ultimoLatido", FieldValue.serverTimestamp())
            if (!token.isNullOrBlank()) put("fcmToken", token)
        }

        FirebaseFirestore.getInstance()
            .collection("comercios").document(comercioId)
            .collection("dispositivos").document(uid)
            .set(datos)
            .addOnFailureListener {
                Log.w(TAG, "Latido no subido: ${it.message}")
            }
    }
}
