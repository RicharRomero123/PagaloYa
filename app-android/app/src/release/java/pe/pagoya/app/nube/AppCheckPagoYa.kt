package pe.pagoya.app.nube

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Anti-fake de raíz (build de producción).
 *
 * Play Integrity certifica ante Firebase que quien está escribiendo es el APK
 * real de PagoYa, instalado desde Play, en un Android sin manipular. Sin esto,
 * cualquiera podría descompilar la app, quitarle el parser y escribir pagos
 * inventados directo en Firestore — justo lo que el producto promete impedir.
 *
 * Ojo: solo protege de verdad cuando la *aplicación forzosa* está activada en
 * la consola de Firebase (ver backend/README.md).
 */
object AppCheckPagoYa {
    fun instalar() {
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
    }
}
