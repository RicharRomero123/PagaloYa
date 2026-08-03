"use client";

import { useState } from "react";
import {
  ETIQUETA_PLAN,
  MAX_DISPOSITIVOS,
  type Comercio,
  type Plan,
  type Suscripcion,
} from "@/lib/comercios";
import {
  ETIQUETA_ESTADO_SUSCRIPCION,
  ajustarSuscripcion,
  type EstadoSuscripcion,
} from "@/lib/membresia";

const PLANES: Plan[] = ["gratis", "caserito", "patron"];
const ESTADOS: EstadoSuscripcion[] = ["prueba", "activa", "vencida"];

/**
 * Escotilla de operador: fija plan, estado y fecha de vencimiento a mano.
 *
 * El flujo normal es cobrar N meses (FormularioMembresia), que deja recibo. Esto
 * es para lo que ese flujo no cubre: corregir una fecha, subir el tope de
 * teléfonos por cortesía, o registrar algo cobrado por fuera. NO genera cobro —
 * por eso va plegado y con un aviso.
 */
export function AjusteManual({
  comercio,
  alActualizar,
}: {
  comercio: Comercio;
  alActualizar: (s: Suscripcion) => void;
}) {
  const [abierto, setAbierto] = useState(false);

  if (!abierto) {
    return (
      <button
        type="button"
        onClick={() => setAbierto(true)}
        className="w-full py-2 text-center text-sm font-bold text-azul underline underline-offset-4"
      >
        Ajuste manual de plan
      </button>
    );
  }

  return <Panel comercio={comercio} alActualizar={alActualizar} />;
}

function Panel({
  comercio,
  alActualizar,
}: {
  comercio: Comercio;
  alActualizar: (s: Suscripcion) => void;
}) {
  const actual = comercio.suscripcion;
  const [plan, setPlan] = useState<Plan>(actual?.plan ?? "gratis");
  const [estado, setEstado] = useState<EstadoSuscripcion>(
    actual?.estado ?? "activa",
  );
  const [fecha, setFecha] = useState(() => aInputFecha(actual?.vigenteHasta));
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function guardar() {
    const ms = deInputFecha(fecha);
    if (ms === null) {
      setError("Pon una fecha de vencimiento válida.");
      return;
    }
    setGuardando(true);
    setError(null);
    void ajustarSuscripcion({
      comercioId: comercio.id,
      plan,
      estado,
      vigenteHasta: ms,
    })
      .then(alActualizar)
      .catch(() =>
        setError("No se pudo guardar el ajuste. Revisa tu conexión."),
      )
      .finally(() => setGuardando(false));
  }

  return (
    <div className="rounded-2xl border-2 border-dashed border-borde bg-white p-4">
      <h3 className="font-black">Ajuste manual</h3>
      <p className="mt-1 text-xs text-texto-tenue">
        Fija plan, estado y vencimiento directo.{" "}
        <strong>No genera cobro</strong> ni deja recibo. Para cobrar usa el
        botón naranja de arriba.
      </p>

      <Grupo titulo="Plan">
        {PLANES.map((p) => (
          <Chip key={p} activo={plan === p} onClick={() => setPlan(p)}>
            {ETIQUETA_PLAN[p]} · {MAX_DISPOSITIVOS[p]}{" "}
            {MAX_DISPOSITIVOS[p] === 1 ? "tel." : "tels."}
          </Chip>
        ))}
      </Grupo>

      <Grupo titulo="Estado">
        {ESTADOS.map((e) => (
          <Chip key={e} activo={estado === e} onClick={() => setEstado(e)}>
            {ETIQUETA_ESTADO_SUSCRIPCION[e]}
          </Chip>
        ))}
      </Grupo>

      <label className="mt-4 block">
        <span className="text-xs font-bold uppercase text-texto-tenue">
          Vence el
        </span>
        <input
          type="date"
          value={fecha}
          onChange={(e) => setFecha(e.target.value)}
          className="mt-1 h-11 w-full rounded-xl border border-borde px-3 outline-none focus:border-naranja"
        />
      </label>

      {error && <p className="mt-3 text-sm text-rojo-alerta">{error}</p>}

      <button
        type="button"
        disabled={guardando}
        onClick={guardar}
        className="mt-4 h-12 w-full rounded-xl bg-azul font-bold text-white transition hover:bg-azul/90 disabled:opacity-50"
      >
        {guardando ? "Guardando…" : "Guardar ajuste"}
      </button>
    </div>
  );
}

/** ms → "YYYY-MM-DD" en hora local, para el input date. */
function aInputFecha(ms: number | undefined): string {
  const d = ms ? new Date(ms) : new Date();
  const anio = d.getFullYear();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${anio}-${mes}-${dia}`;
}

/** "YYYY-MM-DD" → ms al FINAL de ese día local (23:59:59), o null si es inválida.
 *  Fin del día para que "vence el 5" incluya todo el día 5. */
function deInputFecha(valor: string): number | null {
  const partes = valor.split("-").map(Number);
  if (partes.length !== 3 || partes.some((n) => !Number.isFinite(n))) {
    return null;
  }
  const [anio, mes, dia] = partes;
  const d = new Date(anio, mes - 1, dia, 23, 59, 59, 0);
  return Number.isNaN(d.getTime()) ? null : d.getTime();
}

function Grupo({
  titulo,
  children,
}: {
  titulo: string;
  children: React.ReactNode;
}) {
  return (
    <div className="mt-4">
      <p className="mb-2 text-xs font-bold uppercase text-texto-tenue">
        {titulo}
      </p>
      <div className="flex flex-wrap gap-2">{children}</div>
    </div>
  );
}

function Chip({
  activo,
  onClick,
  children,
}: {
  activo: boolean;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-full px-3 py-2 text-sm font-bold transition ${
        activo
          ? "bg-naranja text-white"
          : "bg-humo text-texto-medio hover:bg-naranja-suave"
      }`}
    >
      {children}
    </button>
  );
}
