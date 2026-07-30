package pe.pagoya.app.nube

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
 *  - comercios/{id}/miembros/{uid}: rol dueno|trabajador
 *  - comercios/{id}/pagos/{pagoId}: el pago capturado (id determinista = idempotente)
 *  - codigos/{codigo}: comercioId  (vinculación de trabajadores)
 *  - usuarios/{uid}: comercioId    (a qué comercio pertenezco)
 */
object ComercioRepo {

    data class Comercio(
        val id: String,
        val nombre: String,
        val codigo: String,
        val rol: String, // "dueno" | "trabajador"
    )

    private val db get() = FirebaseFirestore.getInstance()

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
        val cargado = Comercio(
            id = comercioId,
            nombre = doc.getString("nombre") ?: "Mi negocio",
            codigo = doc.getString("codigoVinculacion") ?: "",
            rol = miembro.getString("rol") ?: "trabajador",
        )
        _comercio.value = cargado
        return cargado
    }

    suspend fun crearComercio(nombre: String): Result<Comercio> = runCatching {
        val uid = Sesion.uid ?: error("Sin sesión")
        val codigo = "%06d".format(Random.nextInt(0, 1_000_000))
        val refComercio = db.collection("comercios").document()

        val lote = db.batch()
        lote.set(
            refComercio,
            mapOf(
                "nombre" to nombre.trim(),
                "duenoUid" to uid,
                "codigoVinculacion" to codigo,
                "creadoEn" to System.currentTimeMillis(),
            )
        )
        lote.set(
            refComercio.collection("miembros").document(uid),
            mapOf("rol" to "dueno", "nombre" to Sesion.nombreUsuario())
        )
        lote.set(
            db.collection("codigos").document(codigo),
            mapOf("comercioId" to refComercio.id)
        )
        lote.set(
            db.collection("usuarios").document(uid),
            mapOf("comercioId" to refComercio.id)
        )
        lote.commit().await()

        val creado = Comercio(refComercio.id, nombre.trim(), codigo, "dueno")
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
            .set(mapOf("rol" to "trabajador", "nombre" to Sesion.nombreUsuario()))
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
        )
        _comercio.value = unido
        unido
    }

    /**
     * Sube un pago capturado. Fire-and-forget con id determinista: si la misma
     * notificación se procesara dos veces, no se duplica en la nube.
     */
    fun subirPago(pago: Pago) {
        val uid = Sesion.uid ?: return
        val comercioId = _comercio.value?.id ?: return
        val pagoId = "${uid}-${pago.timestamp}-${(pago.monto * 100).toLong()}"
        db.collection("comercios").document(comercioId)
            .collection("pagos").document(pagoId)
            .set(
                mapOf(
                    "billeteraId" to pago.billeteraId,
                    "billeteraNombre" to pago.billeteraNombre,
                    "pagador" to pago.pagador,
                    "monto" to pago.monto,
                    "timestamp" to pago.timestamp,
                    "origenUid" to uid,
                )
            )
    }

    /**
     * Modo escucha: anuncia pagos nuevos del comercio capturados por OTROS
     * teléfonos (los propios ya sonaron localmente al capturarse).
     */
    fun escucharPagos(alNuevoPago: (Pago) -> Unit): ListenerRegistration? {
        val uid = Sesion.uid ?: return null
        val comercioId = _comercio.value?.id ?: return null
        val desde = System.currentTimeMillis()
        return db.collection("comercios").document(comercioId)
            .collection("pagos")
            .whereGreaterThan("timestamp", desde)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { captura, _ ->
                captura?.documentChanges?.forEach { cambio ->
                    val datos = cambio.document.data
                    if (datos["origenUid"] == uid) return@forEach
                    alNuevoPago(
                        Pago(
                            billeteraId = datos["billeteraId"] as? String ?: "?",
                            billeteraNombre = datos["billeteraNombre"] as? String ?: "Pago",
                            pagador = datos["pagador"] as? String ?: "Alguien",
                            monto = (datos["monto"] as? Number)?.toDouble() ?: return@forEach,
                            timestamp = (datos["timestamp"] as? Number)?.toLong()
                                ?: System.currentTimeMillis(),
                        )
                    )
                }
            }
    }
}
