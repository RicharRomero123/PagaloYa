package pe.pagoya.app.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Guardian
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.ui.onboarding.recordarPermisos
import pe.pagoya.app.ui.tema.Aviso
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.FilaPago
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue
import pe.pagoya.app.ui.tema.TipoAviso
import pe.pagoya.app.ui.tema.esDeHoy
import pe.pagoya.app.ui.tema.soloNumero

/**
 * Inicio: lo primero que ve el comerciante al abrir. Responde tres preguntas
 * en este orden — ¿estoy escuchando?, ¿cuánto llevo hoy?, ¿qué acaba de caer?
 */
@Composable
fun PantallaInicio(alRevisarPermisos: () -> Unit) {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    val pagos by RegistroPagos.pagos.collectAsState()
    val permisos = recordarPermisos(captura = comercio?.puedeCapturar ?: false)

    var estadoYape by remember { mutableStateOf(Guardian.estadoYape(contexto)) }
    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) estadoYape = Guardian.estadoYape(contexto)
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    val deHoy = pagos.filter { esDeHoy(it.timestamp) }
    val totalHoy = deHoy.sumOf { it.monto }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    comercio?.nombre ?: "Mi negocio",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                )
                Text(
                    if (comercio?.puedeCapturar == true)
                        "Este teléfono captura los pagos"
                    else "Modo escucha · anuncia lo que capture el dueño",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoMedio,
                )
            }
        }

        // ── El estado: lo más importante de la pantalla ──
        item {
            when {
                estadoYape == Guardian.EstadoBilletera.DETENIDA -> Aviso(
                    tipo = TipoAviso.ALERTA,
                    titulo = "¡Tu Yape está apagado!",
                    texto = "Mientras esté así, tus ventas NO van a sonar. Ábrelo para revivirlo.",
                    textoBoton = "Abrir Yape ahora",
                    alPulsar = {
                        Guardian.abrirYape(contexto)
                        estadoYape = Guardian.estadoYape(contexto)
                    },
                )
                !permisos.todoListo -> Aviso(
                    tipo = TipoAviso.AVISO,
                    titulo = if (permisos.faltantes.size == 1) "Te falta un paso para estar listo"
                    else "Te faltan ${permisos.faltantes.size} pasos para estar listo",
                    texto = "Sin esto tu caja puede quedarse muda justo cuando te pagan.",
                    textoBoton = "Terminar de activar",
                    alPulsar = alRevisarPermisos,
                )
                else -> Aviso(
                    tipo = TipoAviso.OK,
                    titulo = "¡Listo, casero! PagoYa está escuchando",
                    texto = "Si no suena, no te pagaron.",
                )
            }
        }

        // ── El número que le importa ──
        item {
            TarjetaPagoYa(color = AzulNoche) {
                Text(
                    "HOY CAYERON",
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
                    when (deHoy.size) {
                        0 -> "Ningún pago todavía"
                        1 -> "1 pago"
                        else -> "${deHoy.size} pagos"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blanco.copy(alpha = 0.7f),
                )
            }
        }

        if (pagos.isEmpty()) {
            item { TarjetaVacia() }
        } else {
            item { Etiqueta("Últimos pagos", Modifier.padding(top = 8.dp)) }
            item {
                TarjetaPagoYa(relleno = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 4.dp
                )) {
                    pagos.take(6).forEachIndexed { i, pago ->
                        if (i > 0) HorizontalDivider(color = Borde)
                        FilaPago(pago)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaVacia() {
    val contexto = LocalContext.current
    TarjetaPagoYa {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🔊", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Todavía no cae nada",
                style = MaterialTheme.typography.titleLarge,
                color = AzulNoche,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pídele a alguien que te yapee 10 céntimos y escucha cómo suena tu caja.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            BotonSecundario("🔊 Probar la voz", alPulsar = {
                Anunciador.anunciar(contexto, "¡Pago Ya! Así sonará cada venta, casero.")
            })
        }
    }
}
