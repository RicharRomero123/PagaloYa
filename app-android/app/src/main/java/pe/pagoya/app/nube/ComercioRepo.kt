package pe.pagoya.app.nube

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import pe.pagoya.app.core.Pago
import kotlin.random.Random

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

    /** Cuántos pagos recientes vigila el modo escucha. */
    private const val VENTANA_PAGOS = 30L

    /** Ids de pagos que recordamos para no anunciar dos veces el mismo. */
    private const val MAX_RECORDADOS = 200

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

    fun limpiar() {
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
        )
        _comercio.value = cargado
        return cargado
    }

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

        paso("comercio") {
            refComercio.set(
                mapOf(
                    "nombre" to nombre.trim(),
                    "duenoUid" to uid,
                    "codigoVinculacion" to codigo,
                    "creadoEn" to System.currentTimeMillis(),
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

        val creado = Comercio(refComercio.id, nombre.trim(), codigo, "dueno", puedeCapturar = true)
        _comercio.value = creado
        creado
    }

    suspend fun unirseConCodigo(codigo: String): Result<Comercio> = runCatching {
        val uid = Sesion.uid ?: error("Sin sesión")
        val docCodigo = db.collection("codigos").document(codigo.trim()).get().await()
        val comercioId = docCodigo.getString("comercioId")
            ?: error("Código no válido. Pídele al dueño el código de 6 dígitos.")

        db.collection("comercios").document(comercioId)
            .collection("miembros").document(uid)
            .set(
                mapOf(
                    "rol" to "trabajador",
                    "nombre" to Sesion.nombreUsuario(),
                    // Modo escucha: este teléfono anuncia, no captura
                    "puedeCapturar" to false,
                )
            )
            .await()
        db.collection("usuarios").document(uid)
            .set(mapOf("comercioId" to comercioId))
            .await()

        val doc = db.collection("comercios").document(comercioId).get().await()
        val unido = Comercio(
            id = comercioId,
            nombre = doc.getString("nombre") ?: "Mi negocio",
            codigo = "",  // el código solo lo ve el dueño
            rol = "trabajador",
            puedeCapturar = false,
        )
        _comercio.value = unido
        unido
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
     * Modo escucha: anuncia pagos nuevos del comercio capturados por OTROS
     * teléfonos (los propios ya sonaron localmente al capturarse).
     *
     * Sin relojes de por medio: la primera foto que trae Firestore son los
     * pagos que YA existían al conectarnos (esos ya sonaron), así que se marcan
     * como vistos y no se anuncian. De ahí en adelante solo suena lo que LLEGA
     * nuevo. Da igual si el teléfono del trabajador está adelantado o atrasado
     * respecto al del dueño: no se pierden ni se repiten anuncios.
     */
    fun escucharPagos(alNuevoPago: (Pago) -> Unit): ListenerRegistration? {
        val uid = Sesion.uid ?: return null
        val comercioId = _comercio.value?.id ?: return null
        yaVistos.clear()
        var baseTomada = false

        return db.collection("comercios").document(comercioId)
            .collection("pagos")
            // Orden por la hora del SERVIDOR: es la única común a todos los
            // teléfonos del comercio.
            .orderBy("recibidoEn", Query.Direction.DESCENDING)
            .limit(VENTANA_PAGOS)
            .addSnapshotListener { captura, error ->
                if (error != null || captura == null) {
                    error?.let { Log.w(TAG, "Modo escucha interrumpido: ${it.message}") }
                    return@addSnapshotListener
                }
                val esBase = !baseTomada
                // Solo la respuesta del servidor sirve de base: la caché local
                // puede llegar vacía primero y luego "descubrir" pagos viejos,
                // que no hay que anunciar como si acabaran de caer.
                if (!captura.metadata.isFromCache) baseTomada = true

                captura.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .sortedBy { it.document.getLong("timestamp") ?: 0L }
                    .forEach { cambio ->
                        val esNuevo = recordar(cambio.document.id)
                        if (esBase || !esNuevo) return@forEach
                        val datos = cambio.document.data
                        // Los pagos que capturó este mismo teléfono ya sonaron
                        if (datos["origenUid"] == uid) return@forEach
                        alNuevoPago(aPago(datos) ?: return@forEach)
                    }
            }
    }

    /** true si es la primera vez que vemos este pago. */
    private fun recordar(idPago: String): Boolean {
        if (!yaVistos.add(idPago)) return false
        while (yaVistos.size > MAX_RECORDADOS) {
            yaVistos.remove(yaVistos.first())
        }
        return true
    }

    private fun aPago(datos: Map<String, Any>): Pago? {
        val monto = (datos["monto"] as? Number)?.toDouble() ?: return null
        return Pago(
            billeteraId = datos["billeteraId"] as? String ?: "?",
            billeteraNombre = datos["billeteraNombre"] as? String ?: "Pago",
            pagador = datos["pagador"] as? String ?: "Alguien",
            monto = monto,
            timestamp = (datos["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        )
    }
}
