// Prueba de reglas de PagoYa contra el emulador de Firestore.
// Replica EXACTAMENTE las operaciones que hace la app Android, y ataca las
// reglas como lo haría alguien que quiere inventar pagos falsos.
import { initializeTestEnvironment, assertSucceeds, assertFails } from "@firebase/rules-unit-testing";
import { readFileSync } from "node:fs";
import {
  doc, setDoc, updateDoc, deleteDoc, getDoc, collection, query, orderBy, limit,
  getDocs, serverTimestamp,
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
const dbDueno = env.authenticatedContext(DUENO).firestore();
const dbTrabajador = env.authenticatedContext(TRABAJADOR).firestore();
const dbIntruso = env.authenticatedContext(INTRUSO).firestore();

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
await prueba("leer código", assertSucceeds(getDoc(doc(dbTrabajador, "codigos", codigo))));
await prueba("NO puede inscribirse con la captura habilitada", assertFails(
  setDoc(doc(dbTrabajador, "comercios", comercioId, "miembros", TRABAJADOR), {
    rol: "trabajador", nombre: "El sobrino", puedeCapturar: true,
  })
));
await prueba("inscribirme como trabajador (solo escucha)", assertSucceeds(
  setDoc(doc(dbTrabajador, "comercios", comercioId, "miembros", TRABAJADOR), {
    rol: "trabajador", nombre: "El sobrino", puedeCapturar: false,
  })
));
await prueba("guardar mi perfil", assertSucceeds(
  setDoc(doc(dbTrabajador, "usuarios", TRABAJADOR), { comercioId })
));
await prueba("leer el comercio ya siendo miembro", assertSucceeds(
  getDoc(doc(dbTrabajador, "comercios", comercioId))
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

await env.cleanup();
console.log(fallos === 0 ? "\n🎉 TODAS LAS PRUEBAS PASARON" : `\n💥 ${fallos} PRUEBAS FALLARON`);
process.exit(fallos === 0 ? 0 : 1);
