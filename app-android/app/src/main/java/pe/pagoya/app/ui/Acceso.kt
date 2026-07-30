package pe.pagoya.app.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.Sesion

private val Naranja = Color(0xFFFF6B1A)
private val AzulNoche = Color(0xFF1A2B4A)
private val Crema = Color(0xFFFFF6EF)

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
            .padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("PagoYa", color = Naranja, fontSize = 42.sp, fontWeight = FontWeight.Black)
        Text("Tu caja habla. Tus pagos suenan.", color = AzulNoche, fontSize = 16.sp)
        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                cargando = true; error = null
                alcance.launch {
                    Sesion.entrarConGoogle(activity)
                        .onSuccess { alEntrar() }
                        .onFailure { error = "No se pudo entrar con Google. Intenta de nuevo." }
                    cargando = false
                }
            },
            enabled = !cargando,
            colors = ButtonDefaults.buttonColors(containerColor = AzulNoche),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Entrar con Google", fontSize = 16.sp) }

        Spacer(Modifier.height(20.dp))
        Text("— o con tu correo —", color = AzulNoche.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = clave, onValueChange = { clave = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                if (correo.isBlank() || clave.length < 6) {
                    error = "Pon tu correo y una contraseña de al menos 6 caracteres."
                    return@OutlinedButton
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
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (esRegistro) "Crear mi cuenta" else "Entrar con correo") }

        TextButton(onClick = { esRegistro = !esRegistro }) {
            Text(
                if (esRegistro) "Ya tengo cuenta, quiero entrar"
                else "Soy nuevo, quiero crear mi cuenta",
                color = Naranja
            )
        }

        if (cargando) CircularProgressIndicator(
            color = Naranja,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFB00020))
        }
    }
}

@Composable
fun PantallaComercio(alListo: () -> Unit) {
    val alcance = rememberCoroutineScope()
    var nombreNegocio by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("¡Ya casi, casero!", color = AzulNoche, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(20.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp)) {
                Text("Soy el DUEÑO del negocio", fontWeight = FontWeight.Bold, color = Naranja)
                Text("Crea tu comercio. Este teléfono (el del Yape) será el que capture los pagos.",
                    fontSize = 13.sp, color = AzulNoche.copy(alpha = 0.7f))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = nombreNegocio, onValueChange = { nombreNegocio = it },
                    label = { Text("Nombre de tu negocio (ej. Bodega Rosita)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (nombreNegocio.isBlank()) { error = "Ponle nombre a tu negocio."; return@Button }
                        cargando = true; error = null
                        alcance.launch {
                            ComercioRepo.crearComercio(nombreNegocio)
                                .onSuccess { alListo() }
                                .onFailure {
                                    error = "No se pudo crear: ${it.message ?: "error desconocido"}"
                                }
                            cargando = false
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = Naranja),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Crear mi comercio") }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp)) {
                Text("TRABAJO en el negocio", fontWeight = FontWeight.Bold, color = AzulNoche)
                Text("Pídele al dueño su código de 6 dígitos y este teléfono también anunciará los pagos.",
                    fontSize = 13.sp, color = AzulNoche.copy(alpha = 0.7f))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = codigo, onValueChange = { codigo = it.filter(Char::isDigit).take(6) },
                    label = { Text("Código del comercio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        if (codigo.length != 6) { error = "El código tiene 6 dígitos."; return@OutlinedButton }
                        cargando = true; error = null
                        alcance.launch {
                            ComercioRepo.unirseConCodigo(codigo)
                                .onSuccess { alListo() }
                                .onFailure { error = it.message ?: "Código no válido." }
                            cargando = false
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Unirme al comercio") }
            }
        }

        if (cargando) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(color = Naranja,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFB00020))
        }
    }
}
