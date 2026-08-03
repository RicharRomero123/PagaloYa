package pe.pagoya.app.nube

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import pe.pagoya.app.R

/**
 * Se lanza cuando el usuario cierra a propósito el selector de cuentas de
 * Google. No es una falla: la pantalla lo trata distinto (sin "reintenta").
 */
class LoginCancelado : Exception()

/** Sesión del usuario: login con Google (recomendado) o correo/contraseña. */
object Sesion {

    val uid: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    fun conectado(): Boolean = FirebaseAuth.getInstance().currentUser != null

    fun nombreUsuario(): String =
        FirebaseAuth.getInstance().currentUser?.displayName
            ?: FirebaseAuth.getInstance().currentUser?.email
            ?: "Casero"

    /**
     * Login con Google vía Credential Manager.
     *
     * Recién instalada la app, Play Services todavía no tiene el proveedor de
     * credenciales "caliente" y el PRIMER intento suele fallar con un error
     * transitorio (el clásico "falla la primera, entra la segunda"). Por eso
     * reintentamos automáticamente hasta 3 veces con una pausa corta, sin que
     * el usuario tenga que tocar "reintentar". Si él cierra el selector a
     * propósito (cancelación), NO reintentamos.
     */
    suspend fun entrarConGoogle(activity: Activity): Result<Unit> = runCatching {
        val gestor = CredentialManager.create(activity)
        val opcionGoogle = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .build()
        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        val maxIntentos = 3
        var ultimoError: Exception? = null
        for (intento in 1..maxIntentos) {
            try {
                val respuesta = gestor.getCredential(activity, solicitud)
                val credencialGoogle =
                    GoogleIdTokenCredential.createFrom(respuesta.credential.data)
                val credencialFirebase =
                    GoogleAuthProvider.getCredential(credencialGoogle.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credencialFirebase).await()
                return@runCatching Unit
            } catch (e: GetCredentialCancellationException) {
                // El usuario cerró el selector: no es falla, no reintentar.
                throw LoginCancelado()
            } catch (e: GetCredentialException) {
                // Transitorio (proveedor frío, sin credencial aún, etc.):
                // esperar un toque y reintentar.
                ultimoError = e
                if (intento < maxIntentos) delay(600L * intento)
            }
        }
        throw ultimoError ?: IllegalStateException("No se pudo entrar con Google")
    }

    suspend fun entrarConCorreo(
        correo: String,
        clave: String,
        esRegistro: Boolean,
    ): Result<Unit> = runCatching {
        val auth = FirebaseAuth.getInstance()
        if (esRegistro) {
            auth.createUserWithEmailAndPassword(correo.trim(), clave).await()
        } else {
            auth.signInWithEmailAndPassword(correo.trim(), clave).await()
        }
        Unit
    }

    fun salir() {
        FirebaseAuth.getInstance().signOut()
        ComercioRepo.limpiar()
    }
}
