package pe.pagoya.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
    val comercio by ComercioRepo.comercio.collectAsState()

    LaunchedEffect(Unit) { etapa = etapaInicial(contexto) }

    when (etapa) {
        Etapa.CARGANDO -> PantallaCargando()

        Etapa.LOGIN -> PantallaLogin(activity) { etapa = Etapa.COMERCIO }

        Etapa.COMERCIO -> {
            // Si al entrar ya tenía comercio, saltar directo
            LaunchedEffect(Unit) {
                if (ComercioRepo.cargar() != null) etapa = trasElComercio(contexto)
            }
            PantallaComercio {
                ServicioPrimerPlano.arrancar(activity)
                etapa = trasElComercio(contexto)
            }
        }

        Etapa.BIENVENIDA -> PantallaBienvenida {
            marcarBienvenidaVista(contexto)
            etapa = Etapa.PERMISOS
        }

        Etapa.PERMISOS -> AsistentePermisos(
            captura = comercio?.puedeCapturar ?: false,
        ) {
            ServicioPrimerPlano.arrancar(activity)
            etapa = Etapa.PRINCIPAL
        }

        Etapa.PRINCIPAL -> ShellPagoYa(
            alRevisarPermisos = { etapa = Etapa.PERMISOS },
            alSalir = {
                Sesion.salir()
                etapa = Etapa.LOGIN
            },
        )
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
