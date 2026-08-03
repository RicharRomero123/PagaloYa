package pe.pagoya.app.ui.onboarding

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Receipt
import compose.icons.tablericons.Users
import kotlinx.coroutines.launch
import pe.pagoya.app.R
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonPlano
import pe.pagoya.app.ui.tema.CirculoIcono
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.DialogoSalirCuenta
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.PuntosProgreso
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.VerdeOk
import pe.pagoya.app.ui.tema.VerdeSuave

/**
 * Carrusel de bienvenida: tres pantallas que venden la promesa antes de pedir
 * un solo permiso. El orden importa — primero el anti-fake, que es el gancho.
 * La primera lámina lleva a la mascota; las demás, un ícono de trazo.
 */
private data class Lamina(
    val titulo: String,
    val texto: String,
    val fondo: androidx.compose.ui.graphics.Color,
    val tinte: androidx.compose.ui.graphics.Color,
    val icono: ImageVector? = null,
    val imagen: Int? = null,
)

private val LAMINAS = listOf(
    Lamina(
        imagen = R.drawable.mascota_pagoya,
        titulo = "Si no suena,\nno te pagaron",
        texto = "PagoYa canta cada yapeo en voz alta apenas cae. " +
            "Se acabó la captura falsa en tu mostrador.",
        fondo = NaranjaSuave,
        tinte = NaranjaPagoYa,
    ),
    Lamina(
        icono = TablerIcons.Users,
        titulo = "Tú tranquilo,\ntu gente escucha",
        texto = "Aunque no estés en el local, el teléfono de tu trabajador " +
            "anuncia cada pago que entra a tu Yape.",
        fondo = VerdeSuave,
        tinte = VerdeOk,
    ),
    Lamina(
        icono = TablerIcons.Receipt,
        titulo = "Tu caja\ncuadra sola",
        texto = "Total del día, historial y cierre de caja sin anotar " +
            "nada en el cuaderno.",
        fondo = NaranjaSuave,
        tinte = NaranjaPagoYa,
    ),
)

private const val PREFS = "pagoya_config"
private const val CLAVE_BIENVENIDA = "bienvenida_vista"

fun bienvenidaVista(contexto: Context): Boolean =
    contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(CLAVE_BIENVENIDA, false)

fun marcarBienvenidaVista(contexto: Context) {
    contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putBoolean(CLAVE_BIENVENIDA, true).apply()
}

/**
 * @param alSalirCuenta atrás en la primera lámina: cerrar sesión (con
 *        confirmación). Volver a la pantalla de comercio no tiene sentido
 *        porque el comercio ya quedó creado o vinculado; en las demás láminas,
 *        atrás retrocede una lámina con la animación del pager.
 */
@Composable
fun PantallaBienvenida(alSalirCuenta: () -> Unit, alTerminar: () -> Unit) {
    val estadoPager = rememberPagerState(pageCount = { LAMINAS.size })
    val alcance = rememberCoroutineScope()
    val esUltima = estadoPager.currentPage == LAMINAS.lastIndex
    var confirmarSalida by remember { mutableStateOf(false) }

    val atras = {
        if (estadoPager.currentPage > 0) {
            alcance.launch {
                estadoPager.animateScrollToPage(estadoPager.currentPage - 1)
            }
        } else {
            confirmarSalida = true
        }
    }
    BackHandler { atras() }

    if (confirmarSalida) {
        DialogoSalirCuenta(
            alConfirmar = {
                confirmarSalida = false
                alSalirCuenta()
            },
            alCancelar = { confirmarSalida = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { atras() }) {
                Icon(
                    TablerIcons.ArrowLeft,
                    contentDescription = "Volver",
                    tint = AzulNoche,
                )
            }
            if (!esUltima) BotonPlano("Saltar", alTerminar)
        }

        HorizontalPager(
            state = estadoPager,
            modifier = Modifier.weight(1f),
        ) { pagina ->
            val lamina = LAMINAS[pagina]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (lamina.imagen != null) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(lamina.fondo),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painterResource(lamina.imagen),
                            contentDescription = null,
                            modifier = Modifier.size(150.dp),
                        )
                    }
                } else if (lamina.icono != null) {
                    CirculoIcono(
                        lamina.icono,
                        fondo = lamina.fondo,
                        tinte = lamina.tinte,
                        tamano = 160.dp,
                    )
                }
                Spacer(Modifier.height(36.dp))
                Text(
                    lamina.titulo,
                    style = MaterialTheme.typography.headlineLarge,
                    color = AzulNoche,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    lamina.texto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoMedio,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PuntosProgreso(total = LAMINAS.size, actual = estadoPager.currentPage)
            Spacer(Modifier.height(24.dp))
            BotonPagoYa(
                texto = if (esUltima) "¡Empecemos!" else "Siguiente",
                alPulsar = {
                    if (esUltima) {
                        alTerminar()
                    } else {
                        alcance.launch {
                            estadoPager.animateScrollToPage(estadoPager.currentPage + 1)
                        }
                    }
                },
            )
        }
        Box(Modifier.height(8.dp))
    }
}
