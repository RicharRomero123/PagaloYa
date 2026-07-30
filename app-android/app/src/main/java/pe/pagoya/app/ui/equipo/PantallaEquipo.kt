package pe.pagoya.app.ui.equipo

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.tema.AvatarInicial
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaHondo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.VerdeOk
import pe.pagoya.app.ui.tema.VerdeSuave

/**
 * Equipo: quién escucha los pagos de este negocio.
 *
 * Para el dueño, el corazón es el código de 6 dígitos — se comparte por
 * WhatsApp, que es como se coordina de verdad en un negocio peruano.
 */
@Composable
fun PantallaEquipo() {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    var miembros by remember { mutableStateOf<List<ComercioRepo.Miembro>>(emptyList()) }

    LaunchedEffect(comercio?.id) {
        miembros = runCatching { ComercioRepo.miembros() }.getOrDefault(emptyList())
    }

    val esDueno = comercio?.rol == "dueno"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    "Tu equipo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                )
                Text(
                    "Los teléfonos que anuncian los pagos de ${comercio?.nombre ?: "tu negocio"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }

        if (esDueno && !comercio?.codigo.isNullOrBlank()) {
            item { TarjetaCodigo(comercio!!.nombre, comercio!!.codigo, contexto) }
        } else {
            item {
                TarjetaPagoYa(color = VerdeSuave) {
                    Text(
                        "🎧 Estás en modo escucha",
                        style = MaterialTheme.typography.titleMedium,
                        color = VerdeOk,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Este teléfono anuncia los pagos que caen al Yape del dueño. " +
                            "No necesita capturar nada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzulNoche,
                    )
                }
            }
        }

        if (miembros.isNotEmpty()) {
            item { Etiqueta("Quiénes escuchan", Modifier.padding(top = 8.dp)) }
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    miembros.forEachIndexed { i, m ->
                        if (i > 0) HorizontalDivider(color = Borde)
                        FilaMiembro(m)
                    }
                }
            }
        }

        item {
            TarjetaPagoYa(color = Humo) {
                Text(
                    "💡 Cómo funciona",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulNoche,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "El teléfono con el Yape del negocio es el único que captura. " +
                        "Los demás solo escuchan y anuncian, aunque estés en tu casa. " +
                        "Nadie más que ese teléfono puede registrar un pago.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }
    }
}

@Composable
private fun TarjetaCodigo(negocio: String, codigo: String, contexto: Context) {
    TarjetaPagoYa {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Etiqueta("Código de tu negocio")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                codigo.forEach { digito ->
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaranjaSuave),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            digito.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = NaranjaHondo,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Pásaselo a tu gente. Con este código su teléfono empieza a " +
                    "cantar tus pagos al toque.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            BotonPagoYa("Compartir por WhatsApp", alPulsar = {
                compartirCodigo(contexto, negocio, codigo)
            })
        }
    }
}

@Composable
private fun FilaMiembro(miembro: ComercioRepo.Miembro) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarInicial(miembro.nombre)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                miembro.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = AzulNoche,
            )
            Text(
                if (miembro.rol == "dueno") "Dueño" else "Trabajador",
                style = MaterialTheme.typography.bodySmall,
                color = TextoMedio,
            )
        }
        val (fondo, tinte, etiqueta) = if (miembro.puedeCapturar) {
            Triple(NaranjaSuave, NaranjaHondo, "Captura")
        } else {
            Triple(VerdeSuave, VerdeOk, "Escucha")
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(fondo)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = tinte)
        }
    }
}

private fun compartirCodigo(contexto: Context, negocio: String, codigo: String) {
    val texto = "¡Hola! Instala PagoYa y entra con el código $codigo para escuchar " +
        "los pagos de $negocio en tu teléfono. 🔊"
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(
        Intent.createChooser(envio, "Compartir código")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
