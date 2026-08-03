package pe.pagoya.app.servicio

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Vibraciones

/**
 * Offline Guard (spec 3.3): vigila la conectividad y avisa cuando el teléfono se
 * queda sin internet.
 *
 * En un mercado el dato se cae seguido, y sin red el modo escucha deja de recibir
 * pagos por FCM (y el capturador no puede subirlos). Antes de que alguien se dé
 * cuenta tarde, PagoYa lo avisa: banner persistente + un aviso hablado UNA sola
 * vez en la transición online→offline (nunca en bucle).
 *
 * El `NetworkCallback` lo registra `ServicioPrimerPlano`, que corre siempre. La
 * UI observa `sinRed` para pintar el banner.
 */
object RedGuardia {

    private val _sinRed = MutableStateFlow(false)

    /** true mientras el teléfono está sin internet. La UI lo observa. */
    val sinRed: StateFlow<Boolean> = _sinRed

    private var registrado = false
    private var callback: ConnectivityManager.NetworkCallback? = null

    /** Registra el vigilante de red. Idempotente: no duplica el callback. */
    fun iniciar(context: Context) {
        if (registrado) return
        val appContext = context.applicationContext
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return

        // Estado inicial real, sin avisar: no queremos hablar al arrancar aunque
        // ya estemos offline (solo avisamos en la transición a offline).
        _sinRed.value = !hayInternet(cm)

        val solicitud = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                marcarConRed(appContext)
            }

            override fun onLost(network: Network) {
                // Puede quedar otra red disponible (ej. se cae el Wi-Fi pero hay
                // datos): recién marcamos offline si de verdad no queda ninguna.
                if (!hayInternet(cm)) marcarSinRed(appContext)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capacidades: NetworkCapabilities,
            ) {
                val validada = capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (validada) marcarConRed(appContext) else if (!hayInternet(cm)) marcarSinRed(appContext)
            }
        }
        runCatching {
            cm.registerNetworkCallback(solicitud, cb)
            callback = cb
            registrado = true
        }
    }

    fun detener(context: Context) {
        val cb = callback ?: return
        val cm = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        runCatching { cm?.unregisterNetworkCallback(cb) }
        callback = null
        registrado = false
    }

    private fun marcarSinRed(context: Context) {
        // Solo avisamos en la TRANSICIÓN a offline: si ya estábamos sin red, no
        // repetimos (nada de spam ni bucle).
        if (_sinRed.value) return
        _sinRed.value = true
        Vibraciones.aviso(context)
        Anunciador.anunciar(
            context,
            "¡Cuidado! Te quedaste sin internet. Por ahora revisa la pantalla del Yape hasta que vuelva la señal.",
        )
    }

    private fun marcarConRed(context: Context) {
        if (!_sinRed.value) return
        _sinRed.value = false
        // Aviso corto y opcional al volver la señal (sin vibrar): tranquiliza.
        Anunciador.anunciar(context, "Ya volvió tu internet, casero.")
    }

    private fun hayInternet(cm: ConnectivityManager): Boolean {
        val red = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(red) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
