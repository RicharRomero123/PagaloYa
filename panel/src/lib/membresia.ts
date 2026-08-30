import {
  Timestamp,
  collection,
  doc,
  getDocs,
  increment,
  limit,
  orderBy,
  query,
  serverTimestamp,
  updateDoc,
  writeBatch,
} from "firebase/firestore";
import { db } from "./firebase";
import type { Comercio, Plan, Suscripcion } from "./comercios";

export type Metodo = "efectivo" | "yape" | "transferencia" | "tarjeta";

export type PlanPago = Exclude<Plan, "gratis">;

/** Precios de BRAND.md. El monto es editable: puedes dar descuento. */
export const PRECIOS: Record<PlanPago, number> = {
  caserito: 12.9,
  patron: 24.9,
};

export const ETIQUETA_METODO: Record<Metodo, string> = {
  efectivo: "Efectivo",
  yape: "Yape",
  transferencia: "Transferencia",
  tarjeta: "Tarjeta",
};

export type Cobro = {
  id: string;
  monto: number;
  metodo: Metodo;
  periodoDesde: number;
  periodoHasta: number;
  cobradoPor: string;
  creadoEn: number;
  nota: string | null;
};

export function sumarMeses(desde: number, meses: number): number {
  const fecha = new Date(desde);
  fecha.setMonth(fecha.getMonth() + meses);
  return fecha.getTime();
}

export function sumarDias(desde: number, dias: number): number {
  return desde + dias * 86_400_000;
}

/**
 * Desde cuándo cuenta una renovación.
 *
 * Si al comercio todavía le quedan días, la renovación arranca cuando vence lo
 * que ya pagó — no hoy. Cobrarle el 20 y quitarle los 10 días que le quedaban
 * es la clase de detalle que un comerciante nota y no perdona.
 */
export function baseDeRenovacion(comercio: Comercio, ahora = Date.now()): number {
  const vigente = comercio.suscripcion?.vigenteHasta ?? 0;
  return vigente > ahora ? vigente : ahora;
}

/**
 * Cobro manual: activa el plan y deja el asiento contable, **en un solo lote**.
 * Si se hicieran por separado y fallara el segundo, tendrías un comercio activo
 * sin registro de quién cobró — justo lo que rompe las comisiones y tu caja.
 */
export async function cobrarYActivar(datos: {
  comercioId: string;
  plan: PlanPago;
  meses: number;
  metodo: Metodo;
  monto: number;
  nota: string;
  operadorUid: string;
  ahora?: number;
}): Promise<Suscripcion> {
  const desde = datos.ahora ?? Date.now();
  const hasta = sumarMeses(desde, datos.meses);

  const suscripcion: Suscripcion = {
    plan: datos.plan,
    estado: "activa",
    origen: "manual",
    vigenteHasta: hasta,
  };

  const lote = writeBatch(db);
  lote.update(doc(db, "comercios", datos.comercioId), { suscripcion });
  lote.set(
    doc(collection(db, "comercios", datos.comercioId, "pagosMembresia")),
    {
      monto: datos.monto,
      metodo: datos.metodo,
      periodoDesde: desde,
      periodoHasta: hasta,
      cobradoPor: datos.operadorUid,
      creadoEn: serverTimestamp(),
      ...(datos.nota.trim() ? { nota: datos.nota.trim() } : {}),
    },
  );
  await lote.commit();

  return suscripcion;
}

/** Prueba gratis del beta: activa el plan sin generar cobro. */
export async function darPrueba(
  comercioId: string,
  plan: PlanPago,
  dias: number,
): Promise<Suscripcion> {
  const suscripcion: Suscripcion = {
    plan,
    estado: "prueba",
    origen: "manual",
    vigenteHasta: sumarDias(Date.now(), dias),
  };
  await updateDoc(doc(db, "comercios", comercioId), { suscripcion });
  return suscripcion;
}

export type EstadoSuscripcion = Suscripcion["estado"];

export const ETIQUETA_ESTADO_SUSCRIPCION: Record<EstadoSuscripcion, string> = {
  prueba: "En prueba",
  activa: "Activa",
  vencida: "Vencida",
};

/**
 * Ajuste manual directo de la suscripción: fija plan, estado y fecha de
 * vencimiento a mano, sin generar cobro. Es la escotilla de operador para
 * casos que el flujo de "cobrar N meses" no cubre — corregir una fecha mal
 * puesta, activar un gratis con más teléfonos por cortesía, o registrar un
 * pago que ya se hizo por fuera. Respeta el shape exacto que exige
 * suscripcionValida() en las reglas (origen: "manual", vigenteHasta: int).
 *
 * Para "cobrar" con asiento contable usa cobrarYActivar; esto NO deja recibo.
 */
export async function ajustarSuscripcion(datos: {
  comercioId: string;
  plan: Plan;
  estado: EstadoSuscripcion;
  vigenteHasta: number;
}): Promise<Suscripcion> {
  const suscripcion: Suscripcion = {
    plan: datos.plan,
    estado: datos.estado,
    origen: "manual",
    // Math.trunc: vigenteHasta DEBE ser int para pasar suscripcionValida().
    vigenteHasta: Math.trunc(datos.vigenteHasta),
  };
  await updateDoc(doc(db, "comercios", datos.comercioId), { suscripcion });
  return suscripcion;
}

/**
 * Saca a un miembro/teléfono del comercio (borra comercios/{id}/miembros/{uid})
 * y devuelve el cupo bajando `comercios/{id}.numDispositivos` en 1, **en el
 * mismo lote**.
 *
 * El decremento NO es opcional: la regla de delete de miembros en
 * `backend/firestore.rules` exige que el MISMO WriteBatch baje numDispositivos
 * en exactamente 1 (piso 1, el dueño siempre queda). Un delete suelto —sin
 * tocar el contador— es rechazado con permission-denied. Por eso esto es un
 * batch y no un deleteDoc.
 *
 * Las reglas ya permiten al operador desvincular a un TRABAJADOR (nunca al
 * dueño); el panel solo muestra el botón "Quitar" en trabajadores.
 */
export async function quitarMiembro(
  comercioId: string,
  uid: string,
): Promise<void> {
  const lote = writeBatch(db);
  lote.delete(doc(db, "comercios", comercioId, "miembros", uid));
  lote.update(doc(db, "comercios", comercioId), {
    numDispositivos: increment(-1),
  });
  await lote.commit();
}

/** Corta el plan de inmediato. El historial de cobros queda intacto. */
export async function cortarPlan(comercioId: string): Promise<Suscripcion> {
  const suscripcion: Suscripcion = {
    plan: "gratis",
    estado: "vencida",
    origen: "manual",
    vigenteHasta: Date.now(),
  };
  await updateDoc(doc(db, "comercios", comercioId), { suscripcion });
  return suscripcion;
}

export async function listarCobros(comercioId: string): Promise<Cobro[]> {
  const docs = await getDocs(
    query(
      collection(db, "comercios", comercioId, "pagosMembresia"),
      orderBy("creadoEn", "desc"),
      limit(24),
    ),
  );
  return docs.docs.map((d) => {
    const datos = d.data();
    const creadoEn = datos.creadoEn as Timestamp | undefined;
    return {
      id: d.id,
      monto: Number(datos.monto ?? 0),
      metodo: (datos.metodo as Metodo) ?? "efectivo",
      periodoDesde: Number(datos.periodoDesde ?? 0),
      periodoHasta: Number(datos.periodoHasta ?? 0),
      cobradoPor: (datos.cobradoPor as string) ?? "",
      creadoEn: creadoEn ? creadoEn.toMillis() : 0,
      nota: (datos.nota as string) ?? null,
    };
  });
}

export type Miembro = {
  uid: string;
  nombre: string;
  rol: string;
  puedeCapturar: boolean;
};

export async function listarMiembros(comercioId: string): Promise<Miembro[]> {
  const docs = await getDocs(
    collection(db, "comercios", comercioId, "miembros"),
  );
  return docs.docs.map((d) => {
    const datos = d.data();
    const rol = (datos.rol as string) ?? "trabajador";
    return {
      uid: d.id,
      nombre: (datos.nombre as string) ?? "Sin nombre",
      rol,
      puedeCapturar: (datos.puedeCapturar as boolean) ?? rol === "dueno",
    };
  });
}
