package pe.pagoya.app.ui.notificaciones

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Bell
import compose.icons.tablericons.BellOff
import pe.pagoya.app.core.BandejaNotificaciones
import pe.pagoya.app.core.Notificacion
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Buzón de avisos: campañas, promos y novedades que PagoYa manda por push.
 * Al abrirlo, todo queda marcado como leído (apaga el puntito de la campanita).
 */
@Composable
fun PantallaNotificaciones(alVolver: () -> Unit) {
    val contexto = LocalContext.current
    val avisos by BandejaNotificaciones.avisos.collectAsState()

    LaunchedEffect(Unit) { BandejaNotificaciones.marcarTodasLeidas(contexto) }

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
                "Avisos",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoche,
            )
        }

        if (avisos.isEmpty()) {
            VacioAvisos()
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(avisos) { aviso -> FilaAviso(aviso) }
        }
    }
}

@Composable
private fun FilaAviso(aviso: Notificacion) {
    TarjetaPagoYa {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NaranjaSuave),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    TablerIcons.Bell,
                    contentDescription = null,
                    tint = NaranjaPagoYa,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    aviso.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulNoche,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    aviso.cuerpo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    fechaAviso(aviso.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoTenue,
                )
            }
        }
    }
}

@Composable
private fun VacioAvisos() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            TablerIcons.BellOff,
            contentDescription = null,
            tint = NaranjaPagoYa,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Sin avisos por ahora",
            style = MaterialTheme.typography.titleLarge,
            color = AzulNoche,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Aquí caerán las promos y novedades de PagoYa, casero.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoMedio,
            textAlign = TextAlign.Center,
        )
    }
}

private val FORMATO_AVISO = SimpleDateFormat("d 'de' MMM, h:mm a", Locale("es", "PE"))

private fun fechaAviso(timestamp: Long): String =
    FORMATO_AVISO.format(Date(timestamp))
