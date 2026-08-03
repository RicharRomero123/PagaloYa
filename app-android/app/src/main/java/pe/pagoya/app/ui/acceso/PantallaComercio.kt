package pe.pagoya.app.ui.acceso

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.BuildingStore
import compose.icons.tablericons.Headphones
import kotlinx.coroutines.launch
import pe.pagoya.app.core.Enlaces
import pe.pagoya.app.core.Plan
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.LimiteDispositivos
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonPlano
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.DialogoSalirCuenta
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TextoMedio

private enum class Papel { NINGUNO, DUENO, TRABAJADOR }

/**
 * Segundo paso del alta: definir el papel de ESTE teléfono.
 *
 * Es una decisión con peso: el teléfono del dueño es el único que captura los
 * pagos, así que se pregunta de frente y sin ambigüedad, una tarjeta grande
 * por opción, en vez de mostrar los dos formularios a la vez.
 *
 * Es el primer paso después de entrar: atrás aquí significa "no era mi
 * cuenta" — cerrar sesión con confirmación y volver al acceso.
 */
@Composable
fun PantallaComercio(alCerrarSesion: () -> Unit, alListo: () -> Unit) {
    val alcance = rememberCoroutineScope()
    var papel by remember { mutableStateOf(Papel.NINGUNO) }
    var nombreNegocio by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmarSalida by remember { mutableStateOf(false) }
    // Cuando el comercio llegó a su tope de teléfonos: se muestra un diálogo
    // con el CTA a WhatsApp (el upgrade se cierra por conversación, no aquí).
    var planLleno by remember { mutableStateOf<Plan?>(null) }
    val contexto = LocalContext.current

    BackHandler { confirmarSalida = true }

    planLleno?.let { plan ->
        DialogoLimiteDispositivos(
            plan = plan,
            alContactar = {
                planLleno = null
                abrirWhatsappUpgrade(contexto, plan)
            },
            alCerrar = { planLleno = null },
        )
    }

    if (confirmarSalida) {
        DialogoSalirCuenta(
            alConfirmar = {
                confirmarSalida = false
                alCerrarSesion()
            },
            alCancelar = { confirmarSalida = false },
        )
    }

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NaranjaPagoYa,
        unfocusedBorderColor = Borde,
        focusedLabelColor = NaranjaPagoYa,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { confirmarSalida = true }) {
                Icon(TablerIcons.ArrowLeft, contentDescription = "Volver", tint = AzulNoche)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "¡Ya casi, casero!",
            style = MaterialTheme.typography.headlineLarge,
            color = AzulNoche,
        )
        Text(
            "¿Qué es este teléfono para tu negocio?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextoMedio,
        )
        Spacer(Modifier.height(24.dp))

        TarjetaPapel(
            icono = TablerIcons.BuildingStore,
            titulo = "Es mi negocio",
            texto = "Este teléfono tiene el Yape del negocio y va a capturar los pagos.",
            elegida = papel == Papel.DUENO,
            alElegir = { papel = Papel.DUENO; error = null },
        )
        Spacer(Modifier.height(12.dp))
        TarjetaPapel(
            icono = TablerIcons.Headphones,
            titulo = "Trabajo aquí",
            texto = "Quiero escuchar y anunciar los pagos que caen al Yape del dueño.",
            elegida = papel == Papel.TRABAJADOR,
            alElegir = { papel = Papel.TRABAJADOR; error = null },
        )

        Spacer(Modifier.height(24.dp))

        when (papel) {
            Papel.NINGUNO -> Unit

            Papel.DUENO -> {
                OutlinedTextField(
                    value = nombreNegocio,
                    onValueChange = { nombreNegocio = it },
                    label = { Text("Nombre de tu negocio") },
                    placeholder = { Text("Bodega Rosita") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = coloresCampo,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                BotonPagoYa(
                    texto = "Crear mi comercio",
                    cargando = cargando,
                    alPulsar = {
                        if (nombreNegocio.isBlank()) {
                            error = "Ponle nombre a tu negocio."
                            return@BotonPagoYa
                        }
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
                )
            }

            Papel.TRABAJADOR -> {
                Text(
                    "Pídele al dueño el código de 6 dígitos que le sale en la pestaña Equipo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.filter(Char::isDigit).take(6) },
                    label = { Text("Código del comercio") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = coloresCampo,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                BotonPagoYa(
                    texto = "Unirme al comercio",
                    cargando = cargando,
                    alPulsar = {
                        if (codigo.length != 6) {
                            error = "El código tiene 6 dígitos."
                            return@BotonPagoYa
                        }
                        cargando = true; error = null
                        alcance.launch {
                            ComercioRepo.unirseConCodigo(codigo)
                                .onSuccess { alListo() }
                                .onFailure {
                                    if (it is LimiteDispositivos) planLleno = it.plan
                                    else error = it.message ?: "Código no válido."
                                }
                            cargando = false
                        }
                    },
                )
            }
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = RojoAlerta,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun TarjetaPapel(
    icono: ImageVector,
    titulo: String,
    texto: String,
    elegida: Boolean,
    alElegir: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alElegir),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (elegida) NaranjaSuave else Blanco
        ),
        border = BorderStroke(if (elegida) 2.dp else 1.dp, if (elegida) NaranjaPagoYa else Borde),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = if (elegida) NaranjaPagoYa else AzulNoche,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulNoche,
                )
                Text(
                    texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }
    }
}

/**
 * El comercio ya llegó a su tope de teléfonos. No hay botón de "pagar" — el
 * upgrade se cierra por WhatsApp (el cobro ocurre fuera de la app).
 */
@Composable
private fun DialogoLimiteDispositivos(
    plan: Plan,
    alContactar: () -> Unit,
    alCerrar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        containerColor = Blanco,
        title = {
            Text(
                "Tu plan está lleno, casero",
                style = MaterialTheme.typography.titleLarge,
                color = AzulNoche,
            )
        },
        text = {
            val siguiente = when (plan) {
                Plan.GRATIS -> "Caserito (hasta 3 teléfonos)"
                Plan.CASERITO -> "Patrón (hasta 10 teléfonos)"
                Plan.PATRON -> "un plan a tu medida"
            }
            Text(
                "El plan ${plan.etiqueta} llega hasta ${plan.maxDispositivos} " +
                    "${if (plan.maxDispositivos == 1) "teléfono" else "teléfonos"}. " +
                    "Para sumar más, pásate al $siguiente. Escríbenos y te lo activamos al toque.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
            )
        },
        confirmButton = {
            TextButton(onClick = alContactar) {
                Text(
                    "Escríbenos por WhatsApp",
                    style = MaterialTheme.typography.labelLarge,
                    color = NaranjaPagoYa,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) {
                Text(
                    "Ahora no",
                    style = MaterialTheme.typography.labelLarge,
                    color = AzulNoche,
                )
            }
        },
    )
}

/** Abre WhatsApp de ventas con el mensaje del plan que se quiere. */
private fun abrirWhatsappUpgrade(contexto: Context, planActual: Plan) {
    val mensaje = "¡Hola! Uso PagoYa (plan ${planActual.etiqueta}) y quiero sumar " +
        "más teléfonos a mi negocio. ¿Cómo hago para subir de plan?"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Enlaces.whatsappVentas(mensaje)))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { contexto.startActivity(intent) }
}