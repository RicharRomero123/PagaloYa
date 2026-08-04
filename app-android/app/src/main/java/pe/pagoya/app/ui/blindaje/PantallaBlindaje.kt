package pe.pagoya.app.ui.blindaje

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Shield
import pe.pagoya.app.core.ProteccionMarca
import pe.pagoya.app.ui.tema.Aviso
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TipoAviso
import pe.pagoya.app.ui.tema.VerdeOk
import pe.pagoya.app.ui.tema.VerdeSuave

/**
 * Blindaje total: el centro de "full protección". Lista todos los candados que
 * evitan que el teléfono mate a Yape o a PagoYa, y cada uno se LEVANTA desde su
 * pantalla real de ajustes del sistema (un botón por candado).
 *
 * Los candados que podemos comprobar (batería y "aparecer encima" de PagoYa)
 * muestran check verde cuando quedan puestos. Los del fabricante y los de la app
 * de Yape se ponen a mano — Android no deja leer esos ajustes de otra app — así
 * que ahí guiamos con los pasos exactos y confiamos en el comerciante.
 *
 * El estado se recalcula cada vez que el usuario vuelve de Ajustes (ON_RESUME).
 */
@Composable
fun PantallaBlindaje(
    alVolver: () -> Unit,
    /** true cuando es un paso del onboarding: añade el botón de "continuar" abajo. */
    enOnboarding: Boolean = false,
    /** Solo en onboarding: avanza al siguiente paso cuando el usuario dice "listo". */
    alTerminar: (() -> Unit)? = null,
) {
    val contexto = LocalContext.current
    BackHandler(onBack = alVolver)

    val escudos = remember { ProteccionMarca.escudos(contexto) }
    // Marca en prefs cuáles de los "a mano" ya tocó el usuario, para no dejarlos
    // eternamente con cara de pendientes.
    var puestos by remember {
        mutableStateOf(escudos.associate { it.id to ProteccionMarca.estaPuesto(contexto, it.id) })
    }
    var verificableCompleto by remember {
        mutableStateOf(ProteccionMarca.blindajeVerificableCompleto(contexto))
    }

    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                puestos = escudos.associate { it.id to ProteccionMarca.estaPuesto(contexto, it.id) }
                verificableCompleto = ProteccionMarca.blindajeVerificableCompleto(contexto)
                if (verificableCompleto) ProteccionMarca.marcarProtegido(contexto)
            }
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .statusBarsPadding(),
    ) {
        // Cabecera con botón de volver.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = alVolver) {
                Icon(TablerIcons.ArrowLeft, contentDescription = "Volver", tint = AzulNoche)
            }
            Text(
                "Blindaje total",
                style = MaterialTheme.typography.titleLarge,
                color = AzulNoche,
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(NaranjaSuave),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            TablerIcons.Shield,
                            contentDescription = null,
                            tint = NaranjaPagoYa,
                            modifier = Modifier.size(52.dp),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Que nada te apague el Yape",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AzulNoche,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Pon estos candados una vez. Con todos puestos, el teléfono " +
                            "ya no duerme a Yape ni a PagoYa y ninguna venta se te escapa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMedio,
                    )
                }
            }

            if (verificableCompleto) {
                item {
                    Aviso(
                        tipo = TipoAviso.OK,
                        titulo = "Lo que podemos revisar, está listo",
                        texto = "Batería y \"aparecer encima\" quedaron puestos. Completa abajo " +
                            "los del fabricante y los de la app de Yape para el blindaje total.",
                    )
                }
            }

            items(escudos.size) { i ->
                val escudo = escudos[i]
                EscudoTarjeta(
                    numero = i + 1,
                    escudo = escudo,
                    puesto = puestos[escudo.id] == true,
                    alPulsar = { ProteccionMarca.abrirEscudo(contexto, escudo.id) },
                )
            }
        }

        // Onboarding: botón fijo abajo para dar por hecho el blindaje y seguir.
        if (enOnboarding) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                BotonPagoYa(
                    texto = "Ya blindé mi teléfono",
                    icono = TablerIcons.Shield,
                    alPulsar = {
                        ProteccionMarca.marcarProtegido(contexto)
                        alTerminar?.invoke()
                    },
                )
            }
        }
    }
}

@Composable
private fun EscudoTarjeta(
    numero: Int,
    escudo: ProteccionMarca.Escudo,
    puesto: Boolean,
    alPulsar: () -> Unit,
) {
    TarjetaPagoYa(color = if (puesto) VerdeSuave else Blanco) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Insignia: número mientras está pendiente; check verde cuando está puesto.
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (puesto) VerdeOk else NaranjaSuave),
                contentAlignment = Alignment.Center,
            ) {
                if (puesto) {
                    Icon(
                        TablerIcons.CircleCheck,
                        contentDescription = "Listo",
                        tint = Blanco,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    Text(
                        "$numero",
                        style = MaterialTheme.typography.titleMedium,
                        color = NaranjaPagoYa,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    escudo.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulNoche,
                )
                if (escudo.verificable && !puesto) {
                    Text(
                        "Lo revisamos por ti",
                        style = MaterialTheme.typography.labelSmall,
                        color = NaranjaPagoYa,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            escudo.descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoMedio,
        )
        if (escudo.pasos.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            escudo.pasos.forEach { paso ->
                Text(
                    paso,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzulNoche,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        BotonSecundario(
            texto = if (puesto) "Revisar de nuevo" else escudo.textoBoton,
            icono = TablerIcons.Shield,
            alPulsar = alPulsar,
        )
    }
}
