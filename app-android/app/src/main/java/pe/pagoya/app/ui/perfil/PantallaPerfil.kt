package pe.pagoya.app.ui.perfil

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Crown
import compose.icons.tablericons.Logout
import compose.icons.tablericons.Shield
import compose.icons.tablericons.Volume
import pe.pagoya.app.core.Enlaces
import pe.pagoya.app.core.Plan
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.onboarding.recordarPermisos
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.DegradadoMarca
import pe.pagoya.app.ui.tema.DialogoSalirCuenta
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaHondo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue
import pe.pagoya.app.ui.tema.VerdeOk
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.fechaCorta

/**
 * Perfil: la identidad del comercio + su plan. Reúne "quién soy y qué tengo
 * contratado": negocio, rol, plan con su fecha de vencimiento/renovación,
 * permisos y cerrar sesión. Se abre desde el avatar de la barra superior.
 * Textos grandes y claros (lo usa gente mayor). Pantalla completa con volver.
 */
@Composable
fun PantallaPerfil(
    alVolver: () -> Unit,
    alRevisarPermisos: () -> Unit,
    alSalir: () -> Unit,
) {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    val permisos = recordarPermisos(captura = comercio?.puedeCapturar ?: false)
    var confirmarSalida by remember { mutableStateOf(false) }

    val nombre = comercio?.nombre ?: "Mi negocio"
    val esDueno = comercio?.rol == "dueno"
    val planActual = comercio?.plan ?: Plan.GRATIS
    val estado = comercio?.planEstado
    val vence = comercio?.planVigenteHasta ?: 0L

    if (confirmarSalida) {
        DialogoSalirCuenta(
            alConfirmar = {
                confirmarSalida = false
                alSalir()
            },
            alCancelar = { confirmarSalida = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        // Cabecera con flecha de volver.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = alVolver),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    TablerIcons.ArrowLeft,
                    contentDescription = "Volver",
                    tint = AzulNoche,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                "Mi perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoche,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Tarjeta identidad con el degradado de marca ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(DegradadoMarca)
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Blanco),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                nombre.take(1).uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                color = NaranjaPagoYa,
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                nombre,
                                style = MaterialTheme.typography.titleLarge,
                                color = Blanco,
                            )
                            Text(
                                if (esDueno) "Dueño · este teléfono captura"
                                else "Trabajador · modo escucha",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Blanco.copy(alpha = 0.9f),
                            )
                        }
                    }
                }
            }

            // ── Tu plan (con fecha de vencimiento/renovación) ──
            item { Etiqueta("Tu plan") }
            item {
                TarjetaPagoYa(color = NaranjaSuave) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            TablerIcons.Crown,
                            contentDescription = null,
                            tint = NaranjaHondo,
                            modifier = Modifier.size(26.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Plan ${planActual.etiqueta}",
                                style = MaterialTheme.typography.titleLarge,
                                color = AzulNoche,
                            )
                            Text(
                                textoVigencia(planActual, estado, vence),
                                style = MaterialTheme.typography.bodyMedium,
                                color = NaranjaHondo,
                            )
                        }
                    }
                }
            }

            // Lista de planes (vitrina). El actual va resaltado.
            items(Plan.entries) { plan ->
                FilaPlan(plan = plan, esActual = plan == planActual)
            }
            item {
                TarjetaPagoYa(color = Humo) {
                    Text(
                        "¿Necesitas más teléfonos? El cambio de plan lo vemos por " +
                            "WhatsApp — te lo activamos al toque, sin vueltas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMedio,
                    )
                    Spacer(Modifier.height(12.dp))
                    BotonPagoYa(
                        "Subir de plan por WhatsApp",
                        alPulsar = { abrirWhatsappPlanes(contexto, planActual) },
                    )
                }
            }

            // ── Permisos (resumen, atajo a revisarlos) ──
            item { Etiqueta("Que nada te deje mudo", Modifier.padding(top = 8.dp)) }
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = alRevisarPermisos)
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            TablerIcons.Shield,
                            contentDescription = null,
                            tint = AzulNoche,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Permisos de la app",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AzulNoche,
                            )
                            Text(
                                if (permisos.todoListo) "Todo en orden"
                                else "Te faltan ${permisos.faltantes.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (permisos.todoListo) VerdeOk else NaranjaPagoYa,
                            )
                        }
                        if (permisos.todoListo) {
                            Icon(
                                TablerIcons.CircleCheck,
                                contentDescription = null,
                                tint = VerdeOk,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            // ── Cerrar sesión ──
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { confirmarSalida = true })
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            TablerIcons.Logout,
                            contentDescription = null,
                            tint = RojoAlerta,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Cerrar sesión",
                            style = MaterialTheme.typography.bodyLarge,
                            color = RojoAlerta,
                        )
                    }
                }
            }

            item {
                Text(
                    "PagoYa · Tu caja habla. Tus pagos suenan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTenue,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Texto de vigencia del plan:
 *  - Gratis: sin vencimiento.
 *  - Prueba: "Prueba gratis · vence 27 jun 2026".
 *  - Activa: "Se renueva el 27 jun 2026".
 */
private fun textoVigencia(plan: Plan, estado: String?, vence: Long): String {
    if (plan == Plan.GRATIS || vence <= 0L) {
        return "Sin vencimiento · un teléfono con voz"
    }
    val fecha = fechaCorta(vence)
    return when (estado) {
        "prueba" -> "Prueba gratis · vence $fecha"
        "activa" -> "Se renueva el $fecha"
        else -> "Vence $fecha"
    }
}

/**
 * Tarjeta de un plan en la vitrina. El actual va resaltado en naranja con la
 * palomita; los demás quedan informativos. Precios solo informativos.
 */
@Composable
private fun FilaPlan(plan: Plan, esActual: Boolean) {
    TarjetaPagoYa(color = if (esActual) NaranjaSuave else Blanco) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (plan == Plan.GRATIS) TablerIcons.Volume else TablerIcons.Crown,
                        contentDescription = null,
                        tint = if (esActual) NaranjaHondo else NaranjaPagoYa,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        plan.etiqueta,
                        style = MaterialTheme.typography.titleLarge,
                        color = AzulNoche,
                    )
                    if (esActual) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            TablerIcons.CircleCheck,
                            contentDescription = "Tu plan",
                            tint = VerdeOk,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    plan.resumen,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                if (plan.precioMensual == 0.0) "Gratis"
                else "S/ %.2f".format(plan.precioMensual),
                style = MaterialTheme.typography.titleMedium,
                color = if (esActual) NaranjaHondo else AzulNoche,
            )
        }
    }
}

/** Abre WhatsApp de ventas para subir de plan desde el Perfil. */
private fun abrirWhatsappPlanes(contexto: Context, planActual: Plan) {
    val mensaje = "¡Hola! Uso PagoYa (plan ${planActual.etiqueta}) y quiero " +
        "sumar más teléfonos. ¿Cómo subo de plan?"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Enlaces.whatsappVentas(mensaje)))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { contexto.startActivity(intent) }
}
