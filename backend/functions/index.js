// Cloud Functions de PagoYa (firebase-functions v2, CommonJS).
//
// Filosofía de costos (plan Blaze, pero apuntando a NO salir del free tier):
//   - Solo TRES funciones, todas de baja frecuencia (una por comercio creado,
//     una por pago capturado, una por referido canjeado).
//   - Lo que YA vive gratis en las reglas de Firestore se queda ahí y NO se
//     migra a Functions: la degradación de plan por fecha (planEfectivo) y el
//     tope de dispositivos son puro Security Rules, sin invocaciones.
//   - Estimación de invocaciones/mes (ver README de functions más abajo):
//     con 20 comercios beta y ~150 pagos/comercio/mes ≈ 3 000 invocaciones/mes,
//     contra 2 000 000 gratis. Sobra muchísimo margen.
//
// Regla de oro anti-fake (CLAUDE.md): estas funciones NUNCA crean pagos. El
// fan-out solo LEE un pago que ya nació de una notificación real capturada por
// la app y lo reparte por FCM. Los premios de trial/referido tocan solo la
// suscripción, jamás la caja.

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const logger = require("firebase-functions/logger");

initializeApp();
const db = getFirestore();

// Todas las funciones viven cerca del cliente (Perú → us-central1 es el default
// de FCM/Firestore y el más barato/rápido para la región).
const REGION = "us-central1";
const opciones = { region: REGION };

const DIA_MS = 24 * 60 * 60 * 1000;
const TRIAL_DIAS = 30;
const PREMIO_REFERIDO_DIAS = 15;

// ─────────────────────────────────────────────────────────────────────────────
// 1) trialAlCrearComercio — regala 30 días de Caserito al nacer un comercio.
//
//    onCreate dispara UNA sola vez por comercio → idempotente por diseño. Aun
//    así, si por cualquier motivo el doc ya trae `suscripcion` (p. ej. una
//    migración que la sembró), NO la pisamos.
// ─────────────────────────────────────────────────────────────────────────────
exports.trialAlCrearComercio = onDocumentCreated(
  { ...opciones, document: "comercios/{comercioId}" },
  async (event) => {
    const snap = event.data;
    if (!snap) return; // sin datos (borrado en carrera): nada que hacer.

    const datos = snap.data() || {};
    if (datos.suscripcion) {
      // Ya tiene plan (operador o migración lo puso): respetarlo.
      logger.info("trial: comercio ya tiene suscripcion, no se toca", {
        comercioId: event.params.comercioId,
      });
      return;
    }

    // Date.now() aquí SÍ se puede: es runtime de Function (servidor), no cliente.
    const vigenteHasta = Date.now() + TRIAL_DIAS * DIA_MS;

    await snap.ref.update({
      suscripcion: {
        plan: "caserito",
        estado: "prueba",
        origen: "sistema",
        vigenteHasta,
      },
    });

    logger.info("trial: 30 días de Caserito otorgados", {
      comercioId: event.params.comercioId,
      vigenteHasta,
    });
  }
);

// ─────────────────────────────────────────────────────────────────────────────
// 2) fanoutPago — despierta a los teléfonos "escucha" por FCM cuando entra un
//    pago. Data-only (sin notification) para que la app lo maneje y lo HABLE.
//
//    Nota de costo: hoy lee N docs de comercios/{id}/dispositivos por cada pago
//    para sacar los tokens. Perfecto para la beta (10-20 comercios, pocos
//    teléfonos c/u). Si algún día pesa, migrar a topics FCM `comercio_{id}`
//    (la app se suscribe y FCM reparte sin que leamos tokens). Ver PUSH-CAMPANAS.md.
// ─────────────────────────────────────────────────────────────────────────────
exports.fanoutPago = onDocumentCreated(
  { ...opciones, document: "comercios/{comercioId}/pagos/{pagoId}" },
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const { comercioId, pagoId } = event.params;
    const pago = snap.data() || {};
    const origenUid = pago.origenUid || "";

    // Leer los latidos de dispositivos: de cada uno sacamos su fcmToken (si lo
    // tiene) y su uid (= id del doc). Al que capturó (origenUid) NO se le manda:
    // ese teléfono ya sonó local.
    const dispositivosSnap = await db
      .collection("comercios")
      .doc(comercioId)
      .collection("dispositivos")
      .get();

    // token → ref del doc (para poder limpiarlo si está muerto).
    const destinos = [];
    dispositivosSnap.forEach((docSnap) => {
      if (docSnap.id === origenUid) return; // el que capturó ya escuchó local.
      const fcmToken = docSnap.get("fcmToken");
      if (typeof fcmToken === "string" && fcmToken.length > 0) {
        destinos.push({ token: fcmToken, ref: docSnap.ref });
      }
    });

    if (destinos.length === 0) {
      logger.info("fanout: sin escuchas con token, nada que enviar", {
        comercioId,
        pagoId,
      });
      return;
    }

    // Contrato EXACTO con la app (todos los valores string). Data-only: sin
    // `notification`, para que la app lo procese en segundo plano y hable.
    const data = {
      tipo: "pago",
      comercioId,
      pagoId,
      billeteraId: String(pago.billeteraId ?? ""),
      billeteraNombre: String(pago.billeteraNombre ?? ""),
      pagador: String(pago.pagador ?? ""),
      monto: String(pago.monto ?? ""),
      timestamp: String(pago.timestamp ?? ""),
      origenUid: String(origenUid),
    };

    const mensaje = {
      tokens: destinos.map((d) => d.token),
      data,
      android: { priority: "high" },
    };

    const respuesta = await getMessaging().sendEachForMulticast(mensaje);

    logger.info("fanout: enviado", {
      comercioId,
      pagoId,
      enviados: destinos.length,
      exitos: respuesta.successCount,
      fallos: respuesta.failureCount,
    });

    // Limpieza de tokens muertos: si FCM dice que un token ya no está
    // registrado o es inválido, lo borramos del doc del dispositivo para no
    // seguir gastando en enviarle.
    const CODIGOS_TOKEN_MUERTO = new Set([
      "messaging/registration-token-not-registered",
      "messaging/invalid-registration-token",
      "messaging/invalid-argument",
    ]);

    const limpiezas = [];
    respuesta.responses.forEach((r, i) => {
      if (r.success) return;
      const codigo = r.error && r.error.code;
      if (CODIGOS_TOKEN_MUERTO.has(codigo)) {
        limpiezas.push(
          destinos[i].ref.update({ fcmToken: FieldValue.delete() })
        );
      } else if (r.error) {
        logger.warn("fanout: fallo de envío no fatal", {
          comercioId,
          codigo,
        });
      }
    });

    if (limpiezas.length > 0) {
      await Promise.all(limpiezas);
      logger.info("fanout: tokens muertos limpiados", {
        comercioId,
        limpiados: limpiezas.length,
      });
    }
  }
);

// ─────────────────────────────────────────────────────────────────────────────
// 3) canjearReferido — programa "Casero trae Casero" (+15 días a ambos).
//
//    Callable v2. Lo llama el DUEÑO del comercio REFERIDO, con el código de 6
//    dígitos del comercio REFERIDOR. Premio atómico (batch): +15 días de
//    Caserito a AMBOS comercios, empujando desde el máximo entre "ahora" y su
//    vigencia actual (no se quita tiempo a nadie).
// ─────────────────────────────────────────────────────────────────────────────
exports.canjearReferido = onCall(opciones, async (request) => {
  // Requiere auth.
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError(
      "unauthenticated",
      "Tienes que iniciar sesión para canjear un código."
    );
  }

  const codigo = request.data && request.data.codigo;
  if (typeof codigo !== "string" || codigo.trim().length === 0) {
    throw new HttpsError(
      "invalid-argument",
      "Falta el código del casero que te invitó."
    );
  }
  const codigoLimpio = codigo.trim();

  // Resolver el comercio del que llama (el REFERIDO) vía usuarios/{uid}.comercioId.
  const usuarioSnap = await db.collection("usuarios").doc(uid).get();
  const referidoId = usuarioSnap.exists ? usuarioSnap.get("comercioId") : null;
  if (!referidoId) {
    throw new HttpsError(
      "failed-precondition",
      "Todavía no tienes un negocio en PagoYa."
    );
  }

  const referidoRef = db.collection("comercios").doc(referidoId);
  const referidoSnap = await referidoRef.get();
  if (!referidoSnap.exists) {
    throw new HttpsError(
      "failed-precondition",
      "No encontramos tu negocio."
    );
  }
  // Validar que quien llama sea el DUEÑO del comercio referido.
  if (referidoSnap.get("duenoUid") !== uid) {
    throw new HttpsError(
      "permission-denied",
      "Solo el dueño del negocio puede canjear un código."
    );
  }

  // El comercio referido no debe haber canjeado ya un referido.
  if (referidoSnap.get("referidoCanjeado") === true) {
    throw new HttpsError(
      "already-exists",
      "Ya usaste un código de referido antes, casero. Es una sola vez."
    );
  }

  // Resolver codigo → comercio REFERIDOR.
  const codigoSnap = await db.collection("codigos").doc(codigoLimpio).get();
  const referidorId = codigoSnap.exists ? codigoSnap.get("comercioId") : null;
  if (!referidorId) {
    throw new HttpsError(
      "not-found",
      "Ese código no existe. Revísalo con tu casero."
    );
  }

  // No auto-referirse.
  if (referidorId === referidoId) {
    throw new HttpsError(
      "failed-precondition",
      "No puedes referirte a ti mismo, vivo."
    );
  }

  const referidorRef = db.collection("comercios").doc(referidorId);
  const referidorSnap = await referidorRef.get();
  if (!referidorSnap.exists) {
    throw new HttpsError(
      "not-found",
      "El negocio de ese código ya no existe."
    );
  }

  // Premio: empujar la suscripción de AMBOS a Caserito, +15 días desde el
  // máximo entre "ahora" y lo que ya tuvieran vigente (no se recorta tiempo).
  const ahora = Date.now();
  const premioMs = PREMIO_REFERIDO_DIAS * DIA_MS;

  const nuevaVigencia = (snap) => {
    const susc = snap.get("suscripcion") || {};
    const vigenteActual =
      typeof susc.vigenteHasta === "number" ? susc.vigenteHasta : 0;
    return Math.max(ahora, vigenteActual) + premioMs;
  };

  const suscReferidor = {
    plan: "caserito",
    estado: "prueba",
    origen: "sistema",
    vigenteHasta: nuevaVigencia(referidorSnap),
  };
  const suscReferido = {
    plan: "caserito",
    estado: "prueba",
    origen: "sistema",
    vigenteHasta: nuevaVigencia(referidoSnap),
  };

  const batch = db.batch();
  batch.update(referidorRef, { suscripcion: suscReferidor });
  batch.update(referidoRef, {
    suscripcion: suscReferido,
    referidoCanjeado: true,
    referidoDe: referidorId,
  });
  await batch.commit();

  logger.info("referido: canjeado", { referidorId, referidoId });

  return { ok: true, mensaje: "¡Ganaste 15 días, casero!" };
});
