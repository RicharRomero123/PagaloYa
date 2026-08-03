package pe.pagoya.app.ui.perfil

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Crown
import compose.icons.tablericons.Logout
import compose.icons.tablericons.Shield
import compose.icons.tablericons.Users
import pe.pagoya.app.core.Plan
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.onboarding.recordarPermisos
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.DialogoSalirCuenta
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.NaranjaHondo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue
import pe.pagoya.app.ui.tema.VerdeOk

/**
 * Perfil: la identidad del comercio en este teléfono. Reúne lo relacionado con
 * "quién soy": el negocio, el rol, el plan y los accesos a permisos y sesión.
 * Se abre desde el avatar de la barra superior. Pantalla completa con volver.
 */
@Composable
fun PantallaPerfil(
    alVolver: () -> Unit,
    alRevisarPermisos: () -> Unit,
    alSalir: () -> Unit,
) {
    val comercio by ComercioRepo.comercio.collectAsState()
    val permisos = recordarPermisos(captura = comercio?.puedeCapturar ?: false)
    var confirmarSalida by remember { mutableStateOf(false) }

    val nombre = comercio?.nombre ?: "Mi negocio"
    val esDueno = comercio?.rol == "dueno"
    val plan = comercio?.plan ?: Plan.GRATIS

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
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = alVolver),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    TablerIcons.ArrowLeft,
                    contentDescription = "Volver",
                    tint = AzulNoche,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoche,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Tarjeta identidad: avatar + nombre + rol + chip de plan ──
            item {
                TarjetaPagoYa {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(NaranjaPagoYa),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                nombre.take(1).uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                color = Blanco,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            nombre,
                            style = MaterialTheme.typography.titleLarge,
                            color = AzulNoche,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (esDueno) "Dueño · este teléfono captura los pagos"
                            else "Trabajador · modo escucha",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoMedio,
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(NaranjaSuave)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    TablerIcons.Crown,
                                    contentDescription = null,
                                    tint = NaranjaHondo,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Plan ${plan.etiqueta}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NaranjaHondo,
                                )
                            }
                        }
                    }
                }
            }

            // ── Estado de los permisos (resumen) ──
            item { Etiqueta("Que nada te deje mudo") }
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    FilaPerfil(
                        icono = TablerIcons.Shield,
                        titulo = "Permisos de la app",
                        subtitulo = if (permisos.todoListo) "Todo en orden"
                        else "Te faltan ${permisos.faltantes.size}",
                        colorSub = if (permisos.todoListo) VerdeOk else NaranjaPagoYa,
                        alPulsar = alRevisarPermisos,
                    )
                }
            }

            // ── Equipo (contexto del negocio) ──
            item {
                TarjetaPagoYa(color = Crema) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            TablerIcons.Users,
                            contentDescription = null,
                            tint = NaranjaPagoYa,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (esDueno)
                                "Tu equipo escucha los pagos que captura este teléfono."
                            else
                                "Escuchas los pagos que captura el teléfono del dueño.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoMedio,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ── Sesión ──
            item { Etiqueta("Sesión", Modifier.padding(top = 8.dp)) }
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
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
}

/** Fila del perfil con ícono, título, subtítulo y comportamiento de toque. */
@Composable
private fun FilaPerfil(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    colorSub: androidx.compose.ui.graphics.Color,
    alPulsar: () -> Unit,
) {
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
        Column(Modifier.weight(1f)) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyLarge,
                color = AzulNoche,
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = colorSub,
            )
        }
    }
}
