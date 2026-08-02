package pe.pagoya.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.Sesion
import pe.pagoya.app.servicio.ServicioPrimerPlano
import pe.pagoya.app.ui.PantallaCargando
import pe.pagoya.app.ui.ShellPagoYa
import pe.pagoya.app.ui.acceso.PantallaComercio
import pe.pagoya.app.ui.acceso.PantallaLogin
import pe.pagoya.app.ui.onboarding.AsistentePermisos
import pe.pagoya.app.ui.onboarding.PantallaBienvenida
import pe.pagoya.app.ui.onboarding.Permisos
import pe.pagoya.app.ui.onboarding.bienvenidaVista
import pe.pagoya.app.ui.onboarding.marcarBienvenidaVista
import pe.pagoya.app.ui.tema.TemaPagoYa

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // El splash (Theme.PagoYa.Splash del manifest) solo vive durante el
        // arranque en frío; aquí se vuelve al tema normal antes de Compose.
        setTheme(R.style.Theme_PagoYa)
        super.onCreate(savedInstanceState)
        setContent { TemaPagoYa { RaizPagoYa(this) } }
    }

    override fun onResume() {
        super.onResume()
        if (Sesion.conectado()) ServicioPrimerPlano.arrancar(this)
    }
}

/**
 * Etapas de nivel superior. Es el mapa de flujos de la app en una línea:
 * entrar → decir qué es este teléfono → entender para qué sirve → darle
 * permisos → usarla.
 */
private enum class Etapa { CARGANDO, LOGIN, COMERCIO, BIENVENIDA, PERMISOS, PRINCIPAL }

@Composable
fun RaizPagoYa(activity: MainActivity) {
    val contexto = LocalContext.current
    var etapa by remember { mutableStateOf(Etapa.CARGANDO) }
    // PERMISOS se abre desde el onboarding o desde "Revisar permisos" del
    // hogar: atrás debe regresar a donde se estaba de verdad.
    var permisosDesdePrincipal by remember { mutableStateOf(false) }
    val comercio by ComercioRepo.comercio.collectAsState()

    LaunchedEffect(Unit) { etapa = etapaInicial(contexto) }

    // Misma coreografía que el flujo de acceso: avanzar desliza a la
    // izquierda, retroceder desliza a la derecha, siempre con fundido.
    AnimatedContent(
        targetState = etapa,
        transitionSpec = {
            val adelante = targetState.ordinal > initialState.ordinal
            val resorte = spring<IntOffset>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
            if (adelante) {
                (slideInHorizontally(resorte) { it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally(resorte) { -it / 3 } + fadeOut(tween(160)))
            } else {
                (slideInHorizontally(resorte) { -it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally(resorte) { it / 3 } + fadeOut(tween(160)))
            }
        },
        label = "raiz",
    ) { actual ->
        when (actual) {
            Etapa.CARGANDO -> PantallaCargando()

            Etapa.LOGIN -> PantallaLogin(activity) { etapa = Etapa.COMERCIO }

            Etapa.COMERCIO -> {
                // Si al entrar ya tenía comercio, saltar directo
                LaunchedEffect(Unit) {
                    if (ComercioRepo.cargar() != null) etapa = trasElComercio(contexto)
                }
                PantallaComercio(
                    alCerrarSesion = {
                        Sesion.salir()
                        etapa = Etapa.LOGIN
                    },
                    alListo = {
                        ServicioPrimerPlano.arrancar(activity)
                        etapa = trasElComercio(contexto)
                    },
                )
            }

            Etapa.BIENVENIDA -> PantallaBienvenida(
                alSalirCuenta = {
                    Sesion.salir()
                    etapa = Etapa.LOGIN
                },
                alTerminar = {
                    marcarBienvenidaVista(contexto)
                    permisosDesdePrincipal = false
                    etapa = Etapa.PERMISOS
                },
            )

            Etapa.PERMISOS -> AsistentePermisos(
                captura = comercio?.puedeCapturar ?: false,
                alVolver = {
                    etapa = if (permisosDesdePrincipal) Etapa.PRINCIPAL else Etapa.BIENVENIDA
                },
            ) {
                ServicioPrimerPlano.arrancar(activity)
                etapa = Etapa.PRINCIPAL
            }

            Etapa.PRINCIPAL -> ShellPagoYa(
                alRevisarPermisos = {
                    permisosDesdePrincipal = true
                    etapa = Etapa.PERMISOS
                },
                alSalir = {
                    Sesion.salir()
                    etapa = Etapa.LOGIN
                },
            )
        }
    }
}

private suspend fun etapaInicial(contexto: Context): Etapa = when {
    !Sesion.conectado() -> Etapa.LOGIN
    ComercioRepo.cargar() == null -> Etapa.COMERCIO
    else -> trasElComercio(contexto)
}

/** Con comercio ya resuelto: falta la bienvenida, faltan permisos, o a trabajar. */
private fun trasElComercio(contexto: Context): Etapa {
    val captura = ComercioRepo.comercio.value?.puedeCapturar ?: false
    return when {
        !bienvenidaVista(contexto) -> Etapa.BIENVENIDA
        !Permisos.todoListo(contexto, captura) -> Etapa.PERMISOS
        else -> Etapa.PRINCIPAL
    }
}
