package pe.pagoya.app.ui.caja

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.FilaPago
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.esDeHoy
import pe.pagoya.app.ui.tema.etiquetaDeDia
import pe.pagoya.app.ui.tema.fechaLarga
import pe.pagoya.app.ui.tema.formatearSoles
import pe.pagoya.app.ui.tema.inicioDelDia
import pe.pagoya.app.ui.tema.soloNumero

/**
 * Caja: el cierre del día y el historial. Reemplaza el cuaderno del mostrador.
 *
 * Se alimenta del registro local (lo que este teléfono capturó o escuchó). El
 * historial completo del comercio en la nube llega con el panel web.
 */
@Composable
fun PantallaCaja() {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    val pagos by RegistroPagos.pagos.collectAsState()

    val deHoy = pagos.filter { esDeHoy(it.timestamp) }
    val totalHoy = deHoy.sumOf { it.monto }
    val porDia = pagos.groupBy { inicioDelDia(it.timestamp) }
        .toSortedMap(compareByDescending<Long> { it })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Tu caja",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoche,
            )
        }

        item {
            TarjetaPagoYa(color = AzulNoche) {
                Text(
                    fechaLarga(System.currentTimeMillis()).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Blanco.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "S/ ",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Blanco.copy(alpha = 0.7f),
                    )
                    Text(
                        soloNumero(totalHoy),
                        style = MaterialTheme.typography.displayLarge,
                        color = Blanco,
                    )
                }
                Text(
                    "${deHoy.size} ${if (deHoy.size == 1) "pago" else "pagos"} hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blanco.copy(alpha = 0.7f),
                )
            }
        }

        // Desglose por billetera: útil para cuadrar contra cada app
        if (deHoy.isNotEmpty()) {
            item {
                TarjetaPagoYa {
                    Etiqueta("Por billetera")
                    deHoy.groupBy { it.billeteraNombre }.entries
                        .sortedByDescending { it.value.sumOf { p -> p.monto } }
                        .forEachIndexed { i, (billetera, suyos) ->
                            if (i > 0) Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "$billetera · ${suyos.size}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AzulNoche,
                                )
                                Text(
                                    formatearSoles(suyos.sumOf { it.monto }),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NaranjaPagoYa,
                                )
                            }
                        }
                }
            }
            item {
                BotonSecundario("📤 Compartir cierre del día", alPulsar = {
                    compartirCierre(contexto, comercio?.nombre ?: "Mi negocio", deHoy)
                })
            }
        }

        if (pagos.isEmpty()) {
            item {
                TarjetaPagoYa {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🧾", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tu caja está en blanco",
                            style = MaterialTheme.typography.titleLarge,
                            color = AzulNoche,
                        )
                        Text(
                            "Apenas caiga el primer yapeo, aparece aquí solito.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoMedio,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        porDia.forEach { (dia, delDia) ->
            item(key = "cabecera-$dia") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Etiqueta(etiquetaDeDia(dia))
                    Text(
                        formatearSoles(delDia.sumOf { it.monto }),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextoMedio,
                    )
                }
            }
            item(key = "lista-$dia") {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    delDia.forEachIndexed { i, pago ->
                        if (i > 0) HorizontalDivider(color = Borde)
                        FilaPago(pago)
                    }
                }
            }
        }
    }
}

/**
 * El cierre del día como texto plano, para mandarlo por WhatsApp — que es como
 * de verdad se pasa la información en un negocio peruano.
 */
private fun compartirCierre(contexto: Context, negocio: String, pagos: List<Pago>) {
    val lineas = pagos.sortedBy { it.timestamp }.joinToString("\n") {
        "• ${it.pagador} — ${formatearSoles(it.monto)} (${it.billeteraNombre})"
    }
    val texto = buildString {
        appendLine("📋 Cierre de caja · $negocio")
        appendLine(fechaLarga(System.currentTimeMillis()))
        appendLine()
        appendLine(lineas)
        appendLine()
        appendLine("TOTAL: ${formatearSoles(pagos.sumOf { it.monto })}")
        appendLine("${pagos.size} ${if (pagos.size == 1) "pago" else "pagos"}")
        appendLine()
        append("Contado por PagoYa 🔊")
    }
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(
        Intent.createChooser(envio, "Compartir cierre")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
