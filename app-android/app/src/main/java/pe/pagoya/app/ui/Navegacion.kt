package pe.pagoya.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Cash
import compose.icons.tablericons.Home
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Users
import pe.pagoya.app.R
import pe.pagoya.app.core.BandejaNotificaciones
import pe.pagoya.app.ui.caja.PantallaCaja
import pe.pagoya.app.ui.equipo.PantallaEquipo
import pe.pagoya.app.ui.inicio.PantallaInicio
import pe.pagoya.app.ui.mas.PantallaMas
import pe.pagoya.app.ui.notificaciones.PantallaNotificaciones
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TextoTenue

/**
 * Las cuatro secciones de la app ya con sesión y comercio listos.
 * El orden es el de importancia para el comerciante, no el alfabético.
 */
enum class Pestana(val etiqueta: String, val icono: ImageVector) {
    INICIO("Inicio", TablerIcons.Home),
    CAJA("Caja", TablerIcons.Cash),
    EQUIPO("Equipo", TablerIcons.Users),
    MAS("Más", TablerIcons.Settings),
}

/**
 * Armazón de la app: barra inferior fija y una pantalla por pestaña.
 * Toda navegación de nivel superior (login, onboarding) vive fuera, en la Raíz.
 */
@Composable
fun ShellPagoYa(
    alRevisarPermisos: () -> Unit,
    alSalir: () -> Unit,
) {
    var pestana by rememberSaveable { mutableStateOf(Pestana.INICIO) }
    var verNotificaciones by rememberSaveable { mutableStateOf(false) }
    val noLeidas by BandejaNotificaciones.noLeidas.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Crema,
            topBar = {
                // Barra superior fija de la app: marca a la izquierda y campanita
                // de avisos a la derecha, INTEGRADA en el header (ya no flotante
                // encima de las pantallas). Igual en todas las pestañas.
                BarraSuperior(
                    noLeidas = noLeidas,
                    alAbrirAvisos = { verNotificaciones = true },
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Blanco, tonalElevation = 0.dp) {
                    Pestana.entries.forEach { destino ->
                        NavigationBarItem(
                            selected = pestana == destino,
                            onClick = { pestana = destino },
                            icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                            label = {
                                Text(destino.etiqueta, style = MaterialTheme.typography.labelSmall)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NaranjaPagoYa,
                                selectedTextColor = NaranjaPagoYa,
                                indicatorColor = NaranjaSuave,
                                unselectedIconColor = TextoTenue,
                                unselectedTextColor = TextoTenue,
                            ),
                        )
                    }
                }
            },
        ) { relleno ->
            Box(
                Modifier
                    .padding(relleno)
                    .fillMaxSize()
            ) {
                when (pestana) {
                    Pestana.INICIO -> PantallaInicio(
                        alRevisarPermisos = alRevisarPermisos,
                        alIrA = { pestana = it },
                    )
                    Pestana.CAJA -> PantallaCaja()
                    Pestana.EQUIPO -> PantallaEquipo()
                    Pestana.MAS -> PantallaMas(
                        alRevisarPermisos = alRevisarPermisos,
                        alSalir = alSalir,
                    )
                }
            }
        }

        // Buzón a pantalla completa por encima de todo (incluida la barra).
        if (verNotificaciones) {
            PantallaNotificaciones(alVolver = { verNotificaciones = false })
        }
    }
}

/**
 * Barra superior de la app: la marca a la izquierda y la campanita de avisos a
 * la derecha. Va fija como header (topBar del Scaffold), no flotante, así se ve
 * integrada en todas las pantallas.
 */
@Composable
private fun BarraSuperior(
    noLeidas: Int,
    alAbrirAvisos: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Crema)
            .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(R.drawable.wordmark_pagoya),
            contentDescription = "PagoYa",
            modifier = Modifier.height(26.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.weight(1f))
        CampanitaAvisos(
            noLeidas = noLeidas,
            alPulsar = alAbrirAvisos,
        )
    }
}

/** Campanita con el contador de avisos sin leer (puntito rojo). */
@Composable
private fun CampanitaAvisos(
    noLeidas: Int,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(44.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(Blanco)
                .border(1.dp, Borde, CircleShape)
                .clickable(onClick = alPulsar),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                TablerIcons.Bell,
                contentDescription = "Avisos",
                tint = AzulNoche,
                modifier = Modifier.size(22.dp),
            )
        }
        if (noLeidas > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(RojoAlerta),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (noLeidas > 9) "9+" else "$noLeidas",
                    style = MaterialTheme.typography.labelSmall,
                    color = Blanco,
                )
            }
        }
    }
}

/** Pantalla de arranque mientras se resuelve sesión y comercio. */
@Composable
fun PantallaCargando() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Image(
            painterResource(R.drawable.splash_icono),
            contentDescription = "PagoYa",
            modifier = Modifier.height(110.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(20.dp))
        Image(
            painterResource(R.drawable.wordmark_pagoya),
            contentDescription = null,
            modifier = Modifier.height(44.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Tu caja habla. Tus pagos suenan.",
            style = MaterialTheme.typography.bodyMedium,
            color = AzulNoche,
        )
    }
}
