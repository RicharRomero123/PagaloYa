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
import compose.icons.tablericons.Bell
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Help
import compose.icons.tablericons.Logout
import compose.icons.tablericons.Shield
import compose.icons.tablericons.Users
import compose.icons.tablericons.Volume
import compose.icons.tablericons.Wallet
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Enlaces
import pe.pagoya.app.core.PreferenciasApariencia
import pe.pagoya.app.core.ProteccionMarca
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.onboarding.Permiso
import pe.pagoya.app.ui.onboarding.Permisos
import pe.pagoya.app.ui.onboarding.recordarPermisos
import pe.pagoya.app.ui.tema.Aviso
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.BilleteraBadge
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPanico
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.DialogoSalirCuenta
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue
import pe.pagoya.app.ui.tema.TipoAviso
import pe.pagoya.app.ui.tema.VerdeOk

/**
 * Configuración, SEGMENTADA por temas para que el comerciante encuentre rápido
 * (regla: como mucho 3 toques hasta lo que busca). Cada sección es una tarjeta
 * con encabezado claro:
 *   Voz · Billetera · Notificaciones · Equipo · Anti fake · Permisos · Ayuda.
 * Textos grandes y directos (lo usa gente mayor). Cerrar sesión al final.
 *
 * Los PLANES ya NO viven aquí: se movieron al Perfil (identidad + plan juntos).
 */
@Composable
fun PantallaMas(alRevisarPermisos: () -> Unit, alBlindar: () -> Unit = {}, alSalir: () -> Unit) {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    val permisos = recordarPermisos(captura = comercio?.puedeCapturar ?: false)
    var vozFuerte by remember { mutableStateOf(Anunciador.vozFuerte(contexto)) }
    val mostrarIconoBilletera by PreferenciasApariencia.mostrarIconoBilletera.collectAsState()
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
            Column {
                Text(
                    "Configuración",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                )
                Text(
                    "Acomoda PagoYa a tu gusto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }

        // ══════════ VOZ ══════════
        item { EncabezadoSeccion(TablerIcons.Volume, "Voz") }
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
                            "Sube el volumen al máximo al anunciar. En mercado, obligatorio.",
                            style = MaterialTheme.typography.bodyMedium,
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

        // ══════════ BILLETERA ══════════
        item { EncabezadoSeccion(TablerIcons.Wallet, "Billetera") }
        item {
            TarjetaPagoYa {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Mostrar el logo de la billetera",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulNoche,
                        )
                        Text(
                            "En la lista de pagos, muestra el ícono de Yape o Plin. " +
                                "Si lo apagas, verás un círculo con la inicial.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoMedio,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = mostrarIconoBilletera,
                        onCheckedChange = {
                            PreferenciasApariencia.definirMostrarIconoBilletera(contexto, it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = NaranjaPagoYa,
                        ),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Así se ve:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoTenue,
                    )
                    Spacer(Modifier.width(12.dp))
                    BilleteraBadge("yape", "Yape")
                    Spacer(Modifier.width(8.dp))
                    BilleteraBadge("plin", "Plin")
                }
            }
        }

        // ══════════ NOTIFICACIONES ══════════
        item { EncabezadoSeccion(TablerIcons.Bell, "Notificaciones") }
        item {
            TarjetaPagoYa {
                Text(
                    "PagoYa te avisa por notificación cuando cae un pago y cuando " +
                        "hay algo importante de tu caja.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
                Spacer(Modifier.height(12.dp))
                BotonSecundario(
                    "Ajustes de notificaciones del teléfono",
                    icono = TablerIcons.Bell,
                    alPulsar = { abrirAjustesNotificaciones(contexto) },
                )
            }
        }

        // ══════════ EQUIPO ══════════
        item { EncabezadoSeccion(TablerIcons.Users, "Equipo") }
        item {
            TarjetaPagoYa {
                Text(
                    if (comercio?.rol == "dueno")
                        "Suma a tu gente para que escuche cada venta desde su propio teléfono."
                    else
                        "Escuchas los pagos que captura el teléfono del dueño.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }

        // ══════════ ANTI FAKE (botón de pánico) ══════════
        item { EncabezadoSeccion(TablerIcons.Volume, "Anti fake") }
        item {
            Text(
                "Si un cliente te muestra un pantallazo para presionarte, aprieta " +
                    "este botón: suena fuerte que ese pago NO entró.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
            )
        }
        item { BotonPanico() }

        // ══════════ PROTECCIÓN (blindaje total) ══════════
        item { EncabezadoSeccion(TablerIcons.Shield, "Protección") }
        item {
            TarjetaPagoYa {
                Text(
                    "Blindaje total",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulNoche,
                )
                Text(
                    "Pon todos los candados del teléfono para que nunca duerma a Yape " +
                        "ni a PagoYa. Se levantan uno por uno desde los ajustes del sistema.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
                Spacer(Modifier.height(12.dp))
                BotonPagoYa("Blindar mi teléfono", icono = TablerIcons.Shield, alPulsar = alBlindar)
            }
        }

        // ══════════ PERMISOS ══════════
        item { EncabezadoSeccion(TablerIcons.CircleCheck, "Permisos") }
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

        // ══════════ AYUDA Y SESIÓN ══════════
        item { EncabezadoSeccion(TablerIcons.Help, "Ayuda") }
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

/** Encabezado de sección: chip con ícono + nombre grande. Guía al ojo. */
@Composable
private fun EncabezadoSeccion(icono: ImageVector, titulo: String) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NaranjaSuave),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = NaranjaPagoYa,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            color = AzulNoche,
        )
    }
}

/** Fila de ajuste con ícono, título y chevron. */
@Composable
private fun FilaAjuste(icono: ImageVector, titulo: String, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = AzulNoche,
            modifier = Modifier.size(22.dp),
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
            modifier = Modifier.size(20.dp),
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
            modifier = Modifier.size(22.dp),
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
                modifier = Modifier.size(22.dp),
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

/** Abre los ajustes de notificaciones de la app en el sistema. */
private fun abrirAjustesNotificaciones(contexto: android.content.Context) {
    runCatching {
        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, contexto.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }.onFailure {
        runCatching {
            contexto.startActivity(
                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:${contexto.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
