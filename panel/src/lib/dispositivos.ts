import {
  Timestamp,
  collection,
  collectionGroup,
  getDocs,
  limit,
  orderBy,
  query,
  type DocumentData,
} from "firebase/firestore";
import { db } from "./firebase";

export type EstadoYape = "ok" | "detenida" | "no_instalada";

/** El latido que sube cada teléfono (ver TelemetriaRepo en la app). */
export type Dispositivo = {
  uid: string;
  comercioId: string;
  nombre: string;
  capturando: boolean;
  listenerConectado: boolean;
  permisoEscucha: boolean;
  ultimaNotifBilletera: number;
  yape: EstadoYape;
  puedeRevivir: boolean;
  bateriaLibre: boolean;
  marca: string;
  modelo: string;
  versionApp: string;
  ultimoLatido: number;
};

const TOPE = 400;

/**
 * Semáforo global: TODOS los dispositivos de todos los comercios en una sola
 * consulta de grupo. Necesita el índice de collectionGroup sobre ultimoLatido
 * (backend/firestore.indexes.json).
 */
export async function listarDispositivos(): Promise<Dispositivo[]> {
  const docs = await getDocs(
    query(
      collectionGroup(db, "dispositivos"),
      orderBy("ultimoLatido", "desc"),
      limit(TOPE),
    ),
  );
  return docs.docs.map((d) =>
    aDispositivo(d.id, d.ref.parent.parent?.id ?? "", d.data()),
  );
}

export async function dispositivosDe(comercioId: string): Promise<Dispositivo[]> {
  const docs = await getDocs(
    collection(db, "comercios", comercioId, "dispositivos"),
  );
  return docs.docs.map((d) => aDispositivo(d.id, comercioId, d.data()));
}

function aDispositivo(
  uid: string,
  comercioId: string,
  datos: DocumentData,
): Dispositivo {
  const latido = datos.ultimoLatido as Timestamp | undefined;
  return {
    uid,
    comercioId,
    nombre: (datos.nombre as string) ?? "Sin nombre",
    capturando: (datos.capturando as boolean) ?? false,
    listenerConectado: (datos.listenerConectado as boolean) ?? false,
    permisoEscucha: (datos.permisoEscucha as boolean) ?? false,
    ultimaNotifBilletera: Number(datos.ultimaNotifBilletera ?? 0),
    yape: (datos.yape as EstadoYape) ?? "no_instalada",
    puedeRevivir: (datos.puedeRevivir as boolean) ?? false,
    bateriaLibre: (datos.bateriaLibre as boolean) ?? false,
    marca: (datos.marca as string) ?? "",
    modelo: (datos.modelo as string) ?? "",
    versionApp: (datos.versionApp as string) ?? "",
    ultimoLatido: latido ? latido.toMillis() : 0,
  };
}

export type SaludTelefono = "ok" | "aviso" | "caido";

/**
 * El vigilante de la app late cada ~15 min; a los 45 sin latido ya van tres
 * seguidos perdidos — eso no es mala suerte, es un teléfono apagado, sin
 * internet o con la app muerta.
 */
const LATIDO_VIEJO = 45 * 60 * 1000;

/**
 * El semáforo, en orden de gravedad. "caido" = las ventas NO están sonando en
 * ese comercio: es una llamada de soporte, no un dato.
 */
export function saludDe(
  d: Dispositivo,
  ahora = Date.now(),
): { salud: SaludTelefono; detalle: string } {
  if (ahora - d.ultimoLatido > LATIDO_VIEJO) {
    return {
      salud: "caido",
      detalle: "La app no da señales: ¿teléfono apagado o sin internet?",
    };
  }
  if (d.capturando) {
    if (!d.permisoEscucha) {
      return { salud: "caido", detalle: "Perdió el permiso de escuchar notificaciones" };
    }
    if (!d.listenerConectado) {
      return { salud: "caido", detalle: "Oído desconectado: no está capturando" };
    }
    if (d.yape === "detenida") {
      return { salud: "caido", detalle: "Yape está detenida y no se pudo revivir" };
    }
    if (d.yape === "no_instalada") {
      return { salud: "aviso", detalle: "Yape no está instalada en este teléfono" };
    }
    if (!d.bateriaLibre) {
      return { salud: "aviso", detalle: "La batería puede dormir a PagoYa" };
    }
    if (!d.puedeRevivir) {
      return { salud: "aviso", detalle: "Sin permiso para revivir Yape solo" };
    }
  } else if (!d.bateriaLibre) {
    return { salud: "aviso", detalle: "La batería puede dormir a PagoYa" };
  }
  return { salud: "ok", detalle: "Escuchando bien" };
}

export const ETIQUETA_SALUD: Record<SaludTelefono, string> = {
  ok: "Bien",
  aviso: "Revisar",
  caido: "Caído",
};

export const COLOR_SALUD: Record<SaludTelefono, string> = {
  ok: "bg-verde-suave text-verde-ok",
  aviso: "bg-ambar-suave text-ambar-aviso",
  caido: "bg-rojo-suave text-rojo-alerta",
};
