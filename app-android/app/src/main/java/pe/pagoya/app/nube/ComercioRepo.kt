package pe.pagoya.app.nube

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.Plan
import pe.pagoya.app.core.PreferenciasBilleteras
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.core.voz.PreferenciasVoz
import kotlin.random.Random

/** El comercio ya llegó a su tope de teléfonos para el plan actual. */
class LimiteDispositivos(val plan: Plan) : Exception()

/**
 * Comercios en Firestore. Modelo:
 *  - comercios/{id}: nombre, duenoUid, codigoVinculacion
 *  - comercios/{id}/miembros/{uid}: rol dueno|trabajador, puedeCapturar
 *  - comercios/{id}/pagos/{pagoId}: el pago capturado (id determinista = idempotente)
 *  - codigos/{codigo}: comercioId  (vinculación de trabajadores)
 *  - usuarios/{uid}: comercioId    (a qué comercio pertenezco)
 */
object ComercioRepo {

    private const val TAG = "PagoYa"

    /** Ids de pagos que recordamos para no anunciar dos veces el mismo. */
    private const val MAX_RECORDADOS = 200

    /** Días de la prueba gratis con la que nace todo comercio nuevo. */
    private const val DIAS_PRUEBA = 30

    /** Plan que se activa durante la prueba gratis (el intermedio, "Caserito"). */
    private const val PLAN_PRUEBA = "caserito"

    data class Comercio(
        val id: String,
        val nombre: String,
        val codigo: String,
        val rol: String, // "dueno" | "trabajador"
        /**
         * Anti-fake: solo el teléfono con el Yape del negocio crea pagos.
         * Los trabajadores están en modo escucha y el servidor les rechaza
         * cualquier intento de escribir un pago (ver firestore.rules).
         */
        val puedeCapturar: Boolean,
        /** Plan del comercio (deriva su tope de dispositivos). */
        val plan: Plan = Plan.GRATIS,
        /**
         * Estado de la suscripción: "prueba" | "activa" | null (gratis puro).
         * Sirve para mostrar en pantalla si es prueba gratis o plan pagado.
         */
        val planEstado: String? = null,
        /**
         * Hasta cuándo vige el plan (millis). 0 = sin vencimiento (gratis).
         * Se muestra como fecha de expiración/renovación en el Perfil.
         */
        val planVigenteHasta: Long = 0L,
        /**
         * Privacidad de caja (permiso criollo). Por DEFECTO en true: el
         * trabajador ve la caja como el dueño. Si el dueño lo apaga, su gente
         * solo escucha los pagos y no ve totales ni métricas. Campo ausente en
         * comercios viejos → true (nadie pierde acceso por la actualización).
         */
        val trabajadorVeCaja: Boolean = true,
        /**
         * "Casero trae Casero": queda en true cuando el dueño ya canjeó un
         * código de referido (la Cloud Function lo marca). Sirve para ocultar la
         * tarjeta de canje una vez usada. Campo ausente → false (aún puede
         * canjear).
         */
        val referidoCanjeado: Boolean = false,
    )

    data class Miembro(
        val uid: String,
        val nombre: String,
        val rol: String,
        val puedeCapturar: Boolean,
    )

    private val db get() = FirebaseFirestore.getInstance()

    private val yaVistos = LinkedHashSet<String>()

    private val _comercio = MutableStateFlow<Comercio?>(null)
    val comercio: StateFlow<Comercio?> = _comercio

    /**
     * Registro del listener que oye mi propio doc de miembro para OBEDECER en
     * vivo lo que el operador cambie desde el panel (ej. me apaga la voz). Se
     * guarda para poder soltarlo (evitar duplicados y fugas al cerrar sesión).
     */
    private var registroPrefs: ListenerRegistration? = null

    fun limpiar() {
        // Al cerrar sesión soltamos el oído remoto: si no, quedaría vivo
        // apuntando al comercio anterior.
        dejarDeEscucharPreferencias()
        _comercio.value = null
    }

    /** Carga el comercio del usuario conectado (null si aún no tiene). */
    suspend fun cargar(): Comercio? {
        val uid = Sesion.uid ?: return null
        _comercio.value?.let { return it }
        val perfil = db.collection("usuarios").document(uid).get().await()
        val comercioId = perfil.getString("comercioId") ?: return null
        val doc = db.collection("comercios").document(comercioId).get().await()
        if (!doc.exists()) return null
        val miembro = db.collection("comercios").document(comercioId)
            .collection("miembros").document(uid).get().await()
        val rol = miembro.getString("rol") ?: "trabajador"
        val cargado = Comercio(
            id = comercioId,
            nombre = doc.getString("nombre") ?: "Mi negocio",
            codigo = doc.getString("codigoVinculacion") ?: "",
            rol = rol,
            // Miembros creados antes de que existiera el campo: manda el rol
            puedeCapturar = miembro.getBoolean("puedeCapturar") ?: (rol == "dueno"),
            plan = planDe(doc),
            planEstado = estadoDe(doc),
            planVigenteHasta = vigenteHastaDe(doc),
            trabajadorVeCaja = doc.getBoolean("trabajadorVeCaja") ?: true,
            referidoCanjeado = doc.getBoolean("referidoCanjeado") ?: false,
        )
        _comercio.value = cargado
        return cargado
    }

    /**
     * Fuerza recargar el comercio desde Firestore, descartando lo que hay en
     * memoria. Se usa tras canjear un referido: la Cloud Function cambió la
     * vigencia y `referidoCanjeado` en el servidor, y aquí lo reflejamos.
     */
    suspend fun recargar(): Comercio? {
        _comercio.value = null
        return cargar()
    }

    /**
     * Plan EFECTIVO del comercio a partir de `suscripcion`. La prueba de 30 días
     * (y cualquier plan pago) se degrada SOLA por fecha: como no hay Cloud
     * Functions que volteen el estado al vencer, la propia `vigenteHasta` manda.
     * Un plan solo cuenta si su estado es "prueba" o "activa" Y aún no venció;
     * en cualquier otro caso (vencida, sin fecha, fecha pasada, plan raro) es
     * Gratis → vuelve a 1 teléfono. Esto refleja exactamente `planEfectivo()` de
     * firestore.rules; el servidor es la fuente de verdad, esto es solo la 1ra
     * capa para la UI y el gate de "unirse".
     */
    @Suppress("UNCHECKED_CAST")
    private fun planDe(doc: com.google.firebase.firestore.DocumentSnapshot): Plan {
        val susc = doc.get("suscripcion") as? Map<String, Any> ?: return Plan.GRATIS
        val estado = susc["estado"] as? String
        val vigenteHasta = (susc["vigenteHasta"] as? Number)?.toLong() ?: 0L
        val vigente = estado in setOf("prueba", "activa") &&
            vigenteHasta >= System.currentTimeMillis()
        return if (vigente) Plan.desdeId(susc["plan"] as? String) else Plan.GRATIS
    }

    /** Estado de la suscripción ("prueba"|"activa") si sigue vigente; si no, null. */
    @Suppress("UNCHECKED_CAST")
    private fun estadoDe(doc: com.google.firebase.firestore.DocumentSnapshot): String? {
        val susc = doc.get("suscripcion") as? Map<String, Any> ?: return null
        val estado = susc["estado"] as? String
        val vigenteHasta = (susc["vigenteHasta"] as? Number)?.toLong() ?: 0L
        val vigente = estado in setOf("prueba", "activa") &&
            vigenteHasta >= System.currentTimeMillis()
        return if (vigente) estado else null
    }

    /** Millis de vencimiento del plan si sigue vigente; si no, 0. */
    @Suppress("UNCHECKED_CAST")
    private fun vigenteHastaDe(doc: com.google.firebase.firestore.DocumentSnapshot): Long {
        val susc = doc.get("suscripcion") as? Map<String, Any> ?: return 0L
        val estado = susc["estado"] as? String
        val vigenteHasta = (susc["vigenteHasta"] as? Number)?.toLong() ?: 0L
        val vigente = estado in setOf("prueba", "activa") &&
            vigenteHasta >= System.currentTimeMillis()
        return if (vigente) vigenteHasta else 0L
    }

    /** Cuántos teléfonos hay vinculados al comercio (dueño incluido). */
    private suspend fun contarDispositivos(comercioId: String): Int =
        db.collection("comercios").document(comercioId)
            .collection("miembros").get().await().size()

    suspend fun crearComercio(nombre: String): Result<Comercio> = runCatching {
        val uid = Sesion.uid ?: error("Sin sesión")
        val codigo = "%06d".format(Random.nextInt(0, 1_000_000))
        val refComercio = db.collection("comercios").document()

        // Pasos secuenciales con etiqueta: si Firebase rechaza uno, el error
        // dice exactamente cuál (clave para diagnosticar reglas).
        suspend fun paso(nombrePaso: String, accion: suspend () -> Unit) {
            runCatching { accion() }.onFailure {
                error("[$nombrePaso] ${it.message}")
            }
        }

        // Prueba gratis de 30 días: el comercio nace con una suscripción "prueba"
        // que vence a los DIAS_PRUEBA. La fecha de vencimiento (vigenteHasta) es
        // la fuente de verdad — la app y el panel la muestran, y al vencer el
        // plan se degrada solo a Gratis (ver planDe/planEfectivo). El operador
        // controla y extiende esto desde el panel.
        val ahora = System.currentTimeMillis()
        val venceElPrueba = ahora + DIAS_PRUEBA * 24L * 60L * 60L * 1000L

        paso("comercio") {
            refComercio.set(
                mapOf(
                    "nombre" to nombre.trim(),
                    "duenoUid" to uid,
                    "codigoVinculacion" to codigo,
                    "creadoEn" to ahora,
                    // Semilla del contador de dispositivos: nace con el dueño (1).
                    // Es lo que hace cumplir el tope del plan del lado servidor.
                    "numDispositivos" to 1,
                    // Prueba gratis de 30 días con fecha de finalización explícita.
                    "suscripcion" to mapOf(
                        "estado" to "prueba",
                        "plan" to PLAN_PRUEBA,
                        "inicioPrueba" to ahora,
                        "vigenteHasta" to venceElPrueba,
                    ),
                )
            ).await()
        }
        paso("miembro-dueno") {
            refComercio.collection("miembros").document(uid)
                .set(
                    mapOf(
                        "rol" to "dueno",
                        "nombre" to Sesion.nombreUsuario(),
                        "puedeCapturar" to true,
                    )
                ).await()
        }
        paso("codigo") {
            db.collection("codigos").document(codigo)
                .set(mapOf("comercioId" to refComercio.id)).await()
        }
        paso("perfil") {
            db.collection("usuarios").document(uid)
                .set(mapOf("comercioId" to refComercio.id)).await()
        }

        val creado = Comercio(
            id = refComercio.id,
            nombre = nombre.trim(),
            codigo = codigo,
            rol = "dueno",
            puedeCapturar = true,
            // Refleja la prueba recién sembrada para que el Perfil muestre la
            // fecha de vencimiento sin esperar a una recarga desde la nube.
            plan = Plan.desdeId(PLAN_PRUEBA),
            planEstado = "prueba",
            planVigenteHasta = venceElPrueba,
        )
        _comercio.value = creado
        creado
    }

    suspend fun unirseConCodigo(codigo: String): Result<Comercio> = runCatching {
        val uid = Sesion.uid ?: error("Sin sesión")
        val docCodigo = db.collection("codigos").document(codigo.trim()).get().await()
        val comercioId = docCodigo.getString("comercioId")
            ?: error("Código no válido. Pídele al dueño el código de 6 dígitos.")

        val doc = db.collection("comercios").document(comercioId).get().await()
        val plan = planDe(doc)

        // Gate del plan (primera capa, cliente). Si este uid YA es miembro, no
        // cuenta como teléfono nuevo — no se bloquea a quien solo re-entra.
        val yaEsMiembro = db.collection("comercios").document(comercioId)
            .collection("miembros").document(uid).get().await().exists()
        if (!yaEsMiembro && contarDispositivos(comercioId) >= plan.maxDispositivos) {
            throw LimiteDispositivos(plan)
        }

        // Baranda de servidor del tope: el miembro nuevo y el +1 al contador
        // numDispositivos van en el MISMO lote atómico. Las reglas de Firestore
        // exigen esa coherencia (create de miembro ligado al incremento) y que
        // el incremento no pase del tope del plan. Así, aunque un APK modificado
        // se salte el gate del cliente de arriba, el servidor rechaza el lote
        // entero si ya no hay cupo. Si este uid ya era miembro (re-entra), no se
        // vuelve a incrementar.
        val refComercio = db.collection("comercios").document(comercioId)
        val batch = db.batch()
        batch.set(
            refComercio.collection("miembros").document(uid),
            mapOf(
                "rol" to "trabajador",
                "nombre" to Sesion.nombreUsuario(),
                // Modo escucha: este teléfono anuncia, no captura
                "puedeCapturar" to false,
            ),
        )
        if (!yaEsMiembro) {
            batch.update(refComercio, "numDispositivos", FieldValue.increment(1))
        }
        batch.commit().await()
        db.collection("usuarios").document(uid)
            .set(mapOf("comercioId" to comercioId))
            .await()

        val unido = Comercio(
            id = comercioId,
            nombre = doc.getString("nombre") ?: "Mi negocio",
            codigo = "",  // el código solo lo ve el dueño
            rol = "trabajador",
            puedeCapturar = false,
            plan = plan,
            planEstado = estadoDe(doc),
            planVigenteHasta = vigenteHastaDe(doc),
            trabajadorVeCaja = doc.getBoolean("trabajadorVeCaja") ?: true,
            referidoCanjeado = doc.getBoolean("referidoCanjeado") ?: false,
        )
        _comercio.value = unido
        unido
    }

    /**
     * El dueño enciende/apaga si su gente ve la caja (total del día y métricas).
     * Es un permiso de UI: el modo escucha del trabajador sigue leyendo los
     * pagos para anunciarlos; esto solo decide si además ve los acumulados.
     * Solo el dueño puede llamarlo (las reglas de Firestore lo exigen).
     */
    suspend fun fijarVerCajaTrabajador(ver: Boolean): Result<Unit> = runCatching {
        val actual = _comercio.value ?: error("Sin comercio")
        db.collection("comercios").document(actual.id)
            .update("trabajadorVeCaja", ver).await()
        _comercio.value = actual.copy(trabajadorVeCaja = ver)
    }

    /** La gente del comercio, para la pestaña Equipo. */
    suspend fun miembros(): List<Miembro> {
        val comercioId = _comercio.value?.id ?: return emptyList()
        val docs = db.collection("comercios").document(comercioId)
            .collection("miembros").get().await()
        return docs.map { doc ->
            val rol = doc.getString("rol") ?: "trabajador"
            Miembro(
                uid = doc.id,
                nombre = doc.getString("nombre") ?: "Sin nombre",
                rol = rol,
                puedeCapturar = doc.getBoolean("puedeCapturar") ?: (rol == "dueno"),
            )
        }.sortedByDescending { it.rol == "dueno" }
    }

    /**
     * Sube un pago capturado. Fire-and-forget con id determinista: si la misma
     * notificación se procesara dos veces, no se duplica en la nube (y el
     * segundo intento lo rechazan las reglas, porque los pagos son inmutables).
     *
     * `recibidoEn` lo pone el SERVIDOR: es la hora autoritativa del pago. El
     * `timestamp` del teléfono queda como dato de la notificación, pero ya no
     * decide nada — así el reloj desajustado de un equipo no rompe nada.
     */
    fun subirPago(pago: Pago) {
        val uid = Sesion.uid ?: return
        val comercio = _comercio.value ?: return
        // Anti-fake, primera capa: solo el teléfono con el Yape del negocio
        // sube pagos. Las reglas de Firestore lo vuelven a exigir del lado del
        // servidor, así que esto es solo para no gastar una escritura de más.
        if (!comercio.puedeCapturar) return

        val centavos = Math.round(pago.monto * 100)
        val pagoId = "$uid-${pago.timestamp}-$centavos"
        db.collection("comercios").document(comercio.id)
            .collection("pagos").document(pagoId)
            .set(
                mapOf(
                    "billeteraId" to pago.billeteraId,
                    "billeteraNombre" to pago.billeteraNombre,
                    "pagador" to pago.pagador,
                    "monto" to pago.monto,
                    "timestamp" to pago.timestamp,
                    "origenUid" to uid,
                    "recibidoEn" to FieldValue.serverTimestamp(),
                )
            )
            .addOnFailureListener {
                // El pago igual sonó en el local y quedó en el registro del
                // teléfono; lo que falla es solo el eco a los trabajadores.
                Log.w(TAG, "Pago no subido ($pagoId): ${it.message}")
            }
    }

    /**
     * Modo escucha por PUSH (reemplaza al viejo listener de Firestore): la
     * Cloud Function empuja cada pago capturado a los demás teléfonos del
     * comercio vía FCM, y el servicio de mensajería lo entrega aquí. Anuncia el
     * pago del comercio capturado por OTRO teléfono (el capturador ya sonó
     * local al momento de capturarlo).
     *
     * Anti-fake: solo se anuncia si el push trae el comercio ACTUAL del usuario
     * y NO lo originó este mismo teléfono. La deduplicación va por `pagoId` (la
     * misma clave determinista que usa la nube), así un push repetido no vuelve
     * a sonar.
     *
     * Devuelve true si se anunció (para diagnóstico), false si se descartó.
     */
    fun recibirPagoRemoto(
        context: Context,
        pagoId: String,
        comercioId: String,
        origenUid: String,
        pago: Pago,
    ): Boolean {
        val uid = Sesion.uid ?: return false
        val actual = _comercio.value?.id ?: return false
        // El push es de otro comercio (o el usuario ya cambió de comercio): no.
        if (comercioId != actual) return false
        // Lo capturó este mismo teléfono: ya sonó local, no repetir.
        if (origenUid == uid) return false
        // Gate de billeteras: este teléfono (modo escucha) apagó esta billetera.
        // La ignoramos por completo — no suena ni entra al historial. La decisión
        // es LOCAL: cada equipo del comercio elige qué billeteras escuchar.
        if (!PreferenciasBilleteras.estaActiva(pago.billeteraId)) {
            Log.d(TAG, "Pago remoto descartado: billetera ${pago.billeteraId} apagada en este equipo")
            return false
        }
        // Anti-duplicado: mismo pagoId no suena dos veces.
        if (!recordar(pagoId)) return false

        RegistroPagos.agregar(context, pago)
        Anunciador.anunciarPago(context, pago)
        return true
    }

    /**
     * Rehidrata la Caja desde Firestore: rellena el registro local con los pagos
     * inmutables del comercio (`comercios/{id}/pagos`). Es carga SILENCIOSA —
     * NO pasa por el Anunciador, NO suena TTS ni vibra: solo llena el registro
     * para que el historial no se sienta perdido tras reinstalar, cambiar de
     * teléfono o limpiar datos (los pagos siguen en la nube, no en el APK).
     *
     * SUMA una carga inicial; no reemplaza ni el capturador ni el push FCM.
     *
     * Ventana de lecturas (para no comerse la cuota): `orderBy(recibidoEn, DESC)`
     * + `limit(300)`. Elegí un tope fijo por docs (no una ventana por fecha)
     * porque es una sola query, sin cálculo de fechas, y 300 coincide con el
     * tope local (RegistroPagos.MAX_GUARDADOS): traer más se truncaría igual al
     * fusionar. Es UNA lectura de <=300 docs por arranque de sesión. A escala
     * grande esto se puede paginar o cachear; Firestore ya cachea offline, así
     * que un `get()` por sesión es aceptable.
     *
     * Deduplicación: la hace RegistroPagos.fusionar por huella estable, así que
     * coexiste sin duplicar con lo ya guardado en SharedPreferences y con lo que
     * llegue por `recibirPagoRemoto` (push).
     *
     * Falla con gracia: si la lectura falla (sin red, etc.), no crashea; la Caja
     * sigue mostrando lo local. Pensado para correr en coroutine de fondo.
     */
    suspend fun rehidratarPagos(context: Context) {
        val comercioId = _comercio.value?.id ?: return
        runCatching {
            val docs = db.collection("comercios").document(comercioId)
                .collection("pagos")
                .orderBy("recibidoEn", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(300)
                .get()
                .await()
            val pagos = docs.mapNotNull { d ->
                val monto = d.getDouble("monto") ?: return@mapNotNull null
                Pago(
                    billeteraId = d.getString("billeteraId") ?: "?",
                    billeteraNombre = d.getString("billeteraNombre") ?: "Pago",
                    pagador = d.getString("pagador") ?: "Alguien",
                    monto = monto,
                    // `timestamp` del pago (de la notificación). Ignoramos
                    // `recibidoEn`/`origenUid`: no son parte del modelo Pago.
                    timestamp = d.getLong("timestamp") ?: 0L,
                )
            }
            RegistroPagos.fusionar(context, pagos)
        }.onFailure {
            // Sin red o sin permiso: la Caja se queda con lo local, no se cae.
            Log.w(TAG, "Rehidratación de caja falló: ${it.message}")
        }
    }

    /**
     * Espejo (best-effort) de la preferencia de escucha de ESTE teléfono en su
     * documento de miembro, para que el operador vea desde el panel qué equipos
     * están silenciados o con horario. NO es la fuente de verdad: el
     * comportamiento manda desde local (SharedPreferences vía PreferenciasVoz);
     * esto es solo visibilidad. Fire-and-forget: no bloquea la UI y, si no hay
     * red, no crashea (Firestore encola y reintenta; si falla, solo se loguea).
     *
     * Contrato de campos EXACTO (las firestore.rules dependen de estos nombres):
     *   silenciado (bool), horarioActivo (bool),
     *   horarioInicio (int, minutos 0-1439), horarioFin (int, minutos 0-1439).
     */
    fun sincronizarEscucha(context: Context) {
        val uid = Sesion.uid ?: return
        val comercioId = _comercio.value?.id ?: return
        db.collection("comercios").document(comercioId)
            .collection("miembros").document(uid)
            .update(
                mapOf(
                    "silenciado" to PreferenciasVoz.silenciado(context),
                    "horarioActivo" to PreferenciasVoz.horarioActivo(context),
                    "horarioInicio" to PreferenciasVoz.horaInicio(context),
                    "horarioFin" to PreferenciasVoz.horaFin(context),
                )
            )
            .addOnFailureListener {
                Log.w(TAG, "No se sincronizó preferencia de escucha: ${it.message}")
            }
    }

    /**
     * OBEDECE EN VIVO: oye MI documento de miembro y baja a este teléfono lo que
     * el operador (o yo mismo desde otro lado) cambie de la preferencia de
     * escucha. Ejemplo: el operador me apaga la voz desde el panel y este equipo
     * se calla al toque, sin reabrir la app.
     *
     * Anti-fake, ojo: mutear —sea local o remoto— SOLO calla la voz de este
     * teléfono. NO toca el historial ni el reenvío: el pago igual se capturó de
     * una notificación REAL del sistema, se guardó en la Caja y le llegó a los
     * demás equipos por push. Silenciar no borra ni inventa nada, solo baja el
     * parlante de este aparato.
     *
     * Es de UNA sola vía: LEE de Firestore y ESCRIBE a SharedPreferences (vía
     * PreferenciasVoz). JAMÁS llama a sincronizarEscucha desde aquí: eso armaría
     * un bucle de escritura (yo escribo → el eco me vuelve → vuelvo a escribir…).
     * El trabajador igual puede revertir desde su app: su toggle escribe a
     * Firestore y el eco regresa con el MISMO valor, así que converge, no rebota.
     *
     * Defensivo: si falla (sin red, doc borrado), retorna sin drama. El SDK de
     * Firestore ya cachea offline y reintenta la reconexión solito.
     */
    fun escucharPreferencias(context: Context) {
        val uid = Sesion.uid ?: return
        val comercioId = _comercio.value?.id ?: return
        val appContext = context.applicationContext

        // Si ya había un oído abierto, lo soltamos antes de abrir otro: así no
        // duplicamos callbacks (arranque frío + onResume podrían llamar dos veces).
        registroPrefs?.remove()

        registroPrefs = db.collection("comercios").document(comercioId)
            .collection("miembros").document(uid)
            .addSnapshotListener { snap, err ->
                // Falla con gracia: sin red, con error o doc inexistente, no
                // hacemos nada. El SDK reintenta solo cuando vuelva la conexión.
                if (err != null || snap == null || !snap.exists()) return@addSnapshotListener

                // Espejamos SOLO los campos PRESENTES en el snapshot. Si un doc
                // viejo no trae el campo, NO lo pisamos con un default: así el
                // panel no borra sin querer una preferencia que el equipo eligió
                // localmente y que aún no ha subido.
                snap.getBoolean("silenciado")?.let {
                    PreferenciasVoz.definirSilenciado(appContext, it)
                }
                snap.getBoolean("horarioActivo")?.let {
                    PreferenciasVoz.definirHorarioActivo(appContext, it)
                }
                snap.getLong("horarioInicio")?.let {
                    PreferenciasVoz.definirHoraInicio(appContext, it.toInt())
                }
                snap.getLong("horarioFin")?.let {
                    PreferenciasVoz.definirHoraFin(appContext, it.toInt())
                }
            }
    }

    /** Suelta el oído remoto de preferencias (logout o teardown del servicio). */
    fun dejarDeEscucharPreferencias() {
        registroPrefs?.remove()
        registroPrefs = null
    }

    /** true si es la primera vez que vemos este pago. */
    private fun recordar(idPago: String): Boolean {
        if (!yaVistos.add(idPago)) return false
        while (yaVistos.size > MAX_RECORDADOS) {
            yaVistos.remove(yaVistos.first())
        }
        return true
    }
}
