"use client";

import { useEffect, useMemo, useState } from "react";
import {
  ETIQUETA_ESTADO,
  estadoDe,
  listarComercios,
  nombrePlan,
  type Comercio,
  type EstadoMembresia,
  type Suscripcion,
} from "@/lib/comercios";
import { fechaCorta, haceOEn } from "@/lib/formato";
import { salir } from "@/lib/sesion";
import { FichaComercio } from "./FichaComercio";

const COLOR_ESTADO: Record<EstadoMembresia, string> = {
  "al-dia": "bg-verde-suave text-verde-ok",
  "por-vencer": "bg-ambar-suave text-ambar-aviso",
  vencida: "bg-rojo-suave text-rojo-alerta",
  "sin-plan": "bg-humo text-texto-medio",
};

const ORDEN_FILTROS: (EstadoMembresia | "todos")[] = [
  "todos",
  "vencida",
  "por-vencer",
  "al-dia",
  "sin-plan",
];

export function ListaComercios({
  nombreOperador,
  operadorUid,
}: {
  nombreOperador: string;
  operadorUid: string;
}) {
  const [comercios, setComercios] = useState<Comercio[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busqueda, setBusqueda] = useState("");
  const [filtro, setFiltro] = useState<EstadoMembresia | "todos">("todos");
  const [abiertoId, setAbiertoId] = useState<string | null>(null);

  /**
   * Al cobrar, se actualiza el comercio en memoria en vez de recargar la lista.
   * Recargar costaría 200 lecturas cada vez que registras un pago, y la cuota
   * diaria de Spark no está para regalarla.
   */
  function actualizarSuscripcion(id: string, suscripcion: Suscripcion) {
    setComercios((previos) =>
      (previos ?? []).map((c) => (c.id === id ? { ...c, suscripcion } : c)),
    );
  }

  useEffect(() => {
    let vigente = true;
    listarComercios()
      .then((lista) => {
        if (vigente) setComercios(lista);
      })
      .catch(() => {
        if (vigente) setError("No se pudo cargar la lista. Revisa tu conexión.");
      });
    return () => {
      vigente = false;
    };
  }, []);

  const conteos = useMemo(() => {
    const base: Record<EstadoMembresia, number> = {
      "al-dia": 0,
      "por-vencer": 0,
      vencida: 0,
      "sin-plan": 0,
    };
    for (const c of comercios ?? []) base[estadoDe(c)] += 1;
    return base;
  }, [comercios]);

  const abierto = useMemo(
    () => (comercios ?? []).find((c) => c.id === abiertoId) ?? null,
    [comercios, abiertoId],
  );

  const visibles = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    return (comercios ?? []).filter((c) => {
      if (filtro !== "todos" && estadoDe(c) !== filtro) return false;
      if (!texto) return true;
      return (
        c.nombre.toLowerCase().includes(texto) ||
        c.codigoVinculacion.includes(texto) ||
        (c.telefono ?? "").includes(texto)
      );
    });
  }, [comercios, busqueda, filtro]);

  return (
    <main className="mx-auto max-w-5xl px-4 pb-16">
      <header className="flex items-center justify-between py-5">
        <div>
          <h1 className="text-2xl font-black text-naranja">PagoYa</h1>
          <p className="text-xs text-texto-medio">
            Panel de operador{nombreOperador ? ` · ${nombreOperador}` : ""}
          </p>
        </div>
        <button
          type="button"
          onClick={() => void salir()}
          className="text-sm text-texto-medio underline underline-offset-4"
        >
          Salir
        </button>
      </header>

      <section className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Tarjeta titulo="Comercios" valor={comercios?.length ?? null} destacada />
        <Tarjeta titulo="Vencidos" valor={comercios ? conteos.vencida : null} />
        <Tarjeta titulo="Vencen pronto" valor={comercios ? conteos["por-vencer"] : null} />
        <Tarjeta titulo="Al día" valor={comercios ? conteos["al-dia"] : null} />
      </section>

      <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center">
        <input
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          placeholder="Buscar por nombre, código o teléfono…"
          className="h-11 flex-1 rounded-xl border border-borde bg-white px-4 outline-none focus:border-naranja"
        />
        <div className="flex flex-wrap gap-2">
          {ORDEN_FILTROS.map((f) => (
            <button
              key={f}
              type="button"
              onClick={() => setFiltro(f)}
              className={`rounded-full px-3 py-1.5 text-xs font-bold transition ${
                filtro === f
                  ? "bg-naranja text-white"
                  : "bg-white text-texto-medio hover:bg-humo"
              }`}
            >
              {f === "todos" ? "Todos" : ETIQUETA_ESTADO[f]}
            </button>
          ))}
        </div>
      </div>

      <div className="mt-4 overflow-hidden rounded-2xl bg-white">
        {error && <Mensaje texto={error} />}
        {!error && comercios === null && <Mensaje texto="Cargando comercios…" />}
        {!error && comercios !== null && visibles.length === 0 && (
          <Mensaje
            texto={
              comercios.length === 0
                ? "Todavía no hay comercios registrados."
                : "Ningún comercio coincide con la búsqueda."
            }
          />
        )}

        <ul className="divide-y divide-borde">
          {visibles.map((c) => (
            <Fila key={c.id} comercio={c} alAbrir={() => setAbiertoId(c.id)} />
          ))}
        </ul>
      </div>

      {abierto && (
        <FichaComercio
          comercio={abierto}
          operadorUid={operadorUid}
          alCerrar={() => setAbiertoId(null)}
          alActualizar={(s) => actualizarSuscripcion(abierto.id, s)}
        />
      )}

      {comercios !== null && comercios.length >= 200 && (
        <p className="mt-4 text-center text-xs text-texto-tenue">
          Mostrando los 200 comercios más recientes. Toca paginar cuando pases de
          aquí — no subas el tope o te comes la cuota diaria de lecturas.
        </p>
      )}
    </main>
  );
}

function Fila({
  comercio,
  alAbrir,
}: {
  comercio: Comercio;
  alAbrir: () => void;
}) {
  const estado = estadoDe(comercio);
  const vence = comercio.suscripcion?.vigenteHasta ?? 0;

  return (
    <li
      role="button"
      tabIndex={0}
      onClick={alAbrir}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") alAbrir();
      }}
      className="flex cursor-pointer flex-wrap items-center gap-3 px-4 py-3.5 transition hover:bg-humo"
    >
      <div className="min-w-0 flex-1">
        <p className="truncate font-bold">{comercio.nombre}</p>
        <p className="truncate text-xs text-texto-medio">
          Código {comercio.codigoVinculacion || "—"}
          {comercio.direccion ? ` · ${comercio.direccion}` : ""}
          {comercio.telefono ? ` · ${comercio.telefono}` : ""}
        </p>
      </div>

      <div className="text-right text-xs text-texto-medio">
        <p className="font-bold text-azul">{nombrePlan(comercio)}</p>
        <p>
          {estado === "sin-plan"
            ? `Alta ${fechaCorta(comercio.creadoEn)}`
            : `Vence ${haceOEn(vence)}`}
        </p>
      </div>

      <span
        className={`rounded-full px-3 py-1 text-xs font-bold ${COLOR_ESTADO[estado]}`}
      >
        {ETIQUETA_ESTADO[estado]}
      </span>
    </li>
  );
}

function Tarjeta({
  titulo,
  valor,
  destacada = false,
}: {
  titulo: string;
  valor: number | null;
  destacada?: boolean;
}) {
  return (
    <div
      className={`rounded-2xl p-4 ${destacada ? "bg-azul text-white" : "bg-white"}`}
    >
      <p
        className={`text-xs font-bold uppercase tracking-wide ${
          destacada ? "text-white/60" : "text-texto-tenue"
        }`}
      >
        {titulo}
      </p>
      <p className="text-3xl font-black">{valor ?? "—"}</p>
    </div>
  );
}

function Mensaje({ texto }: { texto: string }) {
  return <p className="px-4 py-10 text-center text-sm text-texto-medio">{texto}</p>;
}
