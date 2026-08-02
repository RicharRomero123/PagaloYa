package pe.pagoya.app.ui.tema

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Shield
import pe.pagoya.app.R
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.PreferenciasApariencia
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Piezas de UI compartidas por toda la app. Cualquier pantalla nueva se arma
 * con estas — así el diseño no se desvía con el tiempo.
 *
 * Criterio: botones altos (56 dp) porque se usan con las manos ocupadas y
 * apuradas; nada de texto por debajo de 12 sp.
 */

private val ALTO_BOTON = 56.dp

@Composable
fun BotonPagoYa(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
    icono: ImageVector? = null,
    /** true para logos multicolor (ej. el G de Google) que no deben teñirse. */
    iconoSinTinte: Boolean = false,
) {
    Button(
        onClick = alPulsar,
        enabled = habilitado && !cargando,
        modifier = modifier
            .fillMaxWidth()
            .height(ALTO_BOTON),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Blanco,
            disabledContainerColor = Humo,
            disabledContentColor = TextoTenue,
        ),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = Blanco,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(22.dp),
            )
        } else {
            icono?.let {
                Icon(
                    it,
                    contentDescription = null,
                    tint = if (iconoSinTinte) Color.Unspecified else LocalContentColor.current,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icono: ImageVector? = null,
) {
    OutlinedButton(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier
            .fillMaxWidth()
            .height(ALTO_BOTON),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.5.dp, Borde),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulNoche),
    ) {
        icono?.let {
            Icon(it, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun BotonPlano(texto: String, alPulsar: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = alPulsar, modifier = modifier) {
        Text(
            texto,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun TarjetaPagoYa(
    modifier: Modifier = Modifier,
    color: Color = Blanco,
    relleno: PaddingValues = PaddingValues(16.dp),
    contenido: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(relleno)) { contenido() }
    }
}

/** Etiqueta de sección: "ÚLTIMOS PAGOS", "TU EQUIPO"… */
@Composable
fun Etiqueta(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto.uppercase(Locale("es", "PE")),
        style = MaterialTheme.typography.labelMedium,
        color = TextoTenue,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

enum class TipoAviso { OK, AVISO, ALERTA }

/**
 * Banner de estado. Es el componente más importante de la app: aquí se le dice
 * al comerciante si su caja está hablando o si se quedó muda.
 */
@Composable
fun Aviso(
    tipo: TipoAviso,
    titulo: String,
    texto: String? = null,
    textoBoton: String? = null,
    alPulsar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val (fondo, tinte, icono) = when (tipo) {
        TipoAviso.OK -> Triple(VerdeSuave, VerdeOk, TablerIcons.CircleCheck)
        TipoAviso.AVISO -> Triple(AmbarSuave, AmbarAviso, TablerIcons.Shield)
        TipoAviso.ALERTA -> Triple(RojoSuave, RojoAlerta, TablerIcons.AlertTriangle)
    }
    TarjetaPagoYa(modifier = modifier, color = fondo) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = tinte)
                texto?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = AzulNoche)
                }
            }
        }
        if (textoBoton != null && alPulsar != null) {
            Spacer(Modifier.height(12.dp))
            BotonPagoYa(textoBoton, alPulsar, color = tinte)
        }
    }
}

/** Ilustración barata y cálida: un ícono grande dentro de un círculo de color. */
@Composable
fun CirculoIcono(
    icono: ImageVector,
    modifier: Modifier = Modifier,
    fondo: Color = NaranjaSuave,
    tinte: Color = NaranjaPagoYa,
    tamano: Dp = 120.dp,
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = tinte,
            modifier = Modifier.size(tamano * 0.45f),
        )
    }
}

/**
 * Distintivo circular de la billetera de un pago, con su color y su inicial.
 *
 * Uso descriptivo permitido: identificar de qué billetera llegó la plata,
 * como hace cualquier estado de cuenta. OJO marca: NUNCA incrustar el logo
 * oficial de Yape/Plin en el APK ni usar sus colores en la marca PagoYa —
 * aquí el color solo etiqueta la transacción.
 */
@Composable
fun BilleteraBadge(billeteraId: String, nombre: String, modifier: Modifier = Modifier) {
    val id = billeteraId.lowercase(Locale.ROOT)
    // Ajuste de Apariencia: el comerciante puede apagar el logo. Reactivo, así
    // la lista cambia al instante sin reabrir la app.
    val mostrarIcono by PreferenciasApariencia.mostrarIconoBilletera.collectAsState()
    // Logo real de la billetera (SOLO dentro de la app, en la lista de pagos;
    // ver CLAUDE.md regla 3). Si el ajuste está apagado, cae al círculo de color.
    val logo = if (!mostrarIcono) null else when (id) {
        "yape" -> R.drawable.ic_yape
        "plin" -> R.drawable.ic_plin
        else -> null
    }
    if (logo != null) {
        // Fondo blanco detrás: el PNG de Plin es transparente, así se ve limpio;
        // el de Yape es opaco y cubre el blanco.
        Box(
            modifier = modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Blanco),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(logo),
                contentDescription = nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        return
    }
    // Sin logo (ajuste apagado o billetera desconocida): círculo con la inicial.
    // Conserva los colores de marca para seguir distinguiendo la billetera.
    val fondo = when (id) {
        "yape" -> Color(0xFF742284)
        "plin" -> Color(0xFF00B9AD)
        else -> AzulNoche
    }
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            inicialDe(nombre),
            style = MaterialTheme.typography.titleMedium,
            color = Blanco,
        )
    }
}

/** Avatar con la inicial del pagador. */
@Composable
fun AvatarInicial(nombre: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(NaranjaSuave),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            inicialDe(nombre),
            style = MaterialTheme.typography.titleMedium,
            color = NaranjaHondo,
        )
    }
}

/** Fila de un pago en el historial: badge de la billetera + pagador + monto. */
@Composable
fun FilaPago(pago: Pago, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BilleteraBadge(pago.billeteraId, pago.billeteraNombre)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                pago.pagador,
                style = MaterialTheme.typography.titleMedium,
                color = AzulNoche,
            )
            Text(
                "${pago.billeteraNombre} · ${horaCorta(pago.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextoMedio,
            )
        }
        Text(
            formatearSoles(pago.monto),
            style = MaterialTheme.typography.titleLarge,
            color = NaranjaPagoYa,
        )
    }
}

/** Puntitos de progreso del carrusel y del asistente. */
@Composable
fun PuntosProgreso(total: Int, actual: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val activo = i == actual
            Box(
                Modifier
                    .height(8.dp)
                    .width(if (activo) 22.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (activo) NaranjaPagoYa else Borde)
            )
        }
    }
}

/**
 * Confirmación ligera antes de cerrar sesión — evita salidas por accidente
 * con el gesto de atrás. La cuenta no se borra, solo se cierra la sesión.
 */
@Composable
fun DialogoSalirCuenta(alConfirmar: () -> Unit, alCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = alCancelar,
        containerColor = Blanco,
        title = {
            Text(
                "¿Sales de tu cuenta?",
                style = MaterialTheme.typography.titleLarge,
                color = AzulNoche,
            )
        },
        text = {
            Text(
                "Tu cuenta queda guardada. Entras de nuevo cuando quieras.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
            )
        },
        confirmButton = {
            TextButton(onClick = alConfirmar) {
                Text("Salir", style = MaterialTheme.typography.labelLarge, color = RojoAlerta)
            }
        },
        dismissButton = {
            TextButton(onClick = alCancelar) {
                Text("Quedarme", style = MaterialTheme.typography.labelLarge, color = AzulNoche)
            }
        },
    )
}

@Composable
fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = NaranjaPagoYa)
    }
}

// ── Formato ───────────────────────────────────────────────────────────────

/** 25.5 → "S/ 25.50" */
fun formatearSoles(monto: Double): String = "S/ %,.2f".format(Locale("es", "PE"), monto)

/** 25.5 → "25.50" (para el monto gigante, donde el "S/" va aparte) */
fun soloNumero(monto: Double): String = "%,.2f".format(Locale("es", "PE"), monto)

fun horaCorta(timestamp: Long): String =
    SimpleDateFormat("hh:mm a", Locale("es", "PE")).format(Date(timestamp))

fun fechaLarga(timestamp: Long): String =
    SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "PE")).format(Date(timestamp))
        .replaceFirstChar { it.uppercase(Locale("es", "PE")) }

fun inicialDe(nombre: String): String =
    nombre.trim().firstOrNull()?.uppercase(Locale("es", "PE")) ?: "?"

// ── Días ──────────────────────────────────────────────────────────────────

/** Medianoche del día al que pertenece el timestamp. */
fun inicioDelDia(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun esDeHoy(timestamp: Long): Boolean =
    inicioDelDia(timestamp) == inicioDelDia(System.currentTimeMillis())

/** "Hoy", "Ayer" o "Martes 14 de julio" — como lo diría un casero. */
fun etiquetaDeDia(timestamp: Long): String {
    val dia = inicioDelDia(timestamp)
    val hoy = inicioDelDia(System.currentTimeMillis())
    val unDia = 24 * 60 * 60 * 1000L
    return when (dia) {
        hoy -> "Hoy"
        hoy - unDia -> "Ayer"
        else -> fechaLarga(timestamp)
    }
}
