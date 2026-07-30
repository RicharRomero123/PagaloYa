package pe.pagoya.app.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import pe.pagoya.app.core.Guardian
import pe.pagoya.app.core.ProteccionMarca

/**
 * Todo lo que PagoYa necesita del sistema para que una venta suene, en un solo
 * sitio: qué falta, por qué le importa al comerciante, y a qué pantalla de
 * Ajustes hay que mandarlo.
 *
 * Los permisos dependen del ROL del teléfono:
 *  - captura (el del Yape del negocio): necesita los cuatro
 *  - escucha (el del trabajador): solo los que mantienen la app despierta,
 *    porque sus pagos llegan de la nube, no de una notificación local
 */
enum class Permiso {
    ESCUCHAR_NOTIFICACIONES,
    AVISOS_PAGOYA,
    BATERIA,
    BLINDAR_YAPE,
}

data class TextoPermiso(
    val emoji: String,
    val titulo: String,
    val explicacion: String,
    val porQue: String,
    val textoBoton: String,
    /** Pasos extra, solo los usa el blindaje por marca. */
    val pasos: List<String> = emptyList(),
    /** true cuando el sistema no nos deja comprobarlo solos. */
    val seConfirmaAMano: Boolean = false,
)

object Permisos {

    fun escuchaNotificaciones(contexto: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(contexto)
            .contains(contexto.packageName)

    fun avisosPagoYa(contexto: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            contexto.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun bateriaLibre(contexto: Context): Boolean =
        (contexto.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(contexto.packageName)

    fun yapeBlindado(contexto: Context): Boolean = ProteccionMarca.yaProtegido(contexto)

    fun concedido(contexto: Context, permiso: Permiso): Boolean = when (permiso) {
        Permiso.ESCUCHAR_NOTIFICACIONES -> escuchaNotificaciones(contexto)
        Permiso.AVISOS_PAGOYA -> avisosPagoYa(contexto)
        Permiso.BATERIA -> bateriaLibre(contexto)
        Permiso.BLINDAR_YAPE -> yapeBlindado(contexto)
    }

    /** Los que necesita este teléfono según su rol, en el orden en que se piden. */
    fun requeridos(contexto: Context, captura: Boolean): List<Permiso> = buildList {
        if (captura) add(Permiso.ESCUCHAR_NOTIFICACIONES)
        if (Build.VERSION.SDK_INT >= 33) add(Permiso.AVISOS_PAGOYA)
        add(Permiso.BATERIA)
        // Blindar Yape solo tiene sentido donde está instalado
        if (captura && Guardian.estadoYape(contexto) != Guardian.EstadoBilletera.NO_INSTALADA) {
            add(Permiso.BLINDAR_YAPE)
        }
    }

    fun faltantes(contexto: Context, captura: Boolean): List<Permiso> =
        requeridos(contexto, captura).filterNot { concedido(contexto, it) }

    fun todoListo(contexto: Context, captura: Boolean): Boolean =
        faltantes(contexto, captura).isEmpty()

    fun texto(contexto: Context, permiso: Permiso): TextoPermiso = when (permiso) {
        Permiso.ESCUCHAR_NOTIFICACIONES -> TextoPermiso(
            emoji = "🔔",
            titulo = "Déjanos escuchar tus yapeos",
            explicacion = "Solo leemos las notificaciones de tus billeteras. " +
                "Ni tus chats, ni tus fotos, ni nada más. Palabra de casero.",
            porQue = "Es el permiso que hace todo: sin él, PagoYa no se entera de ninguna venta.",
            textoBoton = "Activar ahora",
        )
        Permiso.AVISOS_PAGOYA -> TextoPermiso(
            emoji = "📣",
            titulo = "Deja que PagoYa te avise",
            explicacion = "Es el avisito fijo que te muestra que estamos encendidos " +
                "y escuchando tus pagos.",
            porQue = "Sin esto no podemos quedarnos despiertos mientras usas otras apps.",
            textoBoton = "Permitir avisos",
        )
        Permiso.BATERIA -> TextoPermiso(
            emoji = "🔋",
            titulo = "Que la batería no nos apague",
            explicacion = "Le decimos a tu teléfono que no duerma a PagoYa " +
                "para ahorrar batería.",
            porQue = "Si el teléfono nos duerme, una venta puede no sonar.",
            textoBoton = "Sin restricción",
        )
        Permiso.BLINDAR_YAPE -> {
            val guia = ProteccionMarca.guiaParaEsteTelefono()
            TextoPermiso(
                emoji = "🛡️",
                titulo = guia.titulo,
                explicacion = "Tu ${guia.marca} puede dormir al Yape solito, " +
                    "y ahí las ventas dejan de sonar aunque PagoYa esté bien.",
                porQue = "Es de una sola vez y es lo que más falla en teléfonos de gama media.",
                textoBoton = "Abrir ajustes",
                pasos = guia.pasos,
                seConfirmaAMano = true,
            )
        }
    }

    /**
     * Manda al usuario a la pantalla exacta. `pedirAvisos` lo pone la pantalla,
     * porque el permiso de notificaciones se pide con un launcher de Compose.
     */
    fun abrirAjuste(contexto: Context, permiso: Permiso, pedirAvisos: () -> Unit) {
        when (permiso) {
            Permiso.ESCUCHAR_NOTIFICACIONES ->
                contexto.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Permiso.AVISOS_PAGOYA -> pedirAvisos()
            Permiso.BATERIA -> contexto.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${contexto.packageName}")
                )
            )
            Permiso.BLINDAR_YAPE -> ProteccionMarca.abrirAjustes(contexto)
        }
    }
}

class EstadoPermisos internal constructor(
    val requeridos: List<Permiso>,
    val faltantes: List<Permiso>,
    /** Para cuando el cambio no vino de Ajustes (ej. "ya lo hice" del blindaje). */
    val refrescar: () -> Unit,
) {
    val todoListo: Boolean get() = faltantes.isEmpty()
    val concedidos: Int get() = requeridos.size - faltantes.size
}

/**
 * Estado de permisos recalculado cada vez que el usuario vuelve de Ajustes.
 * Es lo que hace que el asistente avance solo apenas se concede uno.
 */
@Composable
fun recordarPermisos(captura: Boolean): EstadoPermisos {
    val contexto = LocalContext.current
    // requeridos consulta el PackageManager: se calcula una vez, no por recomposición
    val requeridos = remember(captura) { Permisos.requeridos(contexto, captura) }
    var faltantes by remember(captura) {
        mutableStateOf(Permisos.faltantes(contexto, captura))
    }
    val refrescar = { faltantes = Permisos.faltantes(contexto, captura) }

    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo, captura) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) refrescar()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }
    return EstadoPermisos(
        requeridos = requeridos,
        faltantes = faltantes,
        refrescar = refrescar,
    )
}
