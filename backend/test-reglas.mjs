// Prueba de reglas de PagoYa contra el emulador de Firestore.
// Replica EXACTAMENTE las operaciones que hace la app Android, y ataca las
// reglas como lo haría alguien que quiere inventar pagos falsos.
import { initializeTestEnvironment, assertSucceeds, assertFails } from "@firebase/rules-unit-testing";
import { readFileSync } from "node:fs";
import {
  doc, setDoc, updateDoc, deleteDoc, getDoc, collection, collectionGroup,
  query, orderBy, limit, getDocs, serverTimestamp, writeBatch, increment,
} from "firebase/firestore";

const env = await initializeTestEnvironment({
  projectId: "pagoya-test",
  firestore: { rules: readFileSync("firestore.rules", "utf8") },
});

let fallos = 0;
async function prueba(nombre, promesa) {
  try {
    await promesa;
    console.log(`  ✅ ${nombre}`);
  } catch (e) {
    fallos++;
    console.log(`  ❌ ${nombre}\n     ${String(e.message).split("\n")[0]}`);
  }
}

const DUENO = "uid-dueno";
const TRABAJADOR = "uid-trabajador";
const INTRUSO = "uid-intruso";
const OPERADOR = "uid-operador";      // nivel dueño: administra el equipo
const EMPLEADO = "uid-empleado";      // nivel operador: no toca el equipo
const dbDueno = env.authenticatedContext(DUENO).firestore();
const dbTrabajador = env.authenticatedContext(TRABAJADOR).firestore();
const dbIntruso = env.authenticatedContext(INTRUSO).firestore();
const dbOperador = env.authenticatedContext(OPERADOR).firestore();
const dbEmpleado = env.authenticatedContext(EMPLEADO).firestore();

// El PRIMER dueño se crea a mano en la consola de Firebase; de ahí en adelante
// los da de alta él desde el panel. Aquí se siembra saltando las reglas.
await env.withSecurityRulesDisabled(async (ctx) => {
  // ctx.firestore() solo admite UNA llamada: la segunda intenta reconfigurar
  // una instancia ya iniciada y revienta con "Firestore has already been started".
  const db = ctx.firestore();
  await setDoc(doc(db, "operadores", OPERADOR), {
    nombre: "Yo", nivel: "dueno", activo: true,
  });
  await setDoc(doc(db, "operadores", EMPLEADO), {
    nombre: "Maria", nivel: "operador", activo: true,
  });
});

const comercioId = "comercio-test-1";
const codigo = "123456";

/**
 * Escribe un pago igual que ComercioRepo.subirPago: id determinista
 * `uid-timestamp-centavos` y hora sellada por el servidor.
 */
function subirPago(db, uid, cid, opciones = {}) {
  const {
    monto = 25.5,
    timestamp = Date.now(),
    recibidoEn = serverTimestamp(),
    id,
    extra = {},
    origenUid = uid,
  } = opciones;
  const centavos = Math.round(monto * 100);
  const pagoId = id ?? `${uid}-${timestamp}-${centavos}`;
  return setDoc(doc(db, "comercios", cid, "pagos", pagoId), {
    billeteraId: "yape",
    billeteraNombre: "Yape",
    pagador: "Maria",
    monto,
    timestamp,
    origenUid,
    recibidoEn,
    ...extra,
  });
}

console.log("\n— Flujo del DUEÑO: crear comercio (pasos secuenciales de la app) —");
await prueba("[comercio] crear", assertSucceeds(
  setDoc(doc(dbDueno, "comercios", comercioId), {
    nombre: "Bodega Test", duenoUid: DUENO, codigoVinculacion: codigo, creadoEn: 111,
    numDispositivos: 1,
  })
));
await prueba("[comercio] NO nace con numDispositivos != 1", assertFails(
  setDoc(doc(dbDueno, "comercios", "comercio-inflado"), {
    nombre: "Trampa", duenoUid: DUENO, codigoVinculacion: "777777", creadoEn: 1,
    numDispositivos: 10,
  })
));
await prueba("[miembro-dueno] crear (captura habilitada)", assertSucceeds(
  setDoc(doc(dbDueno, "comercios", comercioId, "miembros", DUENO), {
    rol: "dueno", nombre: "Don Richar", puedeCapturar: true,
  })
));
await prueba("[codigo] crear", assertSucceeds(
  setDoc(doc(dbDueno, "codigos", codigo), { comercioId })
));
await prueba("[perfil] crear", assertSucceeds(
  setDoc(doc(dbDueno, "usuarios", DUENO), { comercioId })
));

console.log("\n— TOPE DE DISPOSITIVOS: el plan gratis solo admite 1 teléfono —");
const MES0 = 30 * 86_400_000;
// El comercio nace en plan gratis (sin suscripción) → tope 1 (solo el dueño).
// Un trabajador NO puede entrar aunque el lote esté bien formado.
await prueba("comercio gratis NO admite un 2do teléfono", assertFails(
  (() => {
    const b = writeBatch(dbTrabajador);
    b.set(doc(dbTrabajador, "comercios", comercioId, "miembros", TRABAJADOR), {
      rol: "trabajador", nombre: "El sobrino", puedeCapturar: false,
    });
    b.update(doc(dbTrabajador, "comercios", comercioId), { numDispositivos: increment(1) });
    return b.commit();
  })()
));
// PRUEBA DE 30 DÍAS VENCIDA: aunque `plan` siga diciendo "caserito", si la
// fecha ya pasó el plan EFECTIVO es gratis (tope 1) → el 2do teléfono se
// rechaza. La degradación la hace la propia `vigenteHasta`, sin Cloud
// Functions. Es el caso canónico del "día 31 sin pagar".
await prueba("operador deja una prueba caserito YA vencida", assertSucceeds(
  updateDoc(doc(dbOperador, "comercios", comercioId), {
    suscripcion: {
      plan: "caserito", estado: "prueba", origen: "manual",
      vigenteHasta: Date.now() - MES0,  // venció hace 30 días
    },
  })
));
await prueba("prueba vencida NO admite un 2do teléfono (cae a gratis)", assertFails(
  (() => {
    const b = writeBatch(dbTrabajador);
    b.set(doc(dbTrabajador, "comercios", comercioId, "miembros", TRABAJADOR), {
      rol: "trabajador", nombre: "El sobrino", puedeCapturar: false,
    });
    b.update(doc(dbTrabajador, "comercios", comercioId), { numDispositivos: increment(1) });
    return b.commit();
  })()
));

// El operador sube el plan a Caserito VIGENTE (tope 3) → ahora sí hay cupo.
await prueba("operador activa Caserito (tope 3) para probar el cupo", assertSucceeds(
  updateDoc(doc(dbOperador, "comercios", comercioId), {
    suscripcion: {
      plan: "caserito", estado: "activa", origen: "manual",
      vigenteHasta: Date.now() + MES0,
    },
  })
));

console.log("\n— Flujo del DUEÑO: recargar app (cargar()) —");
await prueba("leer mi perfil", assertSucceeds(getDoc(doc(dbDueno, "usuarios", DUENO))));
await prueba("leer mi comercio", assertSucceeds(getDoc(doc(dbDueno, "comercios", comercioId))));
await prueba("leer mi miembro", assertSucceeds(getDoc(doc(dbDueno, "comercios", comercioId, "miembros", DUENO))));

console.log("\n— Flujo del DUEÑO: subir pago capturado —");
const pagoReal = Date.now();
await prueba("crear pago válido", assertSucceeds(
  subirPago(dbDueno, DUENO, comercioId, { timestamp: pagoReal })
));

console.log("\n— ANTI-FAKE: pagos que el servidor debe rechazar —");
const otroTs = Date.now();
await prueba("id que no cuadra con el monto (colar el mismo pago con otro id)", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { timestamp: otroTs, id: `${DUENO}-${otroTs}-9999` })
));
await prueba("id que no cuadra con el timestamp", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { timestamp: otroTs, id: `${DUENO}-${otroTs - 1}-2550` })
));
await prueba("venta fechada en el futuro", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { timestamp: Date.now() + 86_400_000 })
));
await prueba("venta enterrada en el pasado (más de 7 días)", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { timestamp: Date.now() - 8 * 86_400_000 })
));
await prueba("hora puesta por el teléfono en vez del servidor", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { recibidoEn: Date.now() })
));
await prueba("sin hora del servidor", assertFails(
  setDoc(doc(dbDueno, "comercios", comercioId, "pagos", `${DUENO}-${Date.now()}-2550`), {
    billeteraId: "yape", billeteraNombre: "Yape", pagador: "Maria",
    monto: 25.5, timestamp: Date.now(), origenUid: DUENO,
  })
));
await prueba("monto negativo", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { monto: -5 })
));
await prueba("monto absurdo (S/ 999,999)", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { monto: 999999 })
));
await prueba("campo colado que la app no manda", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { extra: { verificadoPorYape: true } })
));
await prueba("pago firmado a nombre de otro", assertFails(
  subirPago(dbDueno, DUENO, comercioId, { origenUid: TRABAJADOR })
));

console.log("\n— Flujo del TRABAJADOR: unirse con código —");
// Igual que ComercioRepo.unirseConCodigo: el miembro y el +1 al contador
// numDispositivos van en el MISMO lote atómico. Las reglas exigen esa coherencia.
function loteUnirse(db, cid, uid, datos, incremento = 1) {
  const b = writeBatch(db);
  b.set(doc(db, "comercios", cid, "miembros", uid), datos);
  if (incremento !== 0) {
    b.update(doc(db, "comercios", cid), { numDispositivos: increment(incremento) });
  }
  return b.commit();
}
await prueba("leer código", assertSucceeds(getDoc(doc(dbTrabajador, "codigos", codigo))));
await prueba("NO puede inscribirse con la captura habilitada", assertFails(
  loteUnirse(dbTrabajador, comercioId, TRABAJADOR, {
    rol: "trabajador", nombre: "El sobrino", puedeCapturar: true,
  })
));
await prueba("NO puede unirse sin subir el contador (saltarse el tope)", assertFails(
  setDoc(doc(dbTrabajador, "comercios", comercioId, "miembros", TRABAJADOR), {
    rol: "trabajador", nombre: "El sobrino", puedeCapturar: false,
  })
));
await prueba("inscribirme como trabajador (solo escucha, +1 al contador)", assertSucceeds(
  loteUnirse(dbTrabajador, comercioId, TRABAJADOR, {
    rol: "trabajador", nombre: "El sobrino", puedeCapturar: false,
  })
));
await prueba("guardar mi perfil", assertSucceeds(
  setDoc(doc(dbTrabajador, "usuarios", TRABAJADOR), { comercioId })
));
await prueba("leer el comercio ya siendo miembro", assertSucceeds(
  getDoc(doc(dbTrabajador, "comercios", comercioId))
));

console.log("\n— TOPE DE DISPOSITIVOS: llenar el cupo del plan Caserito (3) —");
// numDispositivos va en 2 (dueño + El sobrino). Caserito admite 3.
const TRAB2 = "uid-trabajador-2";
const TRAB3 = "uid-trabajador-3";
const dbTrab2 = env.authenticatedContext(TRAB2).firestore();
const dbTrab3 = env.authenticatedContext(TRAB3).firestore();
await prueba("3er teléfono SÍ entra (queda en el tope)", assertSucceeds(
  loteUnirse(dbTrab2, comercioId, TRAB2, {
    rol: "trabajador", nombre: "La caja", puedeCapturar: false,
  })
));
await prueba("4to teléfono NO entra (Caserito tope 3)", assertFails(
  loteUnirse(dbTrab3, comercioId, TRAB3, {
    rol: "trabajador", nombre: "El de más", puedeCapturar: false,
  })
));
await prueba("truco: subir el contador de +2 en un solo lote (rechazado)", assertFails(
  loteUnirse(dbTrab3, comercioId, TRAB3, {
    rol: "trabajador", nombre: "Tramposo", puedeCapturar: false,
  }, 2)
));
await prueba("truco: meter miembro sin tocar el contador (rechazado)", assertFails(
  loteUnirse(dbTrab3, comercioId, TRAB3, {
    rol: "trabajador", nombre: "Tramposo", puedeCapturar: false,
  }, 0)
));

console.log("\n— TOPE DE DISPOSITIVOS: al salir se devuelve el cupo —");
// La caja (TRAB2) se va: su doc y el -1 al contador en el mismo lote.
function loteSalir(db, cid, uid, decremento = -1) {
  const b = writeBatch(db);
  b.delete(doc(db, "comercios", cid, "miembros", uid));
  if (decremento !== 0) {
    b.update(doc(db, "comercios", cid), { numDispositivos: increment(decremento) });
  }
  return b.commit();
}
await prueba("salir sin bajar el contador (rechazado)", assertFails(
  deleteDoc(doc(dbTrab2, "comercios", comercioId, "miembros", TRAB2))
));
await prueba("salir bajando el contador -1 (aceptado)", assertSucceeds(
  loteSalir(dbTrab2, comercioId, TRAB2)
));
await prueba("tras liberar cupo, el 4to teléfono ahora SÍ entra", assertSucceeds(
  loteUnirse(dbTrab3, comercioId, TRAB3, {
    rol: "trabajador", nombre: "El de más", puedeCapturar: false,
  })
));
// Dejar el contador donde lo esperan los bloques siguientes: sacar a TRAB3.
await prueba("limpieza: TRAB3 sale (contador vuelve a 2)", assertSucceeds(
  loteSalir(dbTrab3, comercioId, TRAB3)
));

console.log("\n— TOPE DE DISPOSITIVOS: el OPERADOR quita un trabajador desde el panel —");
// Metemos un trabajador temporal para que el operador lo saque (contador 2→3→2).
await prueba("preparar: un trabajador temporal se une (contador → 3)", assertSucceeds(
  loteUnirse(dbTrab2, comercioId, TRAB2, {
    rol: "trabajador", nombre: "Temporal", puedeCapturar: false,
  })
));
await prueba("operador NO puede quitar al DUEÑO (desmantelaría el comercio)", assertFails(
  loteSalir(dbOperador, comercioId, DUENO)
));
await prueba("operador NO borra un trabajador sin bajar el contador", assertFails(
  deleteDoc(doc(dbOperador, "comercios", comercioId, "miembros", TRAB2))
));
await prueba("operador SÍ quita al trabajador con el -1 (soporte del panel)", assertSucceeds(
  loteSalir(dbOperador, comercioId, TRAB2)
));

console.log("\n— Flujo del TRABAJADOR: modo escucha (query real de la app) —");
await prueba("query de pagos por hora del servidor", assertSucceeds(
  getDocs(query(
    collection(dbTrabajador, "comercios", comercioId, "pagos"),
    orderBy("recibidoEn", "desc"),
    limit(30),
  ))
));

console.log("\n— ANTI-FAKE: el trabajador escucha, no inventa —");
await prueba("trabajador NO crea pagos", assertFails(
  subirPago(dbTrabajador, TRABAJADOR, comercioId)
));
await prueba("trabajador NO se auto-habilita la captura", assertFails(
  updateDoc(doc(dbTrabajador, "comercios", comercioId, "miembros", TRABAJADOR), {
    puedeCapturar: true,
  })
));
await prueba("dueño NO puede ascender a un trabajador a dueño", assertFails(
  updateDoc(doc(dbDueno, "comercios", comercioId, "miembros", TRABAJADOR), { rol: "dueno" })
));
await prueba("dueño SÍ habilita un 2do teléfono de captura", assertSucceeds(
  updateDoc(doc(dbDueno, "comercios", comercioId, "miembros", TRABAJADOR), {
    puedeCapturar: true,
  })
));
await prueba("ya habilitado, el trabajador SÍ sube pagos", assertSucceeds(
  subirPago(dbTrabajador, TRABAJADOR, comercioId)
));

console.log("\n— COMPATIBILIDAD: miembros creados antes del campo puedeCapturar —");
const VIEJO = "uid-dueno-viejo";
const dbViejo = env.authenticatedContext(VIEJO).firestore();
const comercioViejo = "comercio-legacy";
await prueba("comercio legacy: crear", assertSucceeds(
  setDoc(doc(dbViejo, "comercios", comercioViejo), {
    nombre: "Bodega Antigua", duenoUid: VIEJO, codigoVinculacion: "654321", creadoEn: 1,
    numDispositivos: 1,
  })
));
await prueba("miembro legacy: sin el campo puedeCapturar", assertSucceeds(
  setDoc(doc(dbViejo, "comercios", comercioViejo, "miembros", VIEJO), {
    rol: "dueno", nombre: "Doña Rosa",
  })
));
await prueba("dueño legacy sigue pudiendo capturar", assertSucceeds(
  subirPago(dbViejo, VIEJO, comercioViejo)
));

console.log("\n— SEGURIDAD: lo que un INTRUSO no debe poder —");
await prueba("intruso NO lee el comercio", assertFails(
  getDoc(doc(dbIntruso, "comercios", comercioId))
));
await prueba("intruso NO lee pagos", assertFails(
  getDocs(collection(dbIntruso, "comercios", comercioId, "pagos"))
));
await prueba("intruso NO se inscribe como dueño", assertFails(
  setDoc(doc(dbIntruso, "comercios", comercioId, "miembros", INTRUSO), {
    rol: "dueno", nombre: "Hacker", puedeCapturar: true,
  })
));
await prueba("intruso NO lista códigos", assertFails(
  getDocs(collection(dbIntruso, "codigos"))
));
await prueba("intruso NO crea un código hacia un comercio ajeno", assertFails(
  setDoc(doc(dbIntruso, "codigos", "999999"), { comercioId })
));
await prueba("intruso NO se roba el comercio (cambiar duenoUid)", assertFails(
  updateDoc(doc(dbIntruso, "comercios", comercioId), { duenoUid: INTRUSO })
));
await prueba("ni el dueño puede regalar su comercio por update", assertFails(
  updateDoc(doc(dbDueno, "comercios", comercioId), { duenoUid: INTRUSO })
));
await prueba("nadie edita un pago (inmutable)", assertFails(
  updateDoc(doc(dbDueno, "comercios", comercioId, "pagos", `${DUENO}-${pagoReal}-2550`), {
    monto: 999999,
  })
));
await prueba("nadie sobreescribe un pago con datos válidos", assertFails(
  setDoc(doc(dbDueno, "comercios", comercioId, "pagos", `${DUENO}-${pagoReal}-2550`), {
    billeteraId: "yape", billeteraNombre: "Yape", pagador: "Otro",
    monto: 25.5, timestamp: pagoReal, origenUid: DUENO, recibidoEn: serverTimestamp(),
  })
));
await prueba("nadie borra un pago (inmutable)", assertFails(
  deleteDoc(doc(dbDueno, "comercios", comercioId, "pagos", `${DUENO}-${pagoReal}-2550`))
));

console.log("\n— PANEL: el OPERADOR ve todo —");
await prueba("operador lee cualquier comercio", assertSucceeds(
  getDoc(doc(dbOperador, "comercios", comercioId))
));
await prueba("operador lista todos los comercios", assertSucceeds(
  getDocs(collection(dbOperador, "comercios"))
));
await prueba("operador lee los pagos (soporte y comercios mudos)", assertSucceeds(
  getDocs(query(
    collection(dbOperador, "comercios", comercioId, "pagos"),
    orderBy("recibidoEn", "desc"),
    limit(30),
  ))
));
await prueba("operador lee los miembros", assertSucceeds(
  getDocs(collection(dbOperador, "comercios", comercioId, "miembros"))
));

console.log("\n— PANEL: membresías —");
const MES = 30 * 86_400_000;
await prueba("operador activa el plan Caserito", assertSucceeds(
  updateDoc(doc(dbOperador, "comercios", comercioId), {
    suscripcion: {
      plan: "caserito", estado: "activa", origen: "manual",
      vigenteHasta: Date.now() + MES,
    },
  })
));
// La Function trialAlCrearComercio / canjearReferido escriben suscripciones con
// origen:'sistema' (saltan reglas con el Admin SDK). Un update POSTERIOR del
// operador que conserve ese origen debe seguir siendo válido para las reglas.
await prueba("suscripción con origen 'sistema' (escrita por operador) aceptada", assertSucceeds(
  updateDoc(doc(dbOperador, "comercios", comercioId), {
    suscripcion: {
      plan: "caserito", estado: "prueba", origen: "sistema",
      vigenteHasta: Date.now() + MES,
    },
  })
));
await prueba("operador registra el cobro", assertSucceeds(
  setDoc(doc(dbOperador, "comercios", comercioId, "pagosMembresia", "cobro-1"), {
    monto: 12.9, metodo: "yape",
    periodoDesde: Date.now(), periodoHasta: Date.now() + MES,
    cobradoPor: OPERADOR, creadoEn: serverTimestamp(),
  })
));
await prueba("el dueño ve su propio recibo", assertSucceeds(
  getDocs(collection(dbDueno, "comercios", comercioId, "pagosMembresia"))
));
await prueba("operador NO renombra el comercio (eso es del dueño)", assertFails(
  updateDoc(doc(dbOperador, "comercios", comercioId), { nombre: "Otro nombre" })
));
await prueba("el dueño SÍ puede renombrar su comercio", assertSucceeds(
  updateDoc(doc(dbDueno, "comercios", comercioId), { nombre: "Bodega Rosita" })
));
// Privacidad de caja: solo el dueño la prende/apaga, y solo con un bool.
await prueba("el dueño apaga la privacidad de caja (trabajadorVeCaja=false)", assertSucceeds(
  updateDoc(doc(dbDueno, "comercios", comercioId), { trabajadorVeCaja: false })
));
await prueba("el dueño la vuelve a mostrar (trabajadorVeCaja=true)", assertSucceeds(
  updateDoc(doc(dbDueno, "comercios", comercioId), { trabajadorVeCaja: true })
));
await prueba("trabajadorVeCaja con valor no-bool rechazado", assertFails(
  updateDoc(doc(dbDueno, "comercios", comercioId), { trabajadorVeCaja: "si" })
));
await prueba("un trabajador NO cambia la privacidad de caja", assertFails(
  updateDoc(doc(dbTrabajador, "comercios", comercioId), { trabajadorVeCaja: false })
));

console.log("\n— PANEL: lo que el cliente NO puede hacer —");
await prueba("el dueño NO se activa su propio plan", assertFails(
  updateDoc(doc(dbDueno, "comercios", comercioId), {
    suscripcion: {
      plan: "patron", estado: "activa", origen: "manual",
      vigenteHasta: Date.now() + 10 * MES,
    },
  })
));
await prueba("nadie nace con membresía puesta", assertFails(
  setDoc(doc(dbIntruso, "comercios", "comercio-gratis"), {
    nombre: "Vivo", duenoUid: INTRUSO, codigoVinculacion: "111111", creadoEn: 1,
    suscripcion: {
      plan: "patron", estado: "activa", origen: "manual",
      vigenteHasta: Date.now() + 10 * MES,
    },
  })
));
await prueba("el dueño NO registra cobros de membresía", assertFails(
  setDoc(doc(dbDueno, "comercios", comercioId, "pagosMembresia", "cobro-falso"), {
    monto: 0.1, metodo: "yape",
    periodoDesde: Date.now(), periodoHasta: Date.now() + 10 * MES,
    cobradoPor: DUENO, creadoEn: serverTimestamp(),
  })
));
await prueba("un cobro no se edita (es contabilidad)", assertFails(
  updateDoc(doc(dbOperador, "comercios", comercioId, "pagosMembresia", "cobro-1"), {
    monto: 999,
  })
));
await prueba("un cobro no se borra", assertFails(
  deleteDoc(doc(dbOperador, "comercios", comercioId, "pagosMembresia", "cobro-1"))
));
await prueba("plan inventado rechazado", assertFails(
  updateDoc(doc(dbOperador, "comercios", comercioId), {
    suscripcion: {
      plan: "premium_vip", estado: "activa", origen: "manual",
      vigenteHasta: Date.now() + MES,
    },
  })
));
console.log("\n— EQUIPO: administrar operadores desde el panel —");
const NUEVO = "uid-nuevo-empleado";
await prueba("el dueño da de alta a un empleado", assertSucceeds(
  setDoc(doc(dbOperador, "operadores", NUEVO), {
    nombre: "Carlos", nivel: "operador", activo: true, creadoEn: serverTimestamp(),
  })
));
await prueba("el dueño lo desactiva (sin borrar el historial)", assertSucceeds(
  updateDoc(doc(dbOperador, "operadores", NUEVO), { activo: false })
));
await prueba("el dueño lo elimina", assertSucceeds(
  deleteDoc(doc(dbOperador, "operadores", NUEVO))
));
await prueba("un operador puede ver el equipo", assertSucceeds(
  getDocs(collection(dbEmpleado, "operadores"))
));

console.log("\n— EQUIPO: escalación de privilegios bloqueada —");
await prueba("un empleado NO da de alta a nadie", assertFails(
  setDoc(doc(dbEmpleado, "operadores", "uid-complice"), {
    nombre: "Complice", nivel: "operador", activo: true,
  })
));
await prueba("un empleado NO se asciende a dueño", assertFails(
  updateDoc(doc(dbEmpleado, "operadores", EMPLEADO), { nivel: "dueno" })
));
await prueba("un empleado NO se reactiva solo", assertFails(
  updateDoc(doc(dbEmpleado, "operadores", EMPLEADO), { activo: true })
));
await prueba("el dueño NO se edita a sí mismo (evita quedar sin dueño)", assertFails(
  updateDoc(doc(dbOperador, "operadores", OPERADOR), { nivel: "operador" })
));
await prueba("el dueño NO se borra a sí mismo", assertFails(
  deleteDoc(doc(dbOperador, "operadores", OPERADOR))
));
await prueba("nivel inventado rechazado", assertFails(
  setDoc(doc(dbOperador, "operadores", "uid-x"), {
    nombre: "X", nivel: "superadmin", activo: true,
  })
));
await prueba("nadie se asciende a operador", assertFails(
  setDoc(doc(dbIntruso, "operadores", INTRUSO), {
    nombre: "Hacker", nivel: "dueno", activo: true,
  })
));
await prueba("el dueño de un comercio NO se hace operador", assertFails(
  setDoc(doc(dbDueno, "operadores", DUENO), {
    nombre: "Yo mismo", nivel: "dueno", activo: true,
  })
));
await prueba("intruso NO lista comercios", assertFails(
  getDocs(collection(dbIntruso, "comercios"))
));
await prueba("intruso NO lista operadores", assertFails(
  getDocs(collection(dbIntruso, "operadores"))
));

console.log("\n— POSTULACIÓN → APROBACIÓN al equipo interno —");
// Un aspirante postula sin poder darse acceso solo. La solicitud NO concede
// permisos: el dueño la revisa y, aparte, crea operadores/{uid}.
const ASPIRANTE = "uid-aspirante";
const dbAspirante = env.authenticatedContext(ASPIRANTE).firestore();

// (a) el propio usuario crea su solicitud en estado "pendiente"
await prueba("(a) aspirante crea SU solicitud en pendiente", assertSucceeds(
  setDoc(doc(dbAspirante, "solicitudesOperador", ASPIRANTE), {
    uid: ASPIRANTE, email: "aspirante@correo.pe", nombre: "Juan Aspirante",
    notaRol: "Quiero ayudar en soporte", estado: "pendiente",
    creadoEn: serverTimestamp(),
  })
));
// (b) no puede autoasignarse "aceptada" en el create
await prueba("(b) NO puede crear su solicitud ya 'aceptada'", assertFails(
  setDoc(doc(env.authenticatedContext("uid-vivo").firestore(), "solicitudesOperador", "uid-vivo"), {
    uid: "uid-vivo", email: "vivo@correo.pe", nombre: "El Vivo",
    estado: "aceptada", creadoEn: serverTimestamp(),
  })
));
// (c) no puede postular en nombre de OTRO uid
await prueba("(c) NO puede crear la solicitud de OTRO uid", assertFails(
  setDoc(doc(dbAspirante, "solicitudesOperador", "uid-tercero"), {
    uid: "uid-tercero", email: "tercero@correo.pe", nombre: "Tercero",
    estado: "pendiente", creadoEn: serverTimestamp(),
  })
));
// (d) el postulante NO puede cambiar su propio estado a "aceptada"
await prueba("(d) postulante NO cambia su estado a 'aceptada'", assertFails(
  updateDoc(doc(dbAspirante, "solicitudesOperador", ASPIRANTE), { estado: "aceptada" })
));
// (e) un usuario normal NO se asciende creando operadores/{suUid} (regla existente)
await prueba("(e) aspirante NO se asciende creando operadores/{suUid}", assertFails(
  setDoc(doc(dbAspirante, "operadores", ASPIRANTE), {
    nombre: "Juan Aspirante", nivel: "operador", activo: true, creadoEn: serverTimestamp(),
  })
));
// (f) el operador/dueño lee la lista de solicitudes
await prueba("(f) operador lista todas las solicitudes", assertSucceeds(
  getDocs(collection(dbOperador, "solicitudesOperador"))
));
// (g) el dueño acepta: crea operadores/{uidPostulante} (paso aparte)
await prueba("(g) dueño acepta: crea operadores/{aspirante}", assertSucceeds(
  setDoc(doc(dbOperador, "operadores", ASPIRANTE), {
    nombre: "Juan Aspirante", nivel: "operador", activo: true, creadoEn: serverTimestamp(),
  })
));
// Extra: el operador SÍ mueve el estado; el aspirante ve su propia solicitud;
// un intruso no la lista; el propio aspirante retira su solicitud pendiente.
await prueba("(extra) operador marca la solicitud como aceptada", assertSucceeds(
  updateDoc(doc(dbOperador, "solicitudesOperador", ASPIRANTE), { estado: "aceptada" })
));
await prueba("(extra) el aspirante lee SU propia solicitud", assertSucceeds(
  getDoc(doc(dbAspirante, "solicitudesOperador", ASPIRANTE))
));
await prueba("(extra) intruso NO lista solicitudes", assertFails(
  getDocs(collection(dbIntruso, "solicitudesOperador"))
));
await prueba("(extra) operador NO reabre a estado raro", assertFails(
  updateDoc(doc(dbOperador, "solicitudesOperador", ASPIRANTE), { estado: "pendiente_vip" })
));
await prueba("(extra) el aspirante retira su solicitud pendiente", assertSucceeds(
  (async () => {
    const otro = env.authenticatedContext("uid-retira").firestore();
    await setDoc(doc(otro, "solicitudesOperador", "uid-retira"), {
      uid: "uid-retira", email: "retira@correo.pe", nombre: "Se Arrepiente",
      estado: "pendiente", creadoEn: serverTimestamp(),
    });
    return deleteDoc(doc(otro, "solicitudesOperador", "uid-retira"));
  })()
));
// (h) un postulante RECHAZADO vuelve a postular. El panel usa setDoc, que sobre
// un doc existente es un update rechazada→pendiente: debe PERMITIRSE (sin
// autoaceptarse). Se prepara con un uid propio que el operador rechaza primero.
const REPOSTULA = "uid-repostula";
const dbRepostula = env.authenticatedContext(REPOSTULA).firestore();
await prueba("(h-prep) el postulante crea su solicitud pendiente", assertSucceeds(
  setDoc(doc(dbRepostula, "solicitudesOperador", REPOSTULA), {
    uid: REPOSTULA, email: "repostula@correo.pe", nombre: "El Insistente",
    estado: "pendiente", creadoEn: serverTimestamp(),
  })
));
await prueba("(h-prep) el operador la RECHAZA", assertSucceeds(
  updateDoc(doc(dbOperador, "solicitudesOperador", REPOSTULA), { estado: "rechazada" })
));
await prueba("(h) rechazado RE-POSTULA (rechazada→pendiente, mismo uid)", assertSucceeds(
  updateDoc(doc(dbRepostula, "solicitudesOperador", REPOSTULA), { estado: "pendiente" })
));
// Reconfirmar: aun con la rama de re-postulación, el postulante NO se autoacepta,
// ni desde pendiente ni saltándose a "aceptada".
await prueba("(h) postulante NO se autoacepta (pendiente→aceptada)", assertFails(
  updateDoc(doc(dbRepostula, "solicitudesOperador", REPOSTULA), { estado: "aceptada" })
));

console.log("\n— TELEMETRÍA: el latido de salud de cada teléfono —");
/** Escribe un latido igual que TelemetriaRepo.subirLatido. */
function subirLatido(db, uid, cid, cambios = {}) {
  return setDoc(doc(db, "comercios", cid, "dispositivos", uid), {
    nombre: "Don Richar", capturando: true, listenerConectado: true,
    permisoEscucha: true, ultimaNotifBilletera: Date.now(), yape: "ok",
    puedeRevivir: true, bateriaLibre: true, marca: "Xiaomi",
    modelo: "Redmi Note 12", versionApp: "0.1.0",
    ultimoLatido: serverTimestamp(),
    ...cambios,
  });
}
await prueba("el dueño sube su latido", assertSucceeds(
  subirLatido(dbDueno, DUENO, comercioId)
));
await prueba("el trabajador sube el suyo", assertSucceeds(
  subirLatido(dbTrabajador, TRABAJADOR, comercioId, { capturando: false })
));
await prueba("nadie escribe el latido de OTRO teléfono", assertFails(
  subirLatido(dbDueno, TRABAJADOR, comercioId)
));
await prueba("intruso NO sube latidos", assertFails(
  subirLatido(dbIntruso, INTRUSO, comercioId)
));
await prueba("latido con campo colado rechazado", assertFails(
  subirLatido(dbDueno, DUENO, comercioId, { montoDelDia: 999 })
));
await prueba("latido con hora del teléfono (no del servidor) rechazado", assertFails(
  subirLatido(dbDueno, DUENO, comercioId, { ultimoLatido: Date.now() })
));
await prueba("estado de yape inventado rechazado", assertFails(
  subirLatido(dbDueno, DUENO, comercioId, { yape: "hackeada" })
));
await prueba("latido CON fcmToken válido (opcional) aceptado", assertSucceeds(
  subirLatido(dbDueno, DUENO, comercioId, { fcmToken: "token-fcm-valido-abc123" })
));
await prueba("latido con fcmToken no-string rechazado", assertFails(
  subirLatido(dbDueno, DUENO, comercioId, { fcmToken: 12345 })
));
await prueba("latido con fcmToken > 200 chars rechazado", assertFails(
  subirLatido(dbDueno, DUENO, comercioId, { fcmToken: "x".repeat(201) })
));
await prueba("el dueño ve los dispositivos de su comercio", assertSucceeds(
  getDocs(collection(dbDueno, "comercios", comercioId, "dispositivos"))
));
await prueba("el dueño limpia el latido de un miembro que se fue", assertSucceeds(
  deleteDoc(doc(dbDueno, "comercios", comercioId, "dispositivos", TRABAJADOR))
));

console.log("\n— TELEMETRÍA: el semáforo global del panel —");
await prueba("operador lee TODOS los dispositivos (collectionGroup)", assertSucceeds(
  getDocs(query(
    collectionGroup(dbOperador, "dispositivos"),
    orderBy("ultimoLatido", "desc"),
    limit(400),
  ))
));
await prueba("un comerciante NO puede usar la consulta global", assertFails(
  getDocs(query(collectionGroup(dbDueno, "dispositivos"), limit(10)))
));
await prueba("operador NO escribe latidos (solo los teléfonos)", assertFails(
  subirLatido(dbOperador, OPERADOR, comercioId)
));

console.log("\n— CONFIG: enlaces del producto (WhatsApp/redes) editables desde el panel —");
// El operador administra config/enlaces (WhatsApp de ventas + redes). La app,
// ya autenticada, lo lee para mostrar el WhatsApp. Nadie más lo escribe.
await prueba("operador escribe config/enlaces", assertSucceeds(
  setDoc(doc(dbOperador, "config", "enlaces"), {
    whatsappVentas: "51987654321",
    instagram: "https://instagram.com/pagoya.pe",
    tiktok: "https://tiktok.com/@pagoya.pe",
    facebook: "https://facebook.com/pagoya.pe",
  })
));
await prueba("operador puede sumar una red nueva sin cambiar reglas", assertSucceeds(
  updateDoc(doc(dbOperador, "config", "enlaces"), {
    web: "https://pagoya.pe", correoSoporte: "hola@pagoya.pe",
  })
));
await prueba("usuario autenticado (dueño) LEE los enlaces", assertSucceeds(
  getDoc(doc(dbDueno, "config", "enlaces"))
));
await prueba("trabajador autenticado LEE los enlaces", assertSucceeds(
  getDoc(doc(dbTrabajador, "config", "enlaces"))
));
await prueba("un usuario normal (no operador) NO escribe los enlaces", assertFails(
  setDoc(doc(dbDueno, "config", "enlaces"), { whatsappVentas: "51900000000" })
));
await prueba("intruso NO escribe los enlaces", assertFails(
  setDoc(doc(dbIntruso, "config", "enlaces"), { whatsappVentas: "51900000000" })
));
await prueba("campo con tipo inválido (whatsapp no-string) rechazado", assertFails(
  setDoc(doc(dbOperador, "config", "enlaces"), { whatsappVentas: 51987654321 })
));
await prueba("campo desconocido colado rechazado", assertFails(
  setDoc(doc(dbOperador, "config", "enlaces"), { whatsappVentas: "51987654321", loQueSea: "x" })
));
await prueba("nadie borra la config (ni el operador)", assertFails(
  deleteDoc(doc(dbOperador, "config", "enlaces"))
));

await env.cleanup();
console.log(fallos === 0 ? "\n🎉 TODAS LAS PRUEBAS PASARON" : `\n💥 ${fallos} PRUEBAS FALLARON`);
process.exit(fallos === 0 ? 0 : 1);
