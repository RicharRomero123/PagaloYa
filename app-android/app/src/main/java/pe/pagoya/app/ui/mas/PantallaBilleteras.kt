package pe.pagoya.app.ui.mas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Bulb
import pe.pagoya.app.core.BilleteraParser
import pe.pagoya.app.core.PreferenciasBilleteras
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.BilleteraBadge
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio

/**
 * Billeteras que escucho. El comerciante prende o apaga por cuáles quiere que
 * PagoYa hable en ESTE teléfono. Por defecto están TODAS activas: solo aparecen
 * apagadas las que él mismo bajó (modelo opt-out, ver PreferenciasBilleteras).
 *
 * La decisión es local por equipo: apagar Plin aquí no lo apaga en el teléfono
 * del dueño ni en el de otro trabajador.
 */
@Composable
fun PantallaBilleteras(alVolver: () -> Unit) {
    val contexto = LocalContext.current
    BackHandler(onBack = alVolver)

    // El catálogo no cambia mientras la pantalla está abierta (viene de assets /
    // Remote Config cargado al arrancar): se lee una vez.
    val catalogo = remember { BilleteraParser.catalogo() }
    val deshabilitadas by PreferenciasBilleteras.deshabilitadas.collectAsState()

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
                    "Billeteras que escucho",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                )
            }
        }

        item {
            Text(
                "Apaga las que no uses; por defecto escuchamos todas. Lo que apagues " +
                    "aquí no suena ni entra a tu caja en este teléfono.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
            )
        }

        if (catalogo.isEmpty()) {
            item {
                Text(
                    "Cargando tus billeteras…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }

        items(catalogo, key = { it.id }) { billetera ->
            val activa = billetera.id !in deshabilitadas
            TarjetaPagoYa {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BilleteraBadge(billetera.id, billetera.nombre)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            billetera.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulNoche,
                        )
                        Text(
                            if (activa) "Escuchando esta billetera"
                            else "Apagada: no suena ni se guarda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoMedio,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = activa,
                        onCheckedChange = {
                            PreferenciasBilleteras.alternar(contexto, billetera.id)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = NaranjaPagoYa,
                        ),
                    )
                }
            }
        }

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
                        "Esto es solo para este teléfono. Si apagas una billetera aquí, " +
                            "los demás equipos de tu negocio la siguen escuchando.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMedio,
                    )
                }
            }
        }
    }
}
