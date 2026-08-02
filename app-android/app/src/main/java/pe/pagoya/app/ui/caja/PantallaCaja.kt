package pe.pagoya.app.ui.caja

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.Headphones
import compose.icons.tablericons.Receipt
import compose.icons.tablericons.Share
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.BilleteraBadge
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.FilaPago
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue
import pe.pagoya.app.ui.tema.VerdeOk
import pe.pagoya.app.ui.tema.VerdeSuave
import pe.pagoya.app.ui.tema.esDeHoy
import pe.pagoya.app.ui.tema.etiquetaDeDia
import pe.pagoya.app.ui.tema.fechaLarga
import pe.pagoya.app.ui.tema.formatearSoles
import pe.pagoya.app.ui.tema.inicioDelDia
import pe.pagoya.app.ui.tema.soloNumero
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Caja: el cierre del día y el historial. Reemplaza el cuaderno del mostrador.
 *
 * Se alimenta del registro local (lo que este teléfono capturó o escuchó). El
 * historial completo del comercio en la nube llega con el panel web.
 */
@Composable
fun PantallaCaja() {
    val comercio by ComercioRepo.comercio.collectAsState()
    // Privacidad de caja (permiso criollo). Por DEFECTO el trabajador ve todo;
    // el dueño puede apagarlo desde Equipo y entonces su gente solo escucha los
    // pagos, sin totales ni métricas. El dueño siempre ve su caja.
    //
    // OJO: es una baranda de UI, NO de servidor. El modo escucha necesita leer
    // los pagos individuales para anunciarlos, así que un APK modificado podría
    // sumarlos igual. Es privacidad entre gente honesta, no un candado.
    val cajaVisible = comercio?.let { it.rol == "dueno" || it.trabajadorVeCaja } ?: true
    if (cajaVisible) CajaConDinero() else CajaPrivada()
}

/** Lo que ve el trabajador cuando el dueño apagó la privacidad de caja. */
@Composable
private fun CajaPrivada() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(16.dp),
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
            TarjetaPagoYa(color = VerdeSuave) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        TablerIcons.Headphones,
                        contentDescription = null,
                        tint = VerdeOk,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Estás en modo escucha",
                        style = MaterialTheme.typography.titleMedium,
                        color = VerdeOk,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tu patrón activó la privacidad de la caja. Tu teléfono canta " +
                        "cada pago apenas cae, pero el total del día y las cuentas " +
                        "las ve solo el dueño.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzulNoche,
                )
            }
        }
    }
}

@Composable
private fun CajaConDinero() {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    val pagos by RegistroPagos.pagos.collectAsState()

    // Filtro del historial por billetera (null = todas), como en el concepto
    var filtro by rememberSaveable { mutableStateOf<String?>(null) }
    val billeteras = pagos.map { it.billeteraId to it.billeteraNombre }.distinct()

    val deHoy = pagos.filter { esDeHoy(it.timestamp) }
    val totalHoy = deHoy.sumOf { it.monto }
    val filtrados = if (filtro == null) pagos else pagos.filter { it.billeteraId == filtro }
    val porDia = filtrados.groupBy { inicioDelDia(it.timestamp) }
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

        // Gráfico estadístico: la semana de un vistazo
        if (pagos.isNotEmpty()) {
            item { GraficoSemana(pagos) }
        }

        // Desglose por billetera: útil para cuadrar contra cada app
        if (deHoy.isNotEmpty()) {
            item {
                TarjetaPagoYa {
                    Etiqueta("Hoy por billetera")
                    deHoy.groupBy { it.billeteraId to it.billeteraNombre }.entries
                        .sortedByDescending { it.value.sumOf { p -> p.monto } }
                        .forEachIndexed { i, (billetera, suyos) ->
                            if (i > 0) Spacer(Modifier.height(14.dp))
                            val (id, nombre) = billetera
                            val monto = suyos.sumOf { it.monto }
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BilleteraBadge(id, nombre)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AzulNoche,
                                    )
                                    Text(
                                        "${suyos.size} ${if (suyos.size == 1) "pago" else "pagos"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoMedio,
                                    )
                                }
                                Text(
                                    formatearSoles(monto),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NaranjaPagoYa,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            // Barrita de proporción sobre el total del día
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Humo),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(
                                            (monto / totalHoy).toFloat().coerceIn(0.04f, 1f)
                                        )
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(NaranjaPagoYa),
                                )
                            }
                        }
                }
            }
            item {
                BotonSecundario(
                    "Compartir cierre del día",
                    icono = TablerIcons.Share,
                    alPulsar = {
                        compartirCierre(contexto, comercio?.nombre ?: "Mi negocio", deHoy)
                    },
                )
            }
        }

        if (pagos.isEmpty()) {
            item {
                TarjetaPagoYa {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            TablerIcons.Receipt,
                            contentDescription = null,
                            tint = NaranjaPagoYa,
                            modifier = Modifier.size(48.dp),
                        )
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

        // Filtro por billetera del historial (solo si hay más de una)
        if (billeteras.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChipFiltro("Todos", filtro == null) { filtro = null }
                    billeteras.forEach { (id, nombre) ->
                        ChipFiltro(nombre, filtro == id) { filtro = id }
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

/** Pastilla de filtro: naranja cuando está elegida, blanca cuando no. */
@Composable
private fun ChipFiltro(texto: String, elegido: Boolean, alPulsar: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (elegido) NaranjaPagoYa else Blanco)
            .border(1.dp, if (elegido) NaranjaPagoYa else Borde, RoundedCornerShape(999.dp))
            .clickable(onClick = alPulsar)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (elegido) Blanco else AzulNoche,
        )
    }
}

/**
 * Barras de los últimos 7 días. Sin librería de gráficos: puro Compose,
 * suficiente para que el comerciante vea de un golpe cómo va su semana.
 */
@Composable
private fun GraficoSemana(pagos: List<Pago>) {
    val unDia = 24 * 60 * 60 * 1000L
    val hoy = inicioDelDia(System.currentTimeMillis())
    val dias = (6 downTo 0).map { hoy - it * unDia }
    val totales = dias.map { d ->
        pagos.filter { inicioDelDia(it.timestamp) == d }.sumOf { it.monto }
    }
    val maximo = (totales.maxOrNull() ?: 0.0).coerceAtLeast(0.01)
    val formatoDia = SimpleDateFormat("EEE", Locale("es", "PE"))

    TarjetaPagoYa {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                TablerIcons.ChartBar,
                contentDescription = null,
                tint = NaranjaPagoYa,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Tu semana",
                style = MaterialTheme.typography.titleMedium,
                color = AzulNoche,
                modifier = Modifier.weight(1f),
            )
            Text(
                formatearSoles(totales.sum()),
                style = MaterialTheme.typography.titleMedium,
                color = NaranjaPagoYa,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            dias.forEachIndexed { i, dia ->
                val fraccion = (totales[i] / maximo).toFloat()
                val esHoy = dia == hoy
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((6 + 84 * fraccion).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (esHoy) NaranjaPagoYa else NaranjaSuave),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatoDia.format(Date(dia))
                            .take(1)
                            .uppercase(Locale("es", "PE")),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esHoy) NaranjaPagoYa else TextoTenue,
                    )
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
