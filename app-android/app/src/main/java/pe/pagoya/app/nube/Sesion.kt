package pe.pagoya.app.nube

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import pe.pagoya.app.R

/** Sesión del usuario: login con Google (recomendado) o correo/contraseña. */
object Sesion {

    val uid: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    fun conectado(): Boolean = FirebaseAuth.getInstance().currentUser != null

    fun nombreUsuario(): String =
        FirebaseAuth.getInstance().currentUser?.displayName
            ?: FirebaseAuth.getInstance().currentUser?.email
            ?: "Casero"

    suspend fun entrarConGoogle(activity: Activity): Result<Unit> = runCatching {
        val gestor = CredentialManager.create(activity)
        val opcionGoogle = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .build()
        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()
        val respuesta = gestor.getCredential(activity, solicitud)
        val credencialGoogle = GoogleIdTokenCredential.createFrom(respuesta.credential.data)
        val credencialFirebase =
            GoogleAuthProvider.getCredential(credencialGoogle.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credencialFirebase).await()
        Unit
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
