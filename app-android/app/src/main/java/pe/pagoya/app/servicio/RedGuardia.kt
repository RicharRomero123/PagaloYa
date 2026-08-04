package pe.pagoya.app.servicio

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
 * Robustez (dos claves para que el aviso sea de fiar):
 *  1. Internet REAL, no "conectado a secas": exigimos NET_CAPABILITY_VALIDATED.
 *     Un Wi-Fi de portal cautivo o unos datos sin saldo dicen que hay red pero
 *     NO navegan — y sin navegar los pagos no llegan. VALIDATED = Android probó
 *     la salida a internet y la confirmó.
 *  2. Anti-parpadeo: en el mercado la señal da baches de segundos. No gritamos
 *     por cada bache: solo avisamos si NO hay internet real durante una ventana
 *     seguida (debounce). Si la señal vuelve antes, nadie se entera del bache.
 *
 * El `NetworkCallback` lo registra `ServicioPrimerPlano`, que corre siempre. La
 * UI observa `sinRed` para pintar el banner.
 */
object RedGuardia {

    private val _sinRed = MutableStateFlow(false)

    /** true mientras el teléfono está sin internet CONFIRMADO. La UI lo observa. */
    val sinRed: StateFlow<Boolean> = _sinRed

    private var registrado = false
    private var callback: ConnectivityManager.NetworkCallback? = null

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var trabajoOffline: Job? = null

    /** Cuánto debe estar sin internet real, seguido, antes de que avisemos. */
    private const val ESPERA_CONFIRMACION_MS = 20_000L

    /** Registra el vigilante de red. Idempotente: no duplica el callback. */
    fun iniciar(context: Context) {
        if (registrado) return
        val appContext = context.applicationContext
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return

        // Arrancamos optimistas (sin banner) para no parpadear en cada apertura
        // mientras Android termina de validar la red. Si de verdad no hay salida,
        // el debounce de abajo lo confirma y recién ahí avisamos.
        _sinRed.value = false

        val solicitud = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // "Disponible" todavía NO garantiza internet real: la confirmación
                // llega en onCapabilitiesChanged cuando aparece VALIDATED. Aquí no
                // marcamos online para no cantar victoria antes de tiempo.
            }

            override fun onLost(network: Network) {
                // Puede quedar otra red (se cae el Wi-Fi pero hay datos): recién
                // arrancamos la cuenta regresiva si de verdad no queda ninguna.
                if (!hayInternetReal(cm)) programarOffline(appContext, cm)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capacidades: NetworkCapabilities,
            ) {
                if (capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    marcarConRed(appContext)
                } else if (!hayInternetReal(cm)) {
                    programarOffline(appContext, cm)
                }
            }
        }
        runCatching {
            cm.registerNetworkCallback(solicitud, cb)
            callback = cb
            registrado = true
            // Chequeo inicial: si al arrancar ya no hay internet real, que el
            // debounce lo confirme (sin banner instantáneo ni falso positivo).
            if (!hayInternetReal(cm)) programarOffline(appContext, cm)
        }
    }

    fun detener(context: Context) {
        trabajoOffline?.cancel()
        trabajoOffline = null
        val cb = callback ?: return
        val cm = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        runCatching { cm?.unregisterNetworkCallback(cb) }
        callback = null
        registrado = false
    }

    /**
     * Arranca la cuenta regresiva para avisar offline. Si la señal vuelve antes
     * de cumplirse (marcarConRed cancela el trabajo), el bache pasa en silencio.
     */
    private fun programarOffline(context: Context, cm: ConnectivityManager) {
        if (_sinRed.value) return                     // ya avisamos, no repetir
        if (trabajoOffline?.isActive == true) return  // ya hay una cuenta en curso
        trabajoOffline = alcance.launch {
            delay(ESPERA_CONFIRMACION_MS)
            // Recomprobar al final: puede haber vuelto la red durante la espera.
            if (!hayInternetReal(cm)) marcarSinRed(context)
        }
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
        // Cualquier confirmación de internet real corta una cuenta regresiva
        // pendiente: así un bache que se recuperó no termina avisando tarde.
        trabajoOffline?.cancel()
        trabajoOffline = null
        if (!_sinRed.value) return
        _sinRed.value = false
        // Aviso corto y opcional al volver la señal (sin vibrar): tranquiliza.
        Anunciador.anunciar(context, "Ya volvió tu internet, casero.")
    }

    /**
     * Internet REAL: no basta con estar "conectado". Exigimos que la red tenga
     * la intención de dar internet (INTERNET) Y que Android haya confirmado la
     * salida (VALIDATED). Así un portal cautivo o datos sin saldo cuentan como
     * SIN internet — que es la verdad para lo que nos importa: recibir pagos.
     */
    private fun hayInternetReal(cm: ConnectivityManager): Boolean {
        val red = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(red) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
