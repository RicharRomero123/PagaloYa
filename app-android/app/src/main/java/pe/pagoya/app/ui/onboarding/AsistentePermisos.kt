package pe.pagoya.app.ui.onboarding

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Bulb
import pe.pagoya.app.core.ProteccionMarca
import pe.pagoya.app.ui.blindaje.PantallaBlindaje
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Cargando
import pe.pagoya.app.ui.tema.CirculoIcono
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.PuntosProgreso
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio

/**
 * Asistente de permisos: UN permiso por pantalla, con dibujo, explicación y el
 * "por qué" en criollo. No avanza hasta que el permiso esté dado de verdad —
 * un comerciante que se salta esto tiene una app que no suena, y culpa a PagoYa.
 *
 * @param captura true si es el teléfono con el Yape del negocio (pide los
 *                cuatro permisos); false si es un teléfono de escucha.
 * @param alVolver retrocede al paso anterior del flujo (bienvenida u hogar,
 *                 según de dónde se llegó). Atrás del sistema hace lo mismo.
 */
@Composable
fun AsistentePermisos(captura: Boolean, alVolver: () -> Unit, alTerminar: () -> Unit) {
    val contexto = LocalContext.current
    val estado = recordarPermisos(captura)
    var reclamo by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = alVolver)

    val pedirAvisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { estado.refrescar() }

    LaunchedEffect(estado.todoListo) {
        if (estado.todoListo) alTerminar()
    }

    val permiso = estado.faltantes.firstOrNull()
    if (permiso == null) {
        Cargando(Modifier.fillMaxSize())
        return
    }

    // El blindaje es su propio mundo (varios candados del sistema): en vez de la
    // ficha genérica de un permiso, mostramos el Blindaje total como paso, con su
    // botón de "continuar" que marca el paso hecho y avanza el asistente.
    if (permiso == Permiso.BLINDAR_YAPE) {
        PantallaBlindaje(
            alVolver = alVolver,
            enOnboarding = true,
            alTerminar = { estado.refrescar() },
        )
        return
    }

    val texto = remember(permiso) { Permisos.texto(contexto, permiso) }
    LaunchedEffect(permiso) { reclamo = null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = alVolver) {
                    Icon(
                        TablerIcons.ArrowLeft,
                        contentDescription = "Volver",
                        tint = AzulNoche,
                    )
                }
                Text(
                    "Activemos tu PagoYa",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoMedio,
                )
            }
            Text(
                "Paso ${estado.concedidos + 1} de ${estado.requeridos.size}",
                style = MaterialTheme.typography.labelSmall,
                color = TextoMedio,
            )
        }
        Spacer(Modifier.height(12.dp))
        PuntosProgreso(
            total = estado.requeridos.size,
            actual = estado.concedidos,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(28.dp))
            CirculoIcono(texto.icono, fondo = NaranjaSuave, tamano = 140.dp)
            Spacer(Modifier.height(28.dp))
            Text(
                texto.titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulNoche,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                texto.explicacion,
                style = MaterialTheme.typography.bodyLarge,
                color = TextoMedio,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))

            if (texto.pasos.isNotEmpty()) {
                TarjetaPagoYa(color = Humo) {
                    texto.pasos.forEachIndexed { i, paso ->
                        if (i > 0) Spacer(Modifier.height(8.dp))
                        Text(
                            paso,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AzulNoche,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            TarjetaPagoYa(color = Humo) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        TablerIcons.Bulb,
                        contentDescription = null,
                        tint = NaranjaPagoYa,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        texto.porQue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMedio,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        reclamo?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = RojoAlerta,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
        }

        BotonPagoYa(
            texto = texto.textoBoton,
            alPulsar = {
                reclamo = null
                Permisos.abrirAjuste(contexto, permiso) {
                    pedirAvisos.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )
        Spacer(Modifier.height(10.dp))
        BotonSecundario(
            texto = if (texto.seConfirmaAMano) "Ya lo hice" else "Ya lo activé",
            alPulsar = {
                // El blindaje por marca no se puede comprobar desde la app:
                // ahí confiamos en el usuario. El resto sí se verifica.
                if (texto.seConfirmaAMano) {
                    ProteccionMarca.marcarProtegido(contexto)
                    estado.refrescar()
                } else {
                    estado.refrescar()
                    reclamo = if (Permisos.concedido(contexto, permiso)) null
                    else "Todavía no lo vemos activo. Dale al botón naranja y búscanos en la lista."
                }
            },
        )
        Spacer(Modifier.height(24.dp))
    }
}