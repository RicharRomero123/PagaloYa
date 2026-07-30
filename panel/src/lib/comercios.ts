import {
  collection,
  getDocs,
  limit,
  orderBy,
  query,
  type DocumentData,
} from "firebase/firestore";
import { db } from "./firebase";

export type Plan = "gratis" | "caserito" | "patron";

export type Suscripcion = {
  plan: Plan;
  estado: "prueba" | "activa" | "vencida";
  origen: "manual" | "pasarela";
  vigenteHasta: number;
};

export type Comercio = {
  id: string;
  nombre: string;
  duenoUid: string;
  codigoVinculacion: string;
  creadoEn: number;
  suscripcion: Suscripcion | null;
  direccion: string | null;
  telefono: string | null;
  registradoPor: string | null;
};

/**
 * Estado de membresía calculado. Son los mismos colores que los pines del mapa
 * de cobertura (ver PANEL.md §7), para que un vistazo signifique lo mismo en
 * las dos pantallas.
 */
export type EstadoMembresia = "al-dia" | "por-vencer" | "vencida" | "sin-plan";

const UNA_SEMANA = 7 * 24 * 60 * 60 * 1000;

/**
 * Tope de 200 a propósito: el plan Spark da ~50 000 lecturas al día y un panel
 * descuidado se las come en una tarde. Cuando haya más comercios, esto pasa a
 * paginar, no a subir el límite.
 */
const TOPE = 200;

export async function listarComercios(): Promise<Comercio[]> {
  const consulta = query(
    collection(db, "comercios"),
    orderBy("creadoEn", "desc"),
    limit(TOPE),
  );
  const docs = await getDocs(consulta);
  return docs.docs.map((d) => aComercio(d.id, d.data()));
}

function aComercio(id: string, datos: DocumentData): Comercio {
  const suscripcion = datos.suscripcion as Suscripcion | undefined;
  const contacto = datos.contacto as DocumentData | undefined;
  const ubicacion = datos.ubicacion as DocumentData | undefined;
  return {
    id,
    nombre: (datos.nombre as string) ?? "Sin nombre",
    duenoUid: (datos.duenoUid as string) ?? "",
    codigoVinculacion: (datos.codigoVinculacion as string) ?? "",
    creadoEn: Number(datos.creadoEn ?? 0),
    suscripcion: suscripcion ?? null,
    direccion: (ubicacion?.direccion as string) ?? null,
    telefono: (contacto?.telefono as string) ?? null,
    registradoPor: (datos.registradoPor as string) ?? null,
  };
}

export function estadoDe(comercio: Comercio, ahora = Date.now()): EstadoMembresia {
  const s = comercio.suscripcion;
  if (!s || s.plan === "gratis") return "sin-plan";
  if (s.vigenteHasta <= ahora) return "vencida";
  if (s.vigenteHasta - ahora <= UNA_SEMANA) return "por-vencer";
  return "al-dia";
}

export const ETIQUETA_ESTADO: Record<EstadoMembresia, string> = {
  "al-dia": "Al día",
  "por-vencer": "Vence pronto",
  vencida: "Vencido",
  "sin-plan": "Sin plan",
};

export const ETIQUETA_PLAN: Record<Plan, string> = {
  gratis: "Gratis",
  caserito: "Caserito",
  patron: "Patrón",
};

export function nombrePlan(comercio: Comercio): string {
  const plan = comercio.suscripcion?.plan;
  return plan ? ETIQUETA_PLAN[plan] : "—";
}
