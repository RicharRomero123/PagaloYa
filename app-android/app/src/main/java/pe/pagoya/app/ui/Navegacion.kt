package pe.pagoya.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import compose.icons.tablericons.Headphones
import compose.icons.tablericons.Home
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Users
import pe.pagoya.app.R
import pe.pagoya.app.core.BandejaNotificaciones
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.caja.PantallaCaja
import pe.pagoya.app.ui.equipo.PantallaEquipo
import pe.pagoya.app.ui.inicio.PantallaInicio
import pe.pagoya.app.ui.mas.PantallaMas
import pe.pagoya.app.ui.notificaciones.PantallaNotificaciones
import pe.pagoya.app.ui.perfil.PantallaPerfil
import pe.pagoya.app.ui.soporte.PantallaSoporte
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.DegradadoMarca
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
    var verPerfil by rememberSaveable { mutableStateOf(false) }
    var verSoporte by rememberSaveable { mutableStateOf(false) }
    val noLeidas by BandejaNotificaciones.noLeidas.collectAsState()
    val comercio by ComercioRepo.comercio.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            // Blanco de fondo para que las esquinas inferiores redondeadas del
            // contenido (crema) se recorten limpio contra el blanco de la barra
            // de navegación, sin bordes duros.
            containerColor = Blanco,
            topBar = {
                // Barra superior fija con el DEGRADADO naranja de marca que se
                // extiende edge-to-edge hasta cubrir la barra de estado (estilo
                // iPhone): sin corte. Avatar de perfil a la izquierda; a la
                // derecha, soporte (audífonos) y campanita. Igual en todas las
                // pestañas.
                BarraSuperior(
                    inicialNegocio = (comercio?.nombre ?: "?").take(1).uppercase(),
                    nombreNegocio = comercio?.nombre ?: "Mi negocio",
                    noLeidas = noLeidas,
                    alAbrirPerfil = { verPerfil = true },
                    alAbrirAvisos = { verNotificaciones = true },
                    alAbrirSoporte = { verSoporte = true },
                )
            },
            bottomBar = {
                // Barra de navegación con esquinas SUPERIORES redondeadas: se
                // clipa un Box para que el blanco de la barra se recorte contra
                // el crema del contenido de arriba (look suave, sin borde duro).
                NavigationBar(
                    containerColor = Blanco,
                    tonalElevation = 0.dp,
                    modifier = Modifier.clip(
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ),
                ) {
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
                    // Esquinas inferiores redondeadas del contenido: suaviza el
                    // corte con la barra de navegación (estilo tarjeta flotante).
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Crema)
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

        // Perfil a pantalla completa por encima de todo.
        if (verPerfil) {
            PantallaPerfil(
                alVolver = { verPerfil = false },
                alRevisarPermisos = {
                    verPerfil = false
                    alRevisarPermisos()
                },
                alSalir = alSalir,
            )
        }

        // Soporte a pantalla completa por encima de todo.
        if (verSoporte) {
            PantallaSoporte(alVolver = { verSoporte = false })
        }
    }
}

/**
 * Barra superior fija de la app (topBar del Scaffold): fondo con el DEGRADADO
 * naranja de marca (oscuro→claro) que se pinta por debajo de la barra de estado
 * (transparente, edge-to-edge) para fundirse con ella estilo iPhone, sin corte.
 * El windowInsetsPadding baja el contenido para que no lo tape el reloj/hora.
 * Avatar de perfil a la izquierda con el saludo; a la derecha, soporte
 * (audífonos) y campanita de avisos. Igual en todas las pantallas.
 */
@Composable
private fun BarraSuperior(
    inicialNegocio: String,
    nombreNegocio: String,
    noLeidas: Int,
    alAbrirPerfil: () -> Unit,
    alAbrirAvisos: () -> Unit,
    alAbrirSoporte: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DegradadoMarca)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar de perfil (inicial del negocio) → abre el Perfil.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Blanco)
                    .clickable(onClick = alAbrirPerfil),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    inicialNegocio,
                    style = MaterialTheme.typography.titleLarge,
                    color = NaranjaPagoYa,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Hola, casero",
                    style = MaterialTheme.typography.bodySmall,
                    color = Blanco.copy(alpha = 0.85f),
                )
                Text(
                    nombreNegocio,
                    style = MaterialTheme.typography.titleMedium,
                    color = Blanco,
                    maxLines = 1,
                )
            }
            // Soporte (audífonos).
            IconoBarra(
                icono = TablerIcons.Headphones,
                descripcion = "Ayuda y soporte",
                alPulsar = alAbrirSoporte,
            )
            Spacer(Modifier.width(4.dp))
            // Campanita de avisos (con puntito de no leídas).
            CampanitaAvisos(noLeidas = noLeidas, alPulsar = alAbrirAvisos)
        }
    }
}

/** Botón circular translúcido de la barra superior (sobre el degradado). */
@Composable
private fun IconoBarra(
    icono: ImageVector,
    descripcion: String,
    alPulsar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Blanco.copy(alpha = 0.18f))
            .clickable(onClick = alPulsar),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icono,
            contentDescription = descripcion,
            tint = Blanco,
            modifier = Modifier.size(22.dp),
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
                .size(44.dp)
                .clip(CircleShape)
                .background(Blanco.copy(alpha = 0.18f))
                .clickable(onClick = alPulsar),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                TablerIcons.Bell,
                contentDescription = "Avisos",
                tint = Blanco,
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
