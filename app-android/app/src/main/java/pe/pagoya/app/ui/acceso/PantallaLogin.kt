package pe.pagoya.app.ui.acceso

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import kotlinx.coroutines.launch
import pe.pagoya.app.R
import pe.pagoya.app.nube.LoginCancelado
import pe.pagoya.app.nube.Sesion
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonPlano
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TextoMedio

/**
 * Flujo de acceso en tres pantallas — portada, entrar y crear cuenta — con
 * transiciones de deslizamiento + fundido (estilo iOS). El botón atrás del
 * sistema y la flecha de la UI regresan con la animación inversa.
 */
private enum class PasoAcceso { PORTADA, ENTRAR, CREAR }

@Composable
fun PantallaLogin(activity: Activity, alEntrar: () -> Unit) {
    var paso by rememberSaveable { mutableStateOf(PasoAcceso.PORTADA) }

    BackHandler(enabled = paso != PasoAcceso.PORTADA) { paso = PasoAcceso.PORTADA }

    AnimatedContent(
        targetState = paso,
        transitionSpec = {
            val adelante = targetState.ordinal > initialState.ordinal
            val resorte = spring<IntOffset>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
            if (adelante) {
                (slideInHorizontally(resorte) { it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally(resorte) { -it / 3 } + fadeOut(tween(160)))
            } else {
                (slideInHorizontally(resorte) { -it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally(resorte) { it / 3 } + fadeOut(tween(160)))
            }
        },
        label = "acceso",
    ) { actual ->
        when (actual) {
            PasoAcceso.PORTADA -> Portada(
                activity = activity,
                alEntrar = alEntrar,
                alCorreo = { paso = PasoAcceso.ENTRAR },
                alCrear = { paso = PasoAcceso.CREAR },
            )
            PasoAcceso.ENTRAR -> FormularioCorreo(
                esRegistro = false,
                alVolver = { paso = PasoAcceso.PORTADA },
                alCambiarModo = { paso = PasoAcceso.CREAR },
                alEntrar = alEntrar,
            )
            PasoAcceso.CREAR -> FormularioCorreo(
                esRegistro = true,
                alVolver = { paso = PasoAcceso.PORTADA },
                alCambiarModo = { paso = PasoAcceso.ENTRAR },
                alEntrar = alEntrar,
            )
        }
    }
}

/** Portada: la promesa, Google al toque y las puertas al correo. */
@Composable
private fun Portada(
    activity: Activity,
    alEntrar: () -> Unit,
    alCorreo: () -> Unit,
    alCrear: () -> Unit,
) {
    val alcance = rememberCoroutineScope()
    var cargandoGoogle by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(48.dp))
        Image(
            painterResource(R.drawable.splash_icono),
            contentDescription = "PagoYa",
            modifier = Modifier.height(120.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(20.dp))
        Image(
            painterResource(R.drawable.wordmark_pagoya),
            contentDescription = null,
            modifier = Modifier.height(52.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tu caja habla. Tus pagos suenan.",
            style = MaterialTheme.typography.bodyLarge,
            color = AzulNoche,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))

        BotonPagoYa(
            texto = "Entrar con Google",
            cargando = cargandoGoogle,
            color = AzulNoche,
            icono = ImageVector.vectorResource(R.drawable.ic_google),
            iconoSinTinte = true,
            alPulsar = {
                cargandoGoogle = true; error = null
                alcance.launch {
                    Sesion.entrarConGoogle(activity)
                        .onSuccess { alEntrar() }
                        .onFailure {
                            // Cancelar (cerrar el selector) no muestra error;
                            // el resto sí, pero ya reintentó solo por dentro.
                            error = if (it is LoginCancelado) null
                            else "No se pudo entrar con Google. Intenta de nuevo."
                        }
                    cargandoGoogle = false
                }
            },
        )
        Spacer(Modifier.height(12.dp))
        BotonSecundario("Entrar con mi correo", alPulsar = alCorreo)
        BotonPlano("¿Primera vez? Crea tu cuenta gratis", alPulsar = alCrear)

        error?.let {
            Spacer(Modifier.height(8.dp))
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

/**
 * Entrar y crear cuenta comparten esqueleto pero son pantallas separadas:
 * cada una con su título, sus campos, su botón primario y sus errores en
 * línea debajo del campo (nada de toasts).
 */
@Composable
private fun FormularioCorreo(
    esRegistro: Boolean,
    alVolver: () -> Unit,
    alCambiarModo: () -> Unit,
    alEntrar: () -> Unit,
) {
    val alcance = rememberCoroutineScope()
    val foco = LocalFocusManager.current
    val focoCorreo = remember { FocusRequester() }

    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorClave by remember { mutableStateOf<String?>(null) }
    var errorConfirmar by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    // Autofoco: al llegar, el cursor ya está en el correo y el teclado arriba.
    LaunchedEffect(Unit) { focoCorreo.requestFocus() }

    fun enviar() {
        errorCorreo = null; errorClave = null; errorConfirmar = null; errorGeneral = null
        var hayError = false
        if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
            errorCorreo = "Ese correo no se ve bien. Revísalo."
            hayError = true
        }
        if (clave.length < 6) {
            errorClave = "La contraseña va con 6 caracteres mínimo."
            hayError = true
        }
        if (esRegistro && confirmar != clave) {
            errorConfirmar = "Las contraseñas no coinciden."
            hayError = true
        }
        if (hayError) return

        foco.clearFocus()
        cargando = true
        alcance.launch {
            Sesion.entrarConCorreo(correo, clave, esRegistro)
                .onSuccess { alEntrar() }
                .onFailure {
                    errorGeneral = if (esRegistro) {
                        "No se pudo crear la cuenta. ¿Ya existe? Prueba entrando."
                    } else {
                        "Correo o contraseña incorrectos."
                    }
                }
            cargando = false
        }
    }

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NaranjaPagoYa,
        unfocusedBorderColor = Borde,
        focusedLabelColor = NaranjaPagoYa,
        errorBorderColor = RojoAlerta,
        errorLabelColor = RojoAlerta,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = alVolver) {
                Icon(TablerIcons.ArrowLeft, contentDescription = "Volver", tint = AzulNoche)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (esRegistro) "Crea tu cuenta" else "¡Hola de nuevo, casero!",
            style = MaterialTheme.typography.headlineLarge,
            color = AzulNoche,
        )
        Text(
            if (esRegistro) "Un minuto y tu caja empieza a hablar."
            else "Entra con tu correo y sigue vendiendo.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextoMedio,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; errorCorreo = null; errorGeneral = null },
            label = { Text("Correo") },
            singleLine = true,
            isError = errorCorreo != null,
            supportingText = errorCorreo?.let { { Text(it, color = RojoAlerta) } },
            shape = MaterialTheme.shapes.small,
            colors = coloresCampo,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focoCorreo),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it; errorClave = null; errorGeneral = null },
            label = { Text("Contraseña") },
            singleLine = true,
            isError = errorClave != null,
            supportingText = errorClave?.let { { Text(it, color = RojoAlerta) } },
            shape = MaterialTheme.shapes.small,
            colors = coloresCampo,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (esRegistro) ImeAction.Next else ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { enviar() }),
            modifier = Modifier.fillMaxWidth(),
        )
        if (esRegistro) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = confirmar,
                onValueChange = { confirmar = it; errorConfirmar = null; errorGeneral = null },
                label = { Text("Repite la contraseña") },
                singleLine = true,
                isError = errorConfirmar != null,
                supportingText = errorConfirmar?.let { { Text(it, color = RojoAlerta) } },
                shape = MaterialTheme.shapes.small,
                colors = coloresCampo,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { enviar() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        errorGeneral?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = RojoAlerta,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(20.dp))
        BotonPagoYa(
            texto = if (esRegistro) "Crear mi cuenta" else "Entrar",
            cargando = cargando,
            alPulsar = { enviar() },
        )
        BotonPlano(
            texto = if (esRegistro) "Ya tengo cuenta, quiero entrar"
            else "¿Primera vez? Crea tu cuenta gratis",
            alPulsar = alCambiarModo,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(32.dp))
    }
}