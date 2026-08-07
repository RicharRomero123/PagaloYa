// Cloud Functions de PagoYa (firebase-functions v2, CommonJS).
//
// Filosofía de costos (plan Blaze, pero apuntando a NO salir del free tier):
//   - Funciones de baja frecuencia disparadas por evento (una por comercio
//     creado, una por pago capturado, callables de referido/campaña) MÁS una
//     programada de Modo ciego cada 2 min.
//   - Lo que YA vive gratis en las reglas de Firestore se queda ahí y NO se
//     migra a Functions: la degradación de plan por fecha (planEfectivo) y el
//     tope de dispositivos son puro Security Rules, sin invocaciones.
//   - Estimación de invocaciones/mes (ver README de functions más abajo):
//     con 20 comercios beta y ~150 pagos/comercio/mes ≈ 3 000 invocaciones por
//     eventos + la programada (cada 2 min = ~21 900/mes), contra 2 000 000
//     gratis. Sobra muchísimo margen. La programada hace un collection-group
//     query que en operación normal trae 0 docs (≈0 reads).
//
// Regla de oro anti-fake (CLAUDE.md): estas funciones NUNCA crean pagos. El
// fan-out solo LEE un pago que ya nació de una notificación real capturada por
// la app y lo reparte por FCM. Los premios de trial/referido tocan solo la
// suscripción, jamás la caja.

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue, Timestamp } = require("firebase-admin/firestore");
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

// ── Modo ciego (umbrales) ─────────────────────────────────────────────────────
// Defaults idénticos a los de Remote Config (ciego_*). El proyecto NO gestiona
// Remote Config desde backend/ (la app lo lee del lado cliente), así que la
// Function usa estos defaults locales como fuente de verdad del servidor. Si
// algún día se centraliza RC en el servidor, leerlos aquí con el Admin SDK.
const CIEGO_UMBRAL_SEG = 240; // sin presencia > 4 min ⇒ el capturador se calló.
const CIEGO_HORARIO_INICIO = 7; // hora local (America/Lima) en que empieza a vigilar.
const CIEGO_HORARIO_FIN = 22; // hora local en que deja de vigilar.

// Perú no usa horario de verano: America/Lima es UTC-5 fijo. Basta un offset
// constante para sacar la "hora local" sin arrastrar una lib de zonas horarias.
const LIMA_OFFSET_HORAS = -5;

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
    const inicioPrueba = Date.now();
    const vigenteHasta = inicioPrueba + TRIAL_DIAS * DIA_MS;

    // Mismo contrato EXACTO que siembra la app Android en ComercioRepo:
    //   { estado, plan, inicioPrueba, vigenteHasta }  (+ origen del lado servidor).
    // Así, venga el comercio de la app o de un alta servidor, la suscripción de
    // prueba es idéntica y planEfectivo() la degrada sola por fecha a los 30 días.
    await snap.ref.update({
      suscripcion: {
        estado: "prueba",
        plan: "caserito",
        inicioPrueba,
        vigenteHasta,
        // origen 'sistema' = sembrado por Function (no lo escribe la app). Las
        // reglas lo aceptan como opcional; queda como rastro de procedencia.
        origen: "sistema",
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

// ─────────────────────────────────────────────────────────────────────────────
// 4) enviarCampana — el operador dispara una campaña push a un topic desde el
//    panel web (antes se hacía a mano en la consola de Firebase; ver
//    PUSH-CAMPANAS.md §4, "Fase 2 con Blaze").
//
//    Callable v2. SOLO operadores: la server key / Admin SDK jamás puede vivir
//    en el navegador del panel (cualquiera abriría DevTools y spamearía a toda
//    la base), así que el envío autenticado va SIEMPRE detrás de esta Function.
//
//    Es un mensaje `notification` (NO data-only): así el sistema lo muestra
//    aunque la app esté cerrada, idéntico a la consola de Firebase. Ojo: esto NO
//    es un pago (regla de oro anti-fake). Un push de campaña nunca crea ni suena
//    como un pago; los pagos solo nacen de notificaciones reales capturadas.
//
//    Costo: 1 invocación por campaña, y se mandan poquísimas al mes → lejísimos
//    del free tier (2 000 000 invocaciones/mes gratis).
// ─────────────────────────────────────────────────────────────────────────────
const TOPICS_CAMPANA = ["todos", "plan_gratis", "plan_caserito", "plan_patron"];

exports.enviarCampana = onCall(opciones, async (request) => {
  // Requiere auth.
  const uid = request.auth && request.auth.uid;
  if (!uid) {
    throw new HttpsError(
      "unauthenticated",
      "Tienes que iniciar sesión para enviar campañas."
    );
  }

  // SOLO operadores activos. Se lee operadores/{uid} con el Admin SDK (salta las
  // reglas), igual que el panel confía en esa colección.
  const operadorSnap = await db.collection("operadores").doc(uid).get();
  if (!operadorSnap.exists || operadorSnap.get("activo") === false) {
    throw new HttpsError(
      "permission-denied",
      "Solo un operador puede enviar campañas."
    );
  }

  const datos = request.data || {};
  const titulo = datos.titulo;
  const cuerpo = datos.cuerpo;
  const topic = datos.topic;

  // Título: 1..60 caracteres.
  if (typeof titulo !== "string" || titulo.trim().length < 1 || titulo.length > 60) {
    throw new HttpsError(
      "invalid-argument",
      "El título va de 1 a 60 caracteres, casero. Ni vacío ni tan largo."
    );
  }

  // Cuerpo: 1..180 caracteres.
  if (typeof cuerpo !== "string" || cuerpo.trim().length < 1 || cuerpo.length > 180) {
    throw new HttpsError(
      "invalid-argument",
      "El mensaje va de 1 a 180 caracteres. Corto y al grano."
    );
  }

  // Topic: solo los que la app Android ya conoce.
  if (!TOPICS_CAMPANA.includes(topic)) {
    throw new HttpsError(
      "invalid-argument",
      "Elige un destino válido: todos, plan_gratis, plan_caserito o plan_patron."
    );
  }

  // Envío por FCM a topic. Mensaje `notification` (lo muestra el sistema aunque
  // la app esté cerrada). `pagoya_avisos` es el canal que la app ya crea.
  //
  // Si FCM falla (lo más común la primera vez: la "Firebase Cloud Messaging API
  // (V1)" deshabilitada en Google Cloud), el error crudo llegaría al panel como
  // un opaco "internal". Lo envolvemos en un HttpsError para que su `message` SÍ
  // viaje al cliente y se vea el motivo real (un throw normal se oculta como
  // INTERNAL; el message de un HttpsError sí se entrega).
  try {
    // DATA-ONLY (no `notification`): la app arma la notificación y además
    // guarda el aviso en su buzón (campanita). Con el foreground service vivo,
    // llega a onMessageReceived aunque la app esté en segundo plano — igual que
    // los pagos. Así el buzón captura TODAS las campañas, no solo en primer plano.
    await getMessaging().send({
      topic,
      data: { tipo: "campana", titulo, cuerpo },
      android: { priority: "high" },
    });
  } catch (err) {
    logger.error("campana: FCM falló", {
      codigo: err.code,
      mensaje: err.message,
    });
    throw new HttpsError(
      "internal",
      `No se pudo enviar por FCM: ${err.message}. ` +
        "Revisa que la API de Firebase Cloud Messaging esté habilitada en Google Cloud.",
      { codigoFcm: err.code || "desconocido" }
    );
  }

  // Historial / auditoría: qué se mandó, quién y cuándo.
  await db.collection("campanas").add({
    titulo,
    cuerpo,
    topic,
    operadorUid: uid,
    operadorNombre: operadorSnap.get("nombre") || "",
    enviadoEn: FieldValue.serverTimestamp(),
  });

  logger.info("campana: enviada", { topic, operadorUid: uid });

  return { ok: true };
});

// ─────────────────────────────────────────────────────────────────────────────
// 5) vigilarCiego — MODO CIEGO. Despierta apps CERRADAS cuando el teléfono
//    capturador deja de latir presencia (el dueño está lejos y el local se
//    quedó "sordo"), y lleva el registro de periodos ciegos para la futura
//    Garantía de Aviso.
//
//    Corre cada 2 min (onSchedule). Filosofía de costos: el trabajo pesado es un
//    collection-group query que SOLO trae capturadores VENCIDOS
//    (capturando==true AND ultimaPresencia < cutoff). En operación normal el
//    capturador late cada ~90 s, así que ese query devuelve 0 docs y la corrida
//    cuesta ~0 reads. NO escaneamos todos los comercios ni todos los
//    dispositivos: el índice hace el trabajo.
//
//    Idempotencia del periodo ciego: el doc vive en
//    comercios/{id}/periodosCiegos/{deviceUid} — el id ES el uid del capturador.
//    Así hay a lo sumo UN periodo por capturador. Se abre con create() (falla si
//    ya existe → no se duplica en cada corrida) y se marca `fin` al recuperar.
//    Si el doc anterior ya tiene `fin` (periodo pasado ya cerrado), se sobrescribe
//    con set() para arrancar el nuevo episodio limpio.
//
//    Anti-fake: esta Function NUNCA crea pagos. Solo lee telemetría de presencia
//    y manda avisos de "modo ciego"; jamás suena ni registra como un cobro.
// ─────────────────────────────────────────────────────────────────────────────

// Códigos de FCM que indican token muerto → se limpia del doc del dispositivo.
const CODIGOS_TOKEN_MUERTO = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
  "messaging/invalid-argument",
]);

// ¿El comercio tiene una MEMBRESÍA ACTIVA ahora? (misma lógica que planEfectivo
// de las reglas): la suscripción cuenta solo si su estado es prueba|activa Y su
// vigenteHasta (millis) todavía no pasó. El plan gratis o vencido NO entra al
// Modo ciego: es una garantía de tier pagado.
function membresiaActiva(comercioData, ahoraMs) {
  const s = (comercioData && comercioData.suscripcion) || {};
  const estado = s.estado || "vencida";
  const vigenteHasta = typeof s.vigenteHasta === "number" ? s.vigenteHasta : 0;
  const plan = s.plan || "gratis";
  return (
    (estado === "prueba" || estado === "activa") &&
    vigenteHasta >= ahoraMs &&
    plan !== "gratis"
  );
}

// Hora local (America/Lima, UTC-5 fijo) del instante dado.
function horaLima(fecha) {
  const utcHoras = fecha.getUTCHours() + fecha.getUTCMinutes() / 60;
  const local = (utcHoras + LIMA_OFFSET_HORAS + 24) % 24;
  return local;
}

function enHorarioNegocio(fecha) {
  const h = horaLima(fecha);
  return h >= CIEGO_HORARIO_INICIO && h < CIEGO_HORARIO_FIN;
}

// Reparte un data-message del Modo ciego a TODOS los dispositivos del comercio
// con token (incluido el dueño). Reutiliza el patrón de fanoutPago: multicast +
// limpieza de tokens muertos. Devuelve cuántos se enviaron.
async function avisarModoCiego(comercioId, data) {
  const dispositivosSnap = await db
    .collection("comercios")
    .doc(comercioId)
    .collection("dispositivos")
    .get();

  const destinos = [];
  dispositivosSnap.forEach((docSnap) => {
    const fcmToken = docSnap.get("fcmToken");
    if (typeof fcmToken === "string" && fcmToken.length > 0) {
      destinos.push({ token: fcmToken, ref: docSnap.ref });
    }
  });

  if (destinos.length === 0) return 0;

  const respuesta = await getMessaging().sendEachForMulticast({
    tokens: destinos.map((d) => d.token),
    data,
    android: { priority: "high" },
  });

  const limpiezas = [];
  respuesta.responses.forEach((r, i) => {
    if (r.success) return;
    const codigo = r.error && r.error.code;
    if (CODIGOS_TOKEN_MUERTO.has(codigo)) {
      limpiezas.push(destinos[i].ref.update({ fcmToken: FieldValue.delete() }));
    }
  });
  if (limpiezas.length > 0) await Promise.all(limpiezas);

  return respuesta.successCount;
}

exports.vigilarCiego = onSchedule(
  {
    ...opciones,
    schedule: "every 2 minutes",
    timeZone: "America/Lima",
    // Poca memoria: solo I/O ligero de Firestore + FCM.
    memory: "256MiB",
  },
  async () => {
    const ahora = new Date();
    const ahoraMs = ahora.getTime();
    const cutoff = Timestamp.fromMillis(ahoraMs - CIEGO_UMBRAL_SEG * 1000);

    // Caché de comercios ya resueltos en esta corrida (evita re-leer el doc del
    // comercio si tuviera varios capturadores/periodos).
    const cacheComercio = new Map();
    async function comercio(comercioId) {
      if (cacheComercio.has(comercioId)) return cacheComercio.get(comercioId);
      const snap = await db.collection("comercios").doc(comercioId).get();
      const datos = snap.exists ? snap.data() : null;
      cacheComercio.set(comercioId, datos);
      return datos;
    }

    let aperturas = 0;
    let cierres = 0;

    // ── A) Capturadores VENCIDOS → abrir periodo ciego + avisar ───────────────
    // Query barato: en operación normal devuelve 0 docs (presencia fresca).
    const vencidosSnap = await db
      .collectionGroup("dispositivos")
      .where("capturando", "==", true)
      .where("ultimaPresencia", "<", cutoff)
      .get();

    for (const dispDoc of vencidosSnap.docs) {
      // Ruta: comercios/{comercioId}/dispositivos/{uid}
      const comercioRef = dispDoc.ref.parent.parent;
      if (!comercioRef) continue;
      const comercioId = comercioRef.id;
      const deviceUid = dispDoc.id;

      // Gate 1: solo comercios con membresía activa (garantía de tier pagado).
      const datosComercio = await comercio(comercioId);
      if (!datosComercio || !membresiaActiva(datosComercio, ahoraMs)) continue;

      // Gate 2: solo en horario de negocio (abrir episodios nuevos). Fuera de
      // horario no avisamos ni abrimos: el local está cerrado a propósito.
      if (!enHorarioNegocio(ahora)) continue;

      // Minutos sin presencia (para el aviso y el registro).
      const ultimaPresencia = dispDoc.get("ultimaPresencia");
      const ultimaMs =
        ultimaPresencia && typeof ultimaPresencia.toMillis === "function"
          ? ultimaPresencia.toMillis()
          : ahoraMs;
      const minutos = Math.max(1, Math.floor((ahoraMs - ultimaMs) / 60000));

      const periodoRef = comercioRef
        .collection("periodosCiegos")
        .doc(deviceUid);
      const periodoSnap = await periodoRef.get();
      const yaAbierto =
        periodoSnap.exists && periodoSnap.get("fin") == null;

      if (!yaAbierto) {
        // Abrir un episodio nuevo. set() sobrescribe un periodo anterior YA
        // cerrado (con fin), o crea el primero. El id == deviceUid garantiza un
        // solo periodo abierto por capturador (idempotente entre corridas).
        await periodoRef.set({
          deviceUid,
          inicio: FieldValue.serverTimestamp(),
          fin: null,
          ultimaPresenciaAlAbrir: ultimaPresencia || null,
          minutosAlAbrir: minutos,
        });
        aperturas++;
      }

      // Avisar SIEMPRE (aunque el periodo ya estuviera abierto): así, si el
      // primer push se perdió, el dueño sigue enterándose en la próxima corrida.
      // La app deduplica su banner por comercioId.
      await avisarModoCiego(comercioId, {
        tipo: "ciego",
        comercioId,
        minutos: String(minutos),
      });
    }

    // ── B) Periodos ciegos ABIERTOS → ¿se recuperó la presencia? cerrar + avisar
    // Query barato: normalmente 0 periodos abiertos.
    const abiertosSnap = await db
      .collectionGroup("periodosCiegos")
      .where("fin", "==", null)
      .get();

    for (const perDoc of abiertosSnap.docs) {
      const comercioRef = perDoc.ref.parent.parent;
      if (!comercioRef) continue;
      const comercioId = comercioRef.id;
      const deviceUid = perDoc.id;

      // Releer el dispositivo del capturador para ver su presencia actual.
      const dispSnap = await comercioRef
        .collection("dispositivos")
        .doc(deviceUid)
        .get();

      const capturando = dispSnap.exists ? dispSnap.get("capturando") : false;
      const ultimaPresencia = dispSnap.exists
        ? dispSnap.get("ultimaPresencia")
        : null;
      const presenciaFresca =
        ultimaPresencia &&
        typeof ultimaPresencia.toMillis === "function" &&
        ultimaPresencia.toMillis() >= cutoff.toMillis();

      // Recuperado si: volvió a latir presencia fresca, o dejó de capturar (el
      // negocio cerró la jornada). En ambos casos el "modo ciego" terminó.
      const recuperado = presenciaFresca || capturando === false;
      if (!recuperado) continue;

      await perDoc.ref.update({ fin: FieldValue.serverTimestamp() });
      cierres++;

      // Avisar fin de modo ciego a todos los dispositivos del comercio.
      await avisarModoCiego(comercioId, {
        tipo: "ciego_fin",
        comercioId,
      });
    }

    logger.info("vigilarCiego: corrida", {
      vencidos: vencidosSnap.size,
      aperturas,
      abiertos: abiertosSnap.size,
      cierres,
    });
  }
);
