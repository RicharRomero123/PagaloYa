package pe.pagoya.app.ui.soporte

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.BrandWhatsapp
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.FileText
import compose.icons.tablericons.Help
import compose.icons.tablericons.Lock
import compose.icons.tablericons.Message
import pe.pagoya.app.core.Enlaces
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue

/**
 * Soporte y ayuda: todo lo de "necesito que me ayuden" en un solo lugar —
 * preguntas frecuentes, escribir por WhatsApp, contacto, términos y privacidad.
 * Se abre desde el ícono de audífonos de la barra superior. Textos grandes y
 * claros porque lo usa gente mayor. Pantalla completa con botón volver.
 */
@Composable
fun PantallaSoporte(alVolver: () -> Unit) {
    val contexto = LocalContext.current

    fun abrir(url: String) {
        runCatching {
            contexto.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = alVolver),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    TablerIcons.ArrowLeft,
                    contentDescription = "Volver",
                    tint = AzulNoche,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                "Ayuda y soporte",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoche,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Escríbenos: el atajo más grande y directo (WhatsApp) ──
            item {
                TarjetaPagoYa(color = NaranjaSuave) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NaranjaPagoYa),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                TablerIcons.BrandWhatsapp,
                                contentDescription = null,
                                tint = Blanco,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "¿Necesitas ayuda? Escríbenos",
                                style = MaterialTheme.typography.titleLarge,
                                color = AzulNoche,
                            )
                            Text(
                                "Te respondemos por WhatsApp, al toque.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoMedio,
                            )
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NaranjaPagoYa)
                        .clickable {
                            abrir(
                                Enlaces.whatsappVentas(
                                    "Hola, necesito ayuda con PagoYa."
                                )
                            )
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Abrir WhatsApp",
                        style = MaterialTheme.typography.labelLarge,
                        color = Blanco,
                    )
                }
            }

            // ── Preguntas y guías ──
            item { Etiqueta("Preguntas y guías", Modifier.padding(top = 8.dp)) }
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    FilaSoporte(
                        icono = TablerIcons.Help,
                        titulo = "Preguntas frecuentes",
                        alPulsar = { abrir(Enlaces.FAQ) },
                    )
                    HorizontalDivider(color = Borde)
                    FilaSoporte(
                        icono = TablerIcons.Message,
                        titulo = "Enviar una consulta",
                        alPulsar = { abrir(Enlaces.CONSULTAS) },
                    )
                }
            }

            // ── Legales ──
            item { Etiqueta("Lo legal", Modifier.padding(top = 8.dp)) }
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    FilaSoporte(
                        icono = TablerIcons.FileText,
                        titulo = "Términos y condiciones",
                        alPulsar = { abrir(Enlaces.TERMINOS) },
                    )
                    HorizontalDivider(color = Borde)
                    FilaSoporte(
                        icono = TablerIcons.Lock,
                        titulo = "Política de privacidad",
                        alPulsar = { abrir(Enlaces.PRIVACIDAD) },
                    )
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

/** Fila de soporte con ícono grande, título legible y chevron. */
@Composable
private fun FilaSoporte(icono: ImageVector, titulo: String, alPulsar: () -> Unit) {
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
            tint = NaranjaPagoYa,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(14.dp))
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
