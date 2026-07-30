"use client";

import { useMemo, useState } from "react";
import { ETIQUETA_PLAN, type Comercio, type Suscripcion } from "@/lib/comercios";
import { fechaCorta, soles } from "@/lib/formato";
import {
  ETIQUETA_METODO,
  PRECIOS,
  baseDeRenovacion,
  cobrarYActivar,
  darPrueba,
  sumarMeses,
  type Metodo,
  type PlanPago,
} from "@/lib/membresia";

const PLANES: PlanPago[] = ["caserito", "patron"];
const MESES = [1, 3, 6, 12];
const METODOS: Metodo[] = ["efectivo", "yape", "transferencia"];

export function FormularioMembresia({
  comercio,
  operadorUid,
  alActualizar,
}: {
  comercio: Comercio;
  operadorUid: string;
  alActualizar: (s: Suscripcion) => void;
}) {
  const [plan, setPlan] = useState<PlanPago>(
    comercio.suscripcion?.plan === "patron" ? "patron" : "caserito",
  );
  const [meses, setMeses] = useState(1);
  const [metodo, setMetodo] = useState<Metodo>("yape");
  const [montoTexto, setMontoTexto] = useState("");
  const [nota, setNota] = useState("");
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const desde = useMemo(() => baseDeRenovacion(comercio), [comercio]);
  const hasta = useMemo(() => sumarMeses(desde, meses), [desde, meses]);
  const sugerido = PRECIOS[plan] * meses;
  const monto = montoTexto.trim() === "" ? sugerido : Number(montoTexto);
  const renueva = desde > Date.now();

  async function ejecutar(accion: () => Promise<Suscripcion>, falla: string) {
    setGuardando(true);
    setError(null);
    try {
      alActualizar(await accion());
    } catch {
      setError(falla);
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="rounded-2xl bg-white p-4">
      <h3 className="font-black">
        {renueva ? "Renovar membresía" : "Cobrar y activar"}
      </h3>

      <Grupo titulo="Plan">
        {PLANES.map((p) => (
          <Chip key={p} activo={plan === p} onClick={() => setPlan(p)}>
            {ETIQUETA_PLAN[p]} · {soles(PRECIOS[p])}
          </Chip>
        ))}
      </Grupo>

      <Grupo titulo="Tiempo">
        {MESES.map((m) => (
          <Chip key={m} activo={meses === m} onClick={() => setMeses(m)}>
            {m} {m === 1 ? "mes" : "meses"}
          </Chip>
        ))}
      </Grupo>

      <Grupo titulo="Cómo pagó">
        {METODOS.map((m) => (
          <Chip key={m} activo={metodo === m} onClick={() => setMetodo(m)}>
            {ETIQUETA_METODO[m]}
          </Chip>
        ))}
      </Grupo>

      <div className="mt-4 grid grid-cols-2 gap-3">
        <label className="block">
          <span className="text-xs font-bold uppercase text-texto-tenue">
            Monto cobrado
          </span>
          <input
            inputMode="decimal"
            value={montoTexto}
            onChange={(e) => setMontoTexto(e.target.value)}
            placeholder={sugerido.toFixed(2)}
            className="mt-1 h-11 w-full rounded-xl border border-borde px-3 outline-none focus:border-naranja"
          />
        </label>
        <label className="block">
          <span className="text-xs font-bold uppercase text-texto-tenue">
            Nota (opcional)
          </span>
          <input
            value={nota}
            onChange={(e) => setNota(e.target.value)}
            placeholder="Nº de operación…"
            className="mt-1 h-11 w-full rounded-xl border border-borde px-3 outline-none focus:border-naranja"
          />
        </label>
      </div>

      {/* El resumen antes de confirmar: que vea qué va a pasar, no que lo adivine */}
      <p className="mt-4 rounded-xl bg-humo px-3 py-2 text-sm text-texto-medio">
        {ETIQUETA_PLAN[plan]} · {soles(monto || 0)} ·{" "}
        {renueva ? "sigue desde" : "desde"} {fechaCorta(desde)} y vence el{" "}
        <strong className="text-azul">{fechaCorta(hasta)}</strong>
        {renueva && (
          <span className="block text-xs">
            Todavía le quedaban días: no se los quitamos.
          </span>
        )}
      </p>

      {error && <p className="mt-3 text-sm text-rojo-alerta">{error}</p>}

      <button
        type="button"
        disabled={guardando}
        onClick={() => {
          if (!Number.isFinite(monto) || monto <= 0 || monto > 1000) {
            setError("El monto tiene que estar entre S/ 0.01 y S/ 1000.");
            return;
          }
          void ejecutar(
            () =>
              cobrarYActivar({
                comercioId: comercio.id,
                plan,
                meses,
                metodo,
                monto,
                nota,
                operadorUid,
                ahora: desde,
              }),
            "No se pudo registrar el cobro. Revisa tu conexión e intenta otra vez.",
          );
        }}
        className="mt-4 h-12 w-full rounded-xl bg-naranja font-bold text-white transition hover:bg-naranja-hondo disabled:opacity-50"
      >
        {guardando ? "Registrando…" : `Cobrar ${soles(monto || 0)} y activar`}
      </button>

      <div className="mt-3 flex gap-2">
        <button
          type="button"
          disabled={guardando}
          onClick={() =>
            void ejecutar(
              () => darPrueba(comercio.id, plan, 30),
              "No se pudo dar la prueba.",
            )
          }
          className="h-11 flex-1 rounded-xl border-2 border-borde text-sm font-bold text-azul transition hover:border-naranja disabled:opacity-50"
        >
          Dar 30 días de prueba
        </button>
      </div>
      <p className="mt-2 text-xs text-texto-tenue">
        La prueba activa el plan sin generar cobro. Es para el beta cerrado.
      </p>
    </div>
  );
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
      <p className="mb-2 text-xs font-bold uppercase text-texto-tenue">{titulo}</p>
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
