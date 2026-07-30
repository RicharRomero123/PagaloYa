import {
  collection,
  deleteDoc,
  doc,
  getDocs,
  serverTimestamp,
  setDoc,
  updateDoc,
  type DocumentData,
} from "firebase/firestore";
import { db } from "./firebase";

export type NivelOperador = "dueno" | "operador";

export type Operador = {
  uid: string;
  nombre: string;
  nivel: NivelOperador;
  activo: boolean;
};

export const ETIQUETA_NIVEL: Record<NivelOperador, string> = {
  dueno: "Dueño",
  operador: "Operador",
};

export async function listarOperadores(): Promise<Operador[]> {
  const docs = await getDocs(collection(db, "operadores"));
  return docs.docs
    .map((d) => aOperador(d.id, d.data()))
    .sort((a, b) => (a.nivel === b.nivel ? 0 : a.nivel === "dueno" ? -1 : 1));
}

function aOperador(uid: string, datos: DocumentData): Operador {
  return {
    uid,
    nombre: (datos.nombre as string) ?? "Sin nombre",
    nivel: (datos.nivel as NivelOperador) ?? "operador",
    activo: (datos.activo as boolean) ?? true,
  };
}

/**
 * Da de alta a alguien del equipo.
 *
 * Hace falta su UID, y para tenerlo la persona debe haber entrado al panel al
 * menos una vez: la pantalla "sin acceso" se lo muestra para que te lo dicte.
 * Es el mismo patrón del código de 6 dígitos del comercio, y es a propósito —
 * buscar usuarios por correo exigiría el Admin SDK en un servidor, y eso
 * significa plan Blaze.
 */
export async function agregarOperador(
  uid: string,
  nombre: string,
  nivel: NivelOperador,
): Promise<void> {
  await setDoc(doc(db, "operadores", uid.trim()), {
    nombre: nombre.trim(),
    nivel,
    activo: true,
    creadoEn: serverTimestamp(),
  });
}

/** Baja suave: pierde el acceso al instante pero su nombre sigue apareciendo
 *  en los cobros que registró. Por eso no se borra por defecto. */
export async function definirActivo(uid: string, activo: boolean): Promise<void> {
  await updateDoc(doc(db, "operadores", uid), { activo });
}

export async function cambiarNivel(
  uid: string,
  nivel: NivelOperador,
): Promise<void> {
  await updateDoc(doc(db, "operadores", uid), { nivel });
}

export async function eliminarOperador(uid: string): Promise<void> {
  await deleteDoc(doc(db, "operadores", uid));
}
