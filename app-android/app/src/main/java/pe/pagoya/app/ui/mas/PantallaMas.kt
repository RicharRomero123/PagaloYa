package pe.pagoya.app.ui.mas

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BuildingStore
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Crown
import compose.icons.tablericons.Help
import compose.icons.tablericons.Logout
import compose.icons.tablericons.Volume
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Enlaces
import pe.pagoya.app.core.Plan
import pe.pagoya.app.core.ProteccionMarca
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.onboarding.Permiso
import pe.pagoya.app.ui.onboarding.Permisos
import pe.pagoya.app.ui.onboarding.recordarPermisos
import pe.pagoya.app.ui.tema.Aviso
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Crema
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
import pe.pagoya.app.ui.tema.TipoAviso
import pe.pagoya.app.ui.tema.VerdeOk

/**
 * Más: la voz, el estado de los permisos, el plan y la sesión.
 * Todo lo que se toca una vez y se olvida.
 */
@Composable
fun PantallaMas(alRevisarPermisos: () -> Unit, alSalir: () -> Unit) {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    val permisos = recordarPermisos(captura = comercio?.puedeCapturar ?: false)
    var vozFuerte by remember { mutableStateOf(Anunciador.vozFuerte(contexto)) }
    var enPantallaVoz by remember { mutableStateOf(false) }
    var confirmarSalida by remember { mutableStateOf(false) }

    if (enPantallaVoz) {
        PantallaVoz(alVolver = {
            enPantallaVoz = false
            vozFuerte = Anunciador.vozFuerte(contexto)
        })
        return
    }

    if (confirmarSalida) {
        DialogoSalirCuenta(
            alConfirmar = {
                confirmarSalida = false
                alSalir()
            },
            alCancelar = { confirmarSalida = false },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium, color = AzulNoche)
        }

        // ── Tu negocio: la cabecera del concepto ──
        item {
            TarjetaPagoYa {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NaranjaSuave),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            TablerIcons.BuildingStore,
                            contentDescription = null,
                            tint = NaranjaPagoYa,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            comercio?.nombre ?: "Mi negocio",
                            style = MaterialTheme.typography.titleLarge,
                            color = AzulNoche,
                        )
                        Text(
                            if (comercio?.rol == "dueno") "Dueño · este teléfono captura"
                            else "Trabajador · modo escucha",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoMedio,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(NaranjaSuave)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            "Plan Gratis",
                            style = MaterialTheme.typography.labelSmall,
                            color = NaranjaHondo,
                        )
                    }
                }
            }
        }

        // ── La voz ──
        item { Etiqueta("La voz de tu caja") }
        item {
            TarjetaPagoYa {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Voz fuerte",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulNoche,
                        )
                        Text(
                            "Sube el volumen al máximo mientras anuncia. En mercado, obligatorio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoMedio,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = vozFuerte,
                        onCheckedChange = {
                            vozFuerte = it
                            Anunciador.definirVozFuerte(contexto, it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = NaranjaPagoYa,
                        ),
                    )
                }
                Spacer(Modifier.height(14.dp))
                BotonSecundario(
                    "Probar la voz",
                    icono = TablerIcons.Volume,
                    alPulsar = {
                        Anunciador.anunciar(contexto, "¡Pago Ya! Así sonará cada venta, casero.")
                    },
                )
                Spacer(Modifier.height(10.dp))
                BotonPagoYa("Elegir otra voz", alPulsar = { enPantallaVoz = true })
            }
        }

        // ── Estado del sistema ──
        item { Etiqueta("Que nada te deje mudo", Modifier.padding(top = 8.dp)) }
        item {
            if (permisos.todoListo) {
                Aviso(
                    tipo = TipoAviso.OK,
                    titulo = "Todo en orden",
                    texto = "Los ${permisos.requeridos.size} permisos están activos.",
                )
            } else {
                Aviso(
                    tipo = TipoAviso.AVISO,
                    titulo = "Te faltan ${permisos.faltantes.size}",
                    texto = "Mientras falten, una venta puede no sonar.",
                    textoBoton = "Revisar ahora",
                    alPulsar = alRevisarPermisos,
                )
            }
        }
        item {
            TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                permisos.requeridos.forEachIndexed { i, permiso ->
                    if (i > 0) HorizontalDivider(color = Borde)
                    FilaPermiso(
                        permiso = permiso,
                        concedido = permiso !in permisos.faltantes,
                        alPulsar = {
                            if (permiso == Permiso.BLINDAR_YAPE) {
                                ProteccionMarca.abrirAjustes(contexto)
                            } else {
                                alRevisarPermisos()
                            }
                        },
                    )
                }
            }
        }

        // ── Plan ──
        item { Etiqueta("Tu plan", Modifier.padding(top = 8.dp)) }
        val planActual = comercio?.plan ?: Plan.GRATIS
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
                    "Escríbenos para subir de plan",
                    alPulsar = { abrirWhatsappPlanes(contexto, planActual) },
                )
            }
        }

        // ── Ayuda y sesión ──
        item { Etiqueta("Más", Modifier.padding(top = 8.dp)) }
        item {
            TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                FilaAjuste(
                    icono = TablerIcons.Help,
                    titulo = "Ayuda y soporte",
                    alPulsar = {
                        contexto.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(Enlaces.AYUDA))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                )
                HorizontalDivider(color = Borde)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { confirmarSalida = true })
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        TablerIcons.Logout,
                        contentDescription = null,
                        tint = RojoAlerta,
                        modifier = Modifier.size(20.dp),
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

/**
 * Tarjeta de un plan en la lista de planes. El actual va resaltado en naranja
 * con la palomita; los demás quedan de vitrina. Precios solo informativos —
 * no hay botón de "pagar" (el upgrade se cierra por WhatsApp).
 */
@Composable
private fun FilaPlan(plan: Plan, esActual: Boolean) {
    TarjetaPagoYa(
        color = if (esActual) NaranjaSuave else Blanco,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (plan == Plan.GRATIS) {
                        Icon(
                            TablerIcons.Volume,
                            contentDescription = null,
                            tint = if (esActual) NaranjaHondo else AzulNoche,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            TablerIcons.Crown,
                            contentDescription = null,
                            tint = if (esActual) NaranjaHondo else NaranjaPagoYa,
                            modifier = Modifier.size(20.dp),
                        )
                    }
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

/** Fila de ajuste con ícono, título y chevron — el patrón del concepto. */
@Composable
private fun FilaAjuste(icono: ImageVector, titulo: String, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = AzulNoche,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.bodyLarge,
            color = AzulNoche,
            modifier = Modifier.weight(1f),
        )
        Icon(
            TablerIcons.ChevronRight,
            contentDescription = null,
            tint = TextoTenue,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun FilaPermiso(permiso: Permiso, concedido: Boolean, alPulsar: () -> Unit) {
    val contexto = LocalContext.current
    val texto = remember(permiso) { Permisos.texto(contexto, permiso) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !concedido, onClick = alPulsar)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            texto.icono,
            contentDescription = null,
            tint = AzulNoche,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            texto.titulo,
            style = MaterialTheme.typography.bodyLarge,
            color = AzulNoche,
            modifier = Modifier.weight(1f),
        )
        if (concedido) {
            Icon(
                TablerIcons.CircleCheck,
                contentDescription = "Concedido",
                tint = VerdeOk,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(
                "Falta",
                style = MaterialTheme.typography.labelSmall,
                color = NaranjaPagoYa,
            )
        }
    }
}

/** Abre WhatsApp de ventas para subir de plan desde Ajustes. */
private fun abrirWhatsappPlanes(contexto: android.content.Context, planActual: Plan) {
    val mensaje = "¡Hola! Uso PagoYa (plan ${planActual.etiqueta}) y quiero " +
        "sumar más teléfonos. ¿Cómo subo de plan?"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Enlaces.whatsappVentas(mensaje)))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { contexto.startActivity(intent) }
}
