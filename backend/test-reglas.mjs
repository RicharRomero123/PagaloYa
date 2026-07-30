// Prueba de reglas de PagoYa contra el emulador de Firestore.
// Replica EXACTAMENTE las operaciones que hace la app Android.
import { initializeTestEnvironment, assertSucceeds, assertFails } from "@firebase/rules-unit-testing";
import { readFileSync } from "node:fs";
import { doc, setDoc, getDoc, collection, query, where, orderBy, getDocs } from "firebase/firestore";

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

console.log("\n— Flujo del DUEÑO: crear comercio (pasos secuenciales de la app) —");
await prueba("[comercio] crear", assertSucceeds(
  setDoc(doc(dbDueno, "comercios", comercioId), {
    nombre: "Bodega Test", duenoUid: DUENO, codigoVinculacion: codigo, creadoEn: 111,
  })
));
await prueba("[miembro-dueno] crear", assertSucceeds(
  setDoc(doc(dbDueno, "comercios", comercioId, "miembros", DUENO), {
    rol: "dueno", nombre: "Don Richar",
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
await prueba("crear pago", assertSucceeds(
  setDoc(doc(dbDueno, "comercios", comercioId, "pagos", `${DUENO}-111-2550`), {
    billeteraId: "yape", billeteraNombre: "Yape", pagador: "Maria",
    monto: 25.5, timestamp: Date.now() + 1000, origenUid: DUENO,
  })
));

console.log("\n— Flujo del TRABAJADOR: unirse con código —");
await prueba("leer código", assertSucceeds(getDoc(doc(dbTrabajador, "codigos", codigo))));
await prueba("inscribirme como trabajador", assertSucceeds(
  setDoc(doc(dbTrabajador, "comercios", comercioId, "miembros", TRABAJADOR), {
    rol: "trabajador", nombre: "El sobrino",
  })
));
await prueba("guardar mi perfil", assertSucceeds(
  setDoc(doc(dbTrabajador, "usuarios", TRABAJADOR), { comercioId })
));
await prueba("leer el comercio ya siendo miembro", assertSucceeds(
  getDoc(doc(dbTrabajador, "comercios", comercioId))
));

console.log("\n— Flujo del TRABAJADOR: modo escucha (query de pagos) —");
await prueba("query pagos nuevos", assertSucceeds(
  getDocs(query(
    collection(dbTrabajador, "comercios", comercioId, "pagos"),
    where("timestamp", ">", 0),
    orderBy("timestamp", "asc"),
  ))
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
    rol: "dueno", nombre: "Hacker",
  })
));
await prueba("intruso NO lista códigos", assertFails(
  getDocs(collection(dbIntruso, "codigos"))
));
await prueba("nadie edita un pago (inmutable)", assertFails(
  setDoc(doc(dbDueno, "comercios", comercioId, "pagos", `${DUENO}-111-2550`), {
    monto: 999999,
  })
));

await env.cleanup();
console.log(fallos === 0 ? "\n🎉 TODAS LAS PRUEBAS PASARON" : `\n💥 ${fallos} PRUEBAS FALLARON`);
process.exit(fallos === 0 ? 0 : 1);
