package pe.pagoya.app.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import compose.icons.TablerIcons
import compose.icons.tablericons.Cash
import compose.icons.tablericons.Shield
import compose.icons.tablericons.Users
import compose.icons.tablericons.Volume
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Guardian
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.servicio.RedGuardia
import pe.pagoya.app.servicio.VigiaCiego
import pe.pagoya.app.ui.tema.BannerCiego
import pe.pagoya.app.ui.Pestana
import pe.pagoya.app.ui.onboarding.recordarPermisos
import pe.pagoya.app.ui.tema.Aviso
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.DegradadoMarca
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.FilaPago
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TipoAviso
import pe.pagoya.app.ui.tema.esDeHoy
import pe.pagoya.app.ui.tema.soloNumero

/**
 * Inicio: lo primero que ve el comerciante al abrir. Responde tres preguntas
 * en este orden — ¿estoy escuchando?, ¿cuánto llevo hoy?, ¿qué acaba de caer?
 *
 * @param alIrA salta a otra pestaña del armazón (acciones rápidas).
 */
@Composable
fun PantallaInicio(
    alRevisarPermisos: () -> Unit,
    alIrA: (Pestana) -> Unit = {},
    alBlindar: () -> Unit = {},
) {
    val contexto = LocalContext.current
    val comercio by ComercioRepo.comercio.collectAsState()
    val pagos by RegistroPagos.pagos.collectAsState()
    val permisos = recordarPermisos(captura = comercio?.puedeCapturar ?: false)

    var estadoYape by remember { mutableStateOf(Guardian.estadoYape(contexto)) }
    var yapeSeDuerme by remember { mutableStateOf(Guardian.seDuermeSeguido(contexto)) }
    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                estadoYape = Guardian.estadoYape(contexto)
                yapeSeDuerme = Guardian.seDuermeSeguido(contexto)
            }
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    val sinRed by RedGuardia.sinRed.collectAsState()
    val ciego by VigiaCiego.estado.collectAsState()

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
        // ── MODO CIEGO: lo más urgente. Va PRIMERO, por encima de todo, porque
        // mientras esté ahí las ventas pueden no estar sonando en la caja. ──
        if (ciego.ciego) {
            item { BannerCiego(minutos = ciego.minutos) }
        }

        // ── Modo sin red: banner persistente mientras no haya internet ──
        if (sinRed) {
            item {
                Aviso(
                    tipo = TipoAviso.ALERTA,
                    titulo = "Modo sin red — verifica pantalla",
                    texto = "Te quedaste sin internet. Revisa la pantalla del Yape hasta que vuelva la señal.",
                )
            }
        }

        // ── Avisos CRÍTICOS: solo si algo anda mal (Yape apagado o falta un
        // permiso). El estado "todo OK" ya no ocupa un banner: vive dentro del
        // card del monto (abajo), a la derecha. Así el home no se llena de
        // tarjetas cuando todo está bien. ──
        if (estadoYape == Guardian.EstadoBilletera.DETENIDA) {
            item {
                Aviso(
                    tipo = TipoAviso.ALERTA,
                    titulo = "¡Tu Yape está apagado!",
                    texto = "Mientras esté así, tus ventas NO van a sonar. Ábrelo para revivirlo.",
                    textoBoton = "Abrir Yape ahora",
                    alPulsar = {
                        Guardian.abrirYape(contexto)
                        estadoYape = Guardian.estadoYape(contexto)
                    },
                )
            }
        } else if (!permisos.todoListo) {
            item {
                Aviso(
                    tipo = TipoAviso.AVISO,
                    titulo = if (permisos.faltantes.size == 1) "Te falta un paso para estar listo"
                    else "Te faltan ${permisos.faltantes.size} pasos para estar listo",
                    texto = "Sin esto tu caja puede quedarse muda justo cuando te pagan.",
                    textoBoton = "Terminar de activar",
                    alPulsar = alRevisarPermisos,
                )
            }
        }

        // ── Yape que se duerme seguido: revivirla es un parche; la cura es
        // blindarla en el fabricante. Mostramos esto aunque ahora esté encendida. ──
        if (estadoYape != Guardian.EstadoBilletera.DETENIDA && yapeSeDuerme) {
            item {
                Aviso(
                    tipo = TipoAviso.AVISO,
                    titulo = "Tu Yape se está durmiendo seguido",
                    texto = "El teléfono lo apaga para ahorrar batería y ahí no suenan tus " +
                        "ventas. Blíndalo una vez y deja de dormirse.",
                    textoBoton = "Blindaje total",
                    alPulsar = alBlindar,
                )
            }
        }

        // ── El número que le importa: tarjeta hero con el degradado de marca.
        // A la derecha (antes vacía) va la insignia de estado "Escuchando". ──
        item {
            val todoOk = estadoYape != Guardian.EstadoBilletera.DETENIDA && permisos.todoListo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(DegradadoMarca),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            "HOY GANASTE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Blanco.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f),
                        )
                        // Insignia de estado (llena el lado derecho antes vacío).
                        InsigniaEstado(todoOk = todoOk)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "S/ ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Blanco.copy(alpha = 0.85f),
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
                            1 -> "1 pago · Si no suena, no te pagaron"
                            else -> "${deHoy.size} pagos · Si no suena, no te pagaron"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Blanco.copy(alpha = 0.9f),
                    )
                }
            }
        }

        // ── Acciones rápidas: atajos a lo que se usa a diario ──
        item { Etiqueta("Acciones rápidas", Modifier.padding(top = 8.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AccionRapida(
                    icono = TablerIcons.Volume,
                    etiqueta = "Probar voz",
                    modifier = Modifier.weight(1f),
                ) {
                    Anunciador.anunciar(contexto, "¡Pago Ya! Así sonará cada venta, casero.")
                }
                AccionRapida(
                    icono = TablerIcons.Cash,
                    etiqueta = "Caja",
                    modifier = Modifier.weight(1f),
                ) { alIrA(Pestana.CAJA) }
                AccionRapida(
                    icono = TablerIcons.Users,
                    etiqueta = "Equipo",
                    modifier = Modifier.weight(1f),
                ) { alIrA(Pestana.EQUIPO) }
                AccionRapida(
                    icono = TablerIcons.Shield,
                    etiqueta = "Permisos",
                    modifier = Modifier.weight(1f),
                ) { alRevisarPermisos() }
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

/**
 * Insignia de estado que va en el card del monto (lado derecho, antes vacío).
 * Verde "Escuchando" cuando todo está OK; ámbar "Revisar" si falta algo.
 */
@Composable
private fun InsigniaEstado(todoOk: Boolean) {
    Row(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .background(Blanco.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Blanco),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (todoOk) "Escuchando" else "Revisar",
            style = MaterialTheme.typography.labelSmall,
            color = Blanco,
        )
    }
}

/** Atajo de la fila de acciones: ícono de trazo en caja blanca + etiqueta. */
@Composable
private fun AccionRapida(
    icono: ImageVector,
    etiqueta: String,
    modifier: Modifier = Modifier,
    alPulsar: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(Blanco)
                .border(1.dp, Borde, MaterialTheme.shapes.medium)
                .clickable(onClick = alPulsar),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icono,
                contentDescription = etiqueta,
                tint = NaranjaPagoYa,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = TextoMedio,
            textAlign = TextAlign.Center,
        )
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
            Icon(
                TablerIcons.Volume,
                contentDescription = null,
                tint = NaranjaPagoYa,
                modifier = Modifier.size(48.dp),
            )
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
            BotonSecundario(
                "Probar la voz",
                icono = TablerIcons.Volume,
                alPulsar = {
                    Anunciador.anunciar(contexto, "¡Pago Ya! Así sonará cada venta, casero.")
                },
            )
        }
    }
}
