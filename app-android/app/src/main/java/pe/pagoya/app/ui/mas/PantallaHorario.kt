package pe.pagoya.app.ui.mas

import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Bulb
import compose.icons.tablericons.Clock
import pe.pagoya.app.core.voz.PreferenciasVoz
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaHondo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue

/** Minutos del día (0..1439) → "HH:mm" para mostrar en pantalla. */
internal fun minutosAHora(minutos: Int): String =
    "%02d:%02d".format(minutos / 60, minutos % 60)

/**
 * El horario de tu caja. Aquí el comerciante decide en qué franja quiere que
 * PagoYa hable. Fuera de esa franja la voz se calla, pero el pago igual se
 * guarda en el historial y el teléfono sigue recibiendo: no se pierde nada,
 * solo deja de sonar.
 */
@Composable
fun PantallaHorario(alVolver: () -> Unit) {
    val contexto = LocalContext.current
    BackHandler(onBack = alVolver)

    var activo by remember { mutableStateOf(PreferenciasVoz.horarioActivo(contexto)) }
    var inicio by remember { mutableStateOf(PreferenciasVoz.horaInicio(contexto)) }
    var fin by remember { mutableStateOf(PreferenciasVoz.horaFin(contexto)) }

    // Abre el reloj del sistema; devuelve la nueva hora en minutos del día.
    fun elegirHora(actual: Int, alGuardar: (Int) -> Unit) {
        TimePickerDialog(
            contexto,
            { _, hora, minuto ->
                alGuardar(hora * 60 + minuto)
                ComercioRepo.sincronizarEscucha(contexto)
            },
            actual / 60,
            actual % 60,
            true, // formato 24h: más claro para gente mayor
        ).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = alVolver) {
                    Icon(
                        TablerIcons.ArrowLeft,
                        contentDescription = "Volver",
                        tint = AzulNoche,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "El horario de tu caja",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                )
            }
        }

        // Toggle grande: prender/apagar el horario.
        item {
            TarjetaPagoYa {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Anunciar solo en mi horario",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulNoche,
                        )
                        Text(
                            if (activo)
                                "Anuncia de ${minutosAHora(inicio)} a ${minutosAHora(fin)}."
                            else
                                "Ahora mismo anuncia a toda hora.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (activo) NaranjaHondo else TextoMedio,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = activo,
                        onCheckedChange = {
                            activo = it
                            PreferenciasVoz.definirHorarioActivo(contexto, it)
                            ComercioRepo.sincronizarEscucha(contexto)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = NaranjaPagoYa,
                        ),
                    )
                }
            }
        }

        // Selectores de hora: habilitados solo si el horario está activo.
        item {
            TarjetaPagoYa {
                FilaHora(
                    titulo = "Empieza a hablar",
                    hora = minutosAHora(inicio),
                    habilitado = activo,
                    alPulsar = {
                        elegirHora(inicio) {
                            inicio = it
                            PreferenciasVoz.definirHoraInicio(contexto, it)
                        }
                    },
                )
                Spacer(Modifier.height(6.dp))
                FilaHora(
                    titulo = "Deja de hablar",
                    hora = minutosAHora(fin),
                    habilitado = activo,
                    alPulsar = {
                        elegirHora(fin) {
                            fin = it
                            PreferenciasVoz.definirHoraFin(contexto, it)
                        }
                    },
                )
            }
        }

        // Nota criolla: lo que pasa fuera del horario. Anti-fake bien claro.
        item {
            TarjetaPagoYa(color = Humo) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        TablerIcons.Bulb,
                        contentDescription = null,
                        tint = NaranjaPagoYa,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Fuera de tu horario, PagoYa NO habla, pero igual guarda cada " +
                            "pago en el historial y sigue recibiendo. No se te escapa " +
                            "ninguna venta: solo descansa la voz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMedio,
                    )
                }
            }
        }
    }
}

/** Fila clickable que muestra la hora actual y abre el reloj al tocarla. */
@Composable
private fun FilaHora(
    titulo: String,
    hora: String,
    habilitado: Boolean,
    alPulsar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = alPulsar)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            TablerIcons.Clock,
            contentDescription = null,
            tint = if (habilitado) NaranjaPagoYa else TextoTenue,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.bodyLarge,
            color = if (habilitado) AzulNoche else TextoTenue,
            modifier = Modifier.weight(1f),
        )
        Text(
            hora,
            style = MaterialTheme.typography.titleMedium,
            color = if (habilitado) NaranjaHondo else TextoTenue,
        )
    }
}
