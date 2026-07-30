package pe.pagoya.app.ui.acceso

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.pagoya.app.nube.Sesion
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonPlano
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.CirculoEmoji
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TextoMedio

@Composable
fun PantallaLogin(activity: Activity, alEntrar: () -> Unit) {
    val alcance = rememberCoroutineScope()
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var esRegistro by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(48.dp))
        CirculoEmoji("🔊", tamano = 110.dp)
        Spacer(Modifier.height(20.dp))
        Text(
            "PagoYa",
            style = MaterialTheme.typography.displayMedium,
            color = NaranjaPagoYa,
        )
        Text(
            "Tu caja habla. Tus pagos suenan.",
            style = MaterialTheme.typography.bodyLarge,
            color = AzulNoche,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))

        BotonPagoYa(
            texto = "Entrar con Google",
            cargando = cargando,
            color = AzulNoche,
            alPulsar = {
                cargando = true; error = null
                alcance.launch {
                    Sesion.entrarConGoogle(activity)
                        .onSuccess { alEntrar() }
                        .onFailure { error = "No se pudo entrar con Google. Intenta de nuevo." }
                    cargando = false
                }
            },
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "— o con tu correo —",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoMedio,
        )
        Spacer(Modifier.height(16.dp))

        val coloresCampo = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NaranjaPagoYa,
            unfocusedBorderColor = Borde,
            focusedLabelColor = NaranjaPagoYa,
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = coloresCampo,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = coloresCampo,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        BotonSecundario(
            texto = if (esRegistro) "Crear mi cuenta" else "Entrar con correo",
            habilitado = !cargando,
            alPulsar = {
                if (correo.isBlank() || clave.length < 6) {
                    error = "Pon tu correo y una contraseña de al menos 6 caracteres."
                    return@BotonSecundario
                }
                cargando = true; error = null
                alcance.launch {
                    Sesion.entrarConCorreo(correo, clave, esRegistro)
                        .onSuccess { alEntrar() }
                        .onFailure {
                            error = if (esRegistro) "No se pudo crear la cuenta. ¿Ya existe?"
                            else "Correo o contraseña incorrectos."
                        }
                    cargando = false
                }
            },
        )
        BotonPlano(
            texto = if (esRegistro) "Ya tengo cuenta, quiero entrar"
            else "Soy nuevo, quiero crear mi cuenta",
            alPulsar = { esRegistro = !esRegistro },
        )

        error?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = RojoAlerta,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(40.dp))
    }
}
