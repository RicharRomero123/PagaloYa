"use client";

import { useMemo } from "react";
import type { Comercio } from "@/lib/comercios";
import {
  COLOR_SALUD,
  ETIQUETA_SALUD,
  saludDe,
  type Dispositivo,
} from "@/lib/dispositivos";
import { haceRato } from "@/lib/formato";

/**
 * El semáforo de teléfonos: la razón de ser de la telemetría. Nadie vigila
 * mil teléfonos a mano — esta sección grita sola cuáles se murieron, para
 * llamar al comerciante ANTES de que él reclame que "no suena".
 */
export function SaludTelefonos({
  dispositivos,
  comercios,
  alAbrirComercio,
}: {
  dispositivos: Dispositivo[] | null;
  comercios: Comercio[] | null;
  alAbrirComercio: (comercioId: string) => void;
}) {
  const nombres = useMemo(() => {
    const mapa = new Map<string, string>();
    for (const c of comercios ?? []) mapa.set(c.id, c.nombre);
    return mapa;
  }, [comercios]);

  const conProblemas = useMemo(() => {
    const ahora = Date.now();
    return (dispositivos ?? [])
      .map((d) => ({ dispositivo: d, ...saludDe(d, ahora) }))
      .filter((f) => f.salud !== "ok")
      .sort((a, b) => (a.salud === "caido" ? 0 : 1) - (b.salud === "caido" ? 0 : 1));
  }, [dispositivos]);

  if (dispositivos === null || dispositivos.length === 0) return null;

  if (conProblemas.length === 0) {
    return (
      <p className="mt-4 rounded-2xl bg-verde-suave px-4 py-3 text-sm font-bold text-verde-ok">
        ✅ Los {dispositivos.length} teléfonos están escuchando bien.
      </p>
    );
  }

  return (
    <section className="mt-4 overflow-hidden rounded-2xl border border-rojo-alerta/30 bg-white">
      <h2 className="bg-rojo-suave px-4 py-3 text-sm font-black text-rojo-alerta">
        📵 {conProblemas.length}{" "}
        {conProblemas.length === 1
          ? "teléfono necesita atención"
          : "teléfonos necesitan atención"}
      </h2>
      <ul className="divide-y divide-borde">
        {conProblemas.map(({ dispositivo: d, salud, detalle }) => (
          <li
            key={`${d.comercioId}-${d.uid}`}
            role="button"
            tabIndex={0}
            onClick={() => alAbrirComercio(d.comercioId)}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") alAbrirComercio(d.comercioId);
            }}
            className="flex cursor-pointer flex-wrap items-center gap-3 px-4 py-3 transition hover:bg-humo"
          >
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-bold">
                {nombres.get(d.comercioId) ?? "Comercio"}
                <span className="font-normal text-texto-medio"> · {d.nombre}</span>
              </p>
              <p className="truncate text-xs text-texto-medio">
                {detalle} · {d.marca} {d.modelo} · latido {haceRato(d.ultimoLatido)}
              </p>
            </div>
            <span
              className={`shrink-0 rounded-full px-3 py-1 text-xs font-bold ${COLOR_SALUD[salud]}`}
            >
              {ETIQUETA_SALUD[salud]}
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
