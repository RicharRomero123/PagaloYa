import {
  collection,
  getDocs,
  limit,
  orderBy,
  query,
  Timestamp,
  type DocumentData,
} from "firebase/firestore";
import { httpsCallable } from "firebase/functions";
import { db, functions } from "./firebase";

/**
 * Campañas push del equipo PagoYa. NO son pagos: son avisos/ofertas de
 * marketing y soporte (ver backend/PUSH-CAMPANAS.md). El envío real corre en
 * una Cloud Function (enviarCampana) porque mandar a un topic exige credencial
 * de servidor, que jamás debe vivir en el navegador. Desde el panel solo
 * llamamos la callable y leemos el historial.
 */

/** Topics FCM a los que se suscribe la app según el plan del comercio. */
export type Topic = "todos" | "plan_gratis" | "plan_caserito" | "plan_patron";

export type TopicInfo = {
  id: Topic;
  etiqueta: string;
  /** A quién le llega, en criollo, para elegir bien y no quemar la lista. */
  descripcion: string;
};

/**
 * Los cuatro topics de PUSH-CAMPANAS.md §1. "todos" va a TODA la base: úsalo
 * con cuidado (por eso la UI pide doble confirmación).
 */
export const TOPICS: TopicInfo[] = [
  {
    id: "todos",
    etiqueta: "Todos",
    descripcion: "Todos los teléfonos con la app instalada. Va a toda la base.",
  },
  {
    id: "plan_gratis",
    etiqueta: "Plan Gratis",
    descripcion: "Solo comercios en plan Gratis. Ideal para empujar el upgrade.",
  },
  {
    id: "plan_caserito",
    etiqueta: "Plan Caserito",
    descripcion: "Solo comercios en plan Caserito.",
  },
  {
    id: "plan_patron",
    etiqueta: "Plan Patrón",
    descripcion: "Solo comercios en plan Patrón.",
  },
];

/** Plantilla rápida de campaña, en tono criollo (PUSH-CAMPANAS.md §3). */
export type Plantilla = {
  etiqueta: string;
  titulo: string;
  cuerpo: string;
  topicSugerido: Topic;
};

export const PLANTILLAS: Plantilla[] = [
  {
    etiqueta: "Aviso general",
    titulo: "¡PagoYa está fino!",
    cuerpo: "Actualizamos la app pa' que tus pagos suenen más rápido, casero.",
    topicSugerido: "todos",
  },
  {
    etiqueta: "Recordatorio de cierre",
    titulo: "¿Ya cuadraste tu caja?",
    cuerpo: "Mira el total del día en PagoYa y cierra tranquilo.",
    topicSugerido: "plan_caserito",
  },
  {
    etiqueta: "Empujar upgrade",
    titulo: "Tu gente también puede escuchar los pagos",
    cuerpo:
      "Con el Plan Caserito, tú en casa y tu chamba en la tienda oye cada Yape. Pregúntanos.",
    topicSugerido: "plan_gratis",
  },
];

export type Campana = {
  id: string;
  titulo: string;
  cuerpo: string;
  topic: Topic;
  operadorNombre: string;
  enviadoEn: number;
};

/**
 * Dispara la campaña vía la callable enviarCampana (us-central1). La callable
 * valida operador y textos en el servidor y escribe el historial; aquí solo
 * pasamos los datos. Si la función aún no está desplegada, esto rechaza y el
 * componente muestra el error.
 */
export async function enviarCampana(
  titulo: string,
  cuerpo: string,
  topic: Topic,
): Promise<void> {
  const llamar = httpsCallable<
    { titulo: string; cuerpo: string; topic: Topic },
    { ok: true }
  >(functions, "enviarCampana");
  await llamar({ titulo, cuerpo, topic });
}

/**
 * Lee las últimas 20 campañas de la colección `campanas`, más recientes
 * primero. Las reglas permiten esta lectura a operadores; el panel NUNCA
 * escribe aquí (eso lo hace la Cloud Function).
 */
export async function listarCampanas(): Promise<Campana[]> {
  const consulta = query(
    collection(db, "campanas"),
    orderBy("enviadoEn", "desc"),
    limit(20),
  );
  const docs = await getDocs(consulta);
  return docs.docs.map((d) => aCampana(d.id, d.data()));
}

function aCampana(id: string, datos: DocumentData): Campana {
  const enviadoEn = datos.enviadoEn;
  return {
    id,
    titulo: (datos.titulo as string) ?? "",
    cuerpo: (datos.cuerpo as string) ?? "",
    topic: (datos.topic as Topic) ?? "todos",
    operadorNombre: (datos.operadorNombre as string) ?? "",
    enviadoEn: enviadoEn instanceof Timestamp ? enviadoEn.toMillis() : 0,
  };
}
