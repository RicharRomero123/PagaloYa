package pe.pagoya.app.ui.mas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pagoya.app.core.Anunciador
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
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
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

    if (enPantallaVoz) {
        PantallaVoz(alVolver = {
            enPantallaVoz = false
            vozFuerte = Anunciador.vozFuerte(contexto)
        })
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Más", style = MaterialTheme.typography.headlineMedium, color = AzulNoche)
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
                BotonSecundario("🔊 Probar la voz", alPulsar = {
                    Anunciador.anunciar(contexto, "¡Pago Ya! Así sonará cada venta, casero.")
                })
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
        item {
            TarjetaPagoYa(color = Humo) {
                Text(
                    "Plan Gratis",
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulNoche,
                )
                Text(
                    "Un teléfono, anuncio de voz. El plan Caserito (multi-teléfono, " +
                        "cierre de caja) llega pronto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }

        // ── Sesión ──
        item {
            Text(
                "Cerrar sesión",
                style = MaterialTheme.typography.titleMedium,
                color = RojoAlerta,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = alSalir)
                    .padding(vertical = 20.dp),
            )
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
        Text(texto.emoji, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            texto.titulo,
            style = MaterialTheme.typography.bodyLarge,
            color = AzulNoche,
            modifier = Modifier.weight(1f),
        )
        Text(
            if (concedido) "✅" else "Falta",
            style = MaterialTheme.typography.labelSmall,
            color = if (concedido) VerdeOk else NaranjaPagoYa,
        )
    }
}
