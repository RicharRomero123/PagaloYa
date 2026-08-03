import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  serverTimestamp,
  setDoc,
  updateDoc,
  type DocumentData,
} from "firebase/firestore";
import { auth, db } from "./firebase";
import { agregarOperador, type NivelOperador } from "./operadores";

/**
 * Postulación → aprobación para que el equipo interno se sume solo, sin que el
 * dueño ande copiando UIDs a mano y sin que nadie se autoconceda acceso.
 *
 * El que quiere entrar deja su solicitud (create solo de su propio uid, y
 * siempre nace "pendiente" — así lo blindan las reglas). El dueño la revisa y
 * decide: aceptar (crea el operador) o rechazar.
 *
 * Decisión marcar-vs-borrar: NO borramos, marcamos estado. Razones:
 *  - Al aceptar, dejamos la solicitud en "aceptada" para tener rastro de quién
 *    entró y cuándo, sin necesitar Admin SDK ni logs aparte.
 *  - Al rechazar, la marcamos "rechazada" en vez de borrarla para que el mismo
 *    postulante vea "te rechazaron" y no quede en un limbo pensando que nunca
 *    se envió. Si vuelve a postular, el setDoc pisa el doc y lo regresa a
 *    "pendiente" (create del propio uid), así que puede reintentar.
 *  Limpiar el historial, si algún día estorba, se hace desde la consola.
 */

export type EstadoSolicitud = "pendiente" | "aceptada" | "rechazada";

export type Solicitud = {
  uid: string;
  email: string;
  nombre: string;
  notaRol: string;
  estado: EstadoSolicitud;
};

const COL = "solicitudesOperador";

/** Se dispara cuando las reglas aún no están desplegadas (o no dejan tocar la
 *  colección). Lo usamos para dar un mensaje humano en la UI. */
export class ReglasNoListas extends Error {
  constructor() {
    super("Las reglas de acceso aún no están activas.");
    this.name = "ReglasNoListas";
  }
}

function esPermisoDenegado(e: unknown): boolean {
  return (
    typeof e === "object" &&
    e !== null &&
    "code" in e &&
    (e as { code?: string }).code === "permission-denied"
  );
}

function aSolicitud(uid: string, datos: DocumentData): Solicitud {
  return {
    uid,
    email: (datos.email as string) ?? "",
    nombre: (datos.nombre as string) ?? "Sin nombre",
    notaRol: (datos.notaRol as string) ?? "",
    estado: (datos.estado as EstadoSolicitud) ?? "pendiente",
  };
}

/**
 * Deja (o repone) la solicitud del usuario que tiene la sesión abierta. Siempre
 * nace "pendiente": el que postula no elige su rol, solo lo sugiere en la nota.
 */
export async function postular({
  nombre,
  notaRol,
}: {
  nombre: string;
  notaRol?: string;
}): Promise<void> {
  const u = auth.currentUser;
  if (!u) throw new Error("Necesitas haber iniciado sesión para postular.");
  try {
    await setDoc(doc(db, COL, u.uid), {
      uid: u.uid,
      email: u.email ?? "",
      nombre: nombre.trim(),
      notaRol: (notaRol ?? "").trim(),
      estado: "pendiente",
      creadoEn: serverTimestamp(),
    });
  } catch (e) {
    if (esPermisoDenegado(e)) throw new ReglasNoListas();
    throw e;
  }
}

/** Lo que el propio usuario postuló (o null si nunca postuló). */
export async function miSolicitud(uid: string): Promise<Solicitud | null> {
  try {
    const snap = await getDoc(doc(db, COL, uid));
    if (!snap.exists()) return null;
    return aSolicitud(snap.id, snap.data());
  } catch (e) {
    if (esPermisoDenegado(e)) throw new ReglasNoListas();
    throw e;
  }
}

/** Todas las que esperan respuesta del dueño. */
export async function listarSolicitudesPendientes(): Promise<Solicitud[]> {
  try {
    const docs = await getDocs(collection(db, COL));
    return docs.docs
      .map((d) => aSolicitud(d.id, d.data()))
      .filter((s) => s.estado === "pendiente");
  } catch (e) {
    if (esPermisoDenegado(e)) throw new ReglasNoListas();
    throw e;
  }
}

/**
 * Da de alta al postulante con el nivel elegido (reusa el mismo alta por UID de
 * operadores.ts) y deja la solicitud en "aceptada" como rastro.
 */
export async function aceptarSolicitud(
  sol: Solicitud,
  nivel: NivelOperador = "operador",
): Promise<void> {
  await agregarOperador(sol.uid, sol.nombre, nivel);
  try {
    await updateDoc(doc(db, COL, sol.uid), { estado: "aceptada" });
  } catch (e) {
    // El operador ya quedó creado; si no pudimos marcar la solicitud, no es
    // grave — no reventamos el flujo por el rastro.
    if (!esPermisoDenegado(e)) throw e;
  }
}

/** Marca "rechazada" (no borra: ver nota de cabecera). */
export async function rechazarSolicitud(uid: string): Promise<void> {
  try {
    await updateDoc(doc(db, COL, uid), { estado: "rechazada" });
  } catch (e) {
    if (esPermisoDenegado(e)) throw new ReglasNoListas();
    throw e;
  }
}

/** Por si algún día se quiere limpiar de verdad una solicitud. */
export async function borrarSolicitud(uid: string): Promise<void> {
  await deleteDoc(doc(db, COL, uid));
}
