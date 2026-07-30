package pe.pagoya.app.nube

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Anti-fake de raíz (build de depuración).
 *
 * Play Integrity no funciona en un APK instalado por USB, así que en debug se
 * usa el proveedor de depuración: al arrancar, imprime en Logcat un token
 * (busca "DebugAppCheckProvider") que hay que registrar UNA vez en la consola
 * de Firebase → App Check → PagoYa → Administrar tokens de depuración.
 *
 * Sin registrarlo, la app de desarrollo dejará de escribir en Firestore en
 * cuanto se active la aplicación forzosa (ver backend/README.md).
 */
object AppCheckPagoYa {
    fun instalar() {
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }
}
