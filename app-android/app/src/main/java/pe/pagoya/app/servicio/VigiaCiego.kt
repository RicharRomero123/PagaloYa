package pe.pagoya.app.servicio

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.ConfigCiego
import pe.pagoya.app.core.Guardian
import pe.pagoya.app.core.RegistroCiego
import pe.pagoya.app.core.Salud
import pe.pagoya.app.core.Vibraciones
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.Sesion
import pe.pagoya.app.nube.TelemetriaRepo

/**
 * Modo ciego (ROADMAP §"App Android"): la red de seguridad de "si no suena, no
 * te pagaron". Si el teléfono de la caja deja de escuchar la Yape del negocio,
 * TODOS los teléfonos que oyen ese comercio se enteran — banner rojo + voz —
 * aunque el equipo de la caja esté congelado o sin señal.
 *
 * Dos mitades, ambas dentro del ciclo del servicio de primer plano:
 *
 *  1. EMISOR (solo el capturador): cada `ciego_cadencia_seg` y SOLO en horario
 *     de negocio, escribe `ultimaPresencia` (un latido rápido y liviano). Fuera
 *     de horario no escribe nada (ahorra batería y escrituras).
 *
 *  2. VIGÍA (todos): un snapshot listener a los dispositivos del comercio.
 *     Calcula `ahora - ultimaPresencia` del capturador más fresco; si supera
 *     `ciego_umbral_seg` en horario de negocio ⇒ CIEGO. En el propio teléfono
 *     capturador, además cuenta como ciego si su Yape está DETENIDA o el
 *     listener de notificaciones quedó desconectado (Guardian/Salud).
 *
 * Anti-spam de voz: al ENTRAR en ciego avisa UNA vez; luego re-recuerda a lo
 * más cada ~5 min mientras siga ciego. Al recuperar, cierra el periodo y avisa
 * una vez que ya volvió.
 *
 * Dedupe con FCM: la Cloud Function puede empujar `tipo=ciego` al dueño remoto.
 * Como la detección local y el push comparten `RegistroCiego` (idempotente) y
 * el mismo StateFlow, no se dispara doble.
 */
object VigiaCiego {

    private const val TAG = "PagoYa"

    /** Estado que observa la UI para pintar (o no) el banner rojo. */
    data class Estado(
        val ciego: Boolean = false,
        /** Hace cuánto se dejó de escuchar (ms). 0 si no está ciego. */
        val desdeMs: Long = 0L,
    ) {
        val minutos: Int get() = (desdeMs / 60_000L).toInt().coerceAtLeast(1)
    }

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var emisor: Job? = null
    private var evaluador: Job? = null
    private var listener: ListenerRegistration? = null
    private var iniciado = false

    /** Última presencia conocida del capturador más fresco (millis). */
    @Volatile
    private var ultimaPresencia: Long = 0L

    /** Anti-spam de la alerta hablada mientras sigue ciego. */
    private var ultimoAvisoHablado = 0L
    private const val REPETIR_AVISO_MS = 5 * 60 * 1000L

    /**
     * Arranca las dos mitades. Idempotente: lo llama el servicio en cada
     * onStartCommand y no queremos duplicar listeners ni bucles.
     */
    fun iniciar(context: Context) {
        if (iniciado) return
        if (!Sesion.conectado()) return
        val comercio = ComercioRepo.comercio.value ?: return
        iniciado = true
        val app = context.applicationContext

        RegistroCiego.cargar(app)
        // Si veníamos de un periodo abierto (la app se cerró estando ciega),
        // reflejarlo en el estado para que el banner reaparezca al volver.
        RegistroCiego.periodos.value.firstOrNull { it.abierto }?.let {
            _estado.value = Estado(ciego = true, desdeMs = it.duracionMs)
        }

        escucharDispositivos(app, comercio.id)
        arrancarEvaluador(app)
        if (comercio.puedeCapturar) arrancarEmisor(app)
    }

    fun detener() {
        emisor?.cancel(); emisor = null
        evaluador?.cancel(); evaluador = null
        listener?.remove(); listener = null
        iniciado = false
    }

    // ── 1. EMISOR (solo capturador) ─────────────────────────────────────────

    private fun arrancarEmisor(context: Context) {
        if (emisor?.isActive == true) return
        emisor = alcance.launch {
            while (isActive) {
                // Solo en horario de negocio: de madrugada no vende nadie.
                if (ConfigCiego.enHorarioNegocio()) {
                    TelemetriaRepo.escribirPresencia(context)
                }
                delay(ConfigCiego.cadenciaMs())
            }
        }
    }

    // ── 2. VIGÍA (todos) ────────────────────────────────────────────────────

    /**
     * Snapshot listener a los dispositivos del comercio. Nos quedamos con la
     * `ultimaPresencia` del capturador MÁS FRESCO (puede haber más de un
     * teléfono en captura): basta con que uno escuche para no estar ciego.
     */
    private fun escucharDispositivos(context: Context, comercioId: String) {
        listener?.remove()
        listener = FirebaseFirestore.getInstance()
            .collection("comercios").document(comercioId)
            .collection("dispositivos")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.w(TAG, "Vigía ciego, listener: ${error.message}")
                    return@addSnapshotListener
                }
                var masFresco = 0L
                snap?.documents?.forEach { doc ->
                    val capturando = doc.getBoolean("capturando") ?: false
                    if (!capturando) return@forEach
                    val ts = doc.getTimestamp("ultimaPresencia")?.toDate()?.time ?: 0L
                    if (ts > masFresco) masFresco = ts
                }
                if (masFresco > 0L) ultimaPresencia = masFresco
            }
    }

    /**
     * Bucle de evaluación: cada cadencia recalcula si estamos ciegos y dispara
     * banner/voz o recuperación. Se apoya en `ultimaPresencia` (de la nube) y,
     * en el capturador, también en el estado local de Yape y del listener.
     */
    private fun arrancarEvaluador(context: Context) {
        if (evaluador?.isActive == true) return
        evaluador = alcance.launch {
            while (isActive) {
                evaluar(context)
                delay(ConfigCiego.cadenciaMs())
            }
        }
    }

    private fun evaluar(context: Context) {
        // Fuera de horario no se vigila: ni ciego ni banner (evita falsos
        // positivos de madrugada cuando el capturador dejó de emitir a propósito).
        if (!ConfigCiego.enHorarioNegocio()) {
            if (_estado.value.ciego) recuperar(context, hablar = false)
            return
        }

        val ahora = System.currentTimeMillis()
        val comercio = ComercioRepo.comercio.value

        // El capturador se autoevalúa también con lo LOCAL: si su Yape está
        // detenida o el listener del sistema quedó desconectado, está sordo aquí
        // mismo aunque su reloj de presencia parezca fresco.
        val sordoLocal = comercio?.puedeCapturar == true && (
            Guardian.estadoYape(context) == Guardian.EstadoBilletera.DETENIDA ||
                !Salud.listenerConectado(context)
            )

        // Presencia: si nunca vimos una (arranque en frío sin datos aún), no
        // gritamos ciego a ciegas — esperamos a tener al menos un dato.
        val sinPresencia = ultimaPresencia > 0L &&
            (ahora - ultimaPresencia) > ConfigCiego.umbralMs()

        val ciego = sordoLocal || sinPresencia
        val desde = when {
            sordoLocal -> ConfigCiego.umbralMs() // el local es "ya, ahora": usa el umbral como piso
            sinPresencia -> ahora - ultimaPresencia
            else -> 0L
        }

        if (ciego) marcarCiego(context, desde) else recuperar(context, hablar = true)
    }

    // ── Transiciones de estado ──────────────────────────────────────────────

    /**
     * Entra o mantiene el estado ciego. Al ENTRAR: abre el periodo, vibra y
     * habla una vez. Mientras SIGUE ciego: solo re-recuerda por voz cada ~5 min.
     */
    private fun marcarCiego(context: Context, desdeMs: Long) {
        val entrando = !_estado.value.ciego
        _estado.value = Estado(ciego = true, desdeMs = desdeMs.coerceAtLeast(60_000L))

        if (entrando) {
            RegistroCiego.abrir(context)
            ultimoAvisoHablado = System.currentTimeMillis()
            Vibraciones.aviso(context)
            hablarCiego(context, _estado.value.minutos)
        } else {
            val ahora = System.currentTimeMillis()
            if (ahora - ultimoAvisoHablado >= REPETIR_AVISO_MS) {
                ultimoAvisoHablado = ahora
                hablarCiego(context, _estado.value.minutos)
            }
        }
    }

    /**
     * Vuelve a escuchar: cierra el periodo, limpia el banner y (opcional) avisa
     * que ya volvió. `hablar=false` cuando la recuperación es por fin de horario
     * (no es que "volvió la Yape", es que cerró el negocio) — ahí callamos.
     */
    private fun recuperar(context: Context, hablar: Boolean) {
        if (!_estado.value.ciego && !RegistroCiego.hayPeriodoAbierto()) return
        RegistroCiego.cerrar(context)
        val estabaCiego = _estado.value.ciego
        _estado.value = Estado()
        ultimoAvisoHablado = 0L
        if (hablar && estabaCiego) {
            Anunciador.anunciar(context, "Ya volví a escuchar tus pagos, casero.")
        }
    }

    private fun hablarCiego(context: Context, minutos: Int) {
        val tiempo = if (minutos <= 1) "hace un momento" else "hace $minutos minutos"
        Anunciador.anunciar(
            context,
            "Atención, dejé de escuchar los pagos $tiempo. Revisa que Yape esté prendido.",
        )
    }

    // ── FCM: empujón del dueño remoto (tipo=ciego / ciego_fin) ───────────────

    /**
     * Un push `tipo=ciego` de la Cloud Function fuerza el estado ciego aunque la
     * app estuviera cerrada (el capturador puede estar tan congelado que ni el
     * listener local reacciona). Dedupe con la detección local: si YA estábamos
     * ciegos, no re-vibramos ni re-hablamos aquí — `marcarCiego` decide.
     */
    fun recibirCiegoRemoto(context: Context, minutos: Int) {
        // Reutiliza la maquinaria local (periodo idempotente + anti-spam de voz).
        val desde = minutos.coerceAtLeast(1) * 60_000L
        marcarCiego(context.applicationContext, desde)
    }

    /** Push `tipo=ciego_fin`: el capturador revivió; limpiamos banner y periodo. */
    fun recibirCiegoFinRemoto(context: Context) {
        recuperar(context.applicationContext, hablar = false)
    }
}
