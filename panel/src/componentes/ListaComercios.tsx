"use client";

import Image from "next/image";
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
import { listarDispositivos, type Dispositivo } from "@/lib/dispositivos";
import { fechaCorta, haceOEn } from "@/lib/formato";
import { salir } from "@/lib/sesion";
import { Campanas } from "./Campanas";
import { Configuracion } from "./Configuracion";
import { Equipo } from "./Equipo";
import { FichaComercio } from "./FichaComercio";
import { SaludTelefonos } from "./SaludTelefonos";

const COLOR_ESTADO: Record<EstadoMembresia, string> = {
  "al-dia": "bg-verde-suave text-verde-ok",
  "por-vencer": "bg-ambar-suave text-ambar-aviso",
  vencida: "bg-rojo-suave text-rojo-alerta",
  "sin-plan": "bg-humo text-texto-medio",
};

const PUNTO_ESTADO: Record<EstadoMembresia, string> = {
  "al-dia": "bg-verde-ok",
  "por-vencer": "bg-ambar-aviso",
  vencida: "bg-rojo-alerta",
  "sin-plan": "bg-texto-tenue",
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
  soyDueno,
}: {
  nombreOperador: string;
  operadorUid: string;
  soyDueno: boolean;
}) {
  const [verEquipo, setVerEquipo] = useState(false);
  const [verConfig, setVerConfig] = useState(false);
  const [verCampanas, setVerCampanas] = useState(false);
  const [comercios, setComercios] = useState<Comercio[] | null>(null);
  const [dispositivos, setDispositivos] = useState<Dispositivo[] | null>(null);
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
    // El semáforo es un extra: si falla (p. ej. falta el índice), el panel
    // sigue funcionando y solo no se muestra la sección.
    listarDispositivos()
      .then((lista) => {
        if (vigente) setDispositivos(lista);
      })
      .catch(() => {});
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
    <>
      {/* ── Barra superior sticky, glass ──────────────────────────────── */}
      <header className="sticky top-0 z-30 border-b border-borde/60 bg-crema/80 backdrop-blur-xl">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3">
          <div className="flex min-w-0 items-center gap-2.5">
            <Image
              src="/icon-192.png"
              alt="PagoYa"
              width={38}
              height={38}
              className="h-9 w-9 shrink-0 rounded-xl shadow-suave"
              priority
            />
            <div className="min-w-0 leading-tight">
              <h1 className="text-lg font-black tracking-tight text-azul">
                Pago<span className="text-naranja">Ya</span>
              </h1>
              <p className="truncate text-[11px] font-semibold text-texto-medio">
                Panel de operador{nombreOperador ? ` · ${nombreOperador}` : ""}
              </p>
            </div>
          </div>

          <nav className="flex items-center gap-0.5 sm:gap-1">
            <NavBtn onClick={() => setVerConfig(true)} icono={<IcoEngranaje />}>
              Config
            </NavBtn>
            <NavBtn onClick={() => setVerCampanas(true)} icono={<IcoMegafono />}>
              Campañas
            </NavBtn>
            <NavBtn onClick={() => setVerEquipo(true)} icono={<IcoEquipo />}>
              Equipo
            </NavBtn>
            <button
              type="button"
              onClick={() => void salir()}
              className="btn-fantasma h-9 px-2.5 text-texto-tenue"
              title="Salir"
            >
              <IcoSalir />
              <span className="hidden sm:inline">Salir</span>
            </button>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 pb-20 pt-5">
        {/* ── Tarjetas resumen ────────────────────────────────────────── */}
        <section className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <TarjetaDestacada valor={comercios?.length ?? null} />
          <Tarjeta
            titulo="Vencidos"
            valor={comercios ? conteos.vencida : null}
            estado="vencida"
          />
          <Tarjeta
            titulo="Vencen pronto"
            valor={comercios ? conteos["por-vencer"] : null}
            estado="por-vencer"
          />
          <Tarjeta
            titulo="Al día"
            valor={comercios ? conteos["al-dia"] : null}
            estado="al-dia"
          />
        </section>

        <SaludTelefonos
          dispositivos={dispositivos}
          comercios={comercios}
          alAbrirComercio={setAbiertoId}
        />

        {/* ── Buscador + filtros ──────────────────────────────────────── */}
        <div className="mt-7 flex flex-col gap-3 lg:flex-row lg:items-center">
          <div className="relative flex-1">
            <span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-texto-tenue">
              <IcoBuscar />
            </span>
            <input
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              placeholder="Buscar por nombre, código o teléfono…"
              className="campo h-12 pl-11"
            />
          </div>
          <div className="flex flex-wrap gap-2">
            {ORDEN_FILTROS.map((f) => {
              const activo = filtro === f;
              return (
                <button
                  key={f}
                  type="button"
                  onClick={() => setFiltro(f)}
                  className={`pastilla h-9 transition-all active:scale-95 ${
                    activo
                      ? "bg-azul-relieve text-white shadow-media"
                      : "border border-borde bg-white text-texto-medio hover:border-naranja/50 hover:text-azul"
                  }`}
                >
                  {f !== "todos" && (
                    <span
                      className={`h-1.5 w-1.5 rounded-full ${
                        activo ? "bg-white/80" : PUNTO_ESTADO[f]
                      }`}
                    />
                  )}
                  {f === "todos" ? "Todos" : ETIQUETA_ESTADO[f]}
                </button>
              );
            })}
          </div>
        </div>

        {/* ── Lista ───────────────────────────────────────────────────── */}
        <div className="mt-4 overflow-hidden tarjeta">
          {error && <Mensaje texto={error} />}

          {!error && comercios === null && (
            <ul className="divide-y divide-borde/70">
              {Array.from({ length: 6 }).map((_, i) => (
                <FilaEsqueleto key={i} />
              ))}
            </ul>
          )}

          {!error && comercios !== null && visibles.length === 0 && (
            <Mensaje
              texto={
                comercios.length === 0
                  ? "Todavía no hay comercios registrados."
                  : "Ningún comercio coincide con la búsqueda."
              }
            />
          )}

          {!error && comercios !== null && visibles.length > 0 && (
            <ul className="divide-y divide-borde/70">
              {visibles.map((c) => (
                <Fila key={c.id} comercio={c} alAbrir={() => setAbiertoId(c.id)} />
              ))}
            </ul>
          )}
        </div>

        {comercios !== null && comercios.length >= 200 && (
          <p className="mt-4 text-center text-xs text-texto-tenue">
            Mostrando los 200 comercios más recientes. Toca paginar cuando pases de
            aquí — no subas el tope o te comes la cuota diaria de lecturas.
          </p>
        )}
      </main>

      {abierto && (
        <FichaComercio
          comercio={abierto}
          operadorUid={operadorUid}
          alCerrar={() => setAbiertoId(null)}
          alActualizar={(s) => actualizarSuscripcion(abierto.id, s)}
        />
      )}

      {verEquipo && (
        <Equipo
          miUid={operadorUid}
          soyDueno={soyDueno}
          alCerrar={() => setVerEquipo(false)}
        />
      )}

      {verConfig && <Configuracion alCerrar={() => setVerConfig(false)} />}

      {verCampanas && <Campanas alCerrar={() => setVerCampanas(false)} />}
    </>
  );
}

function NavBtn({
  onClick,
  icono,
  children,
}: {
  onClick: () => void;
  icono: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <button type="button" onClick={onClick} className="btn-fantasma h-9 px-2.5">
      {icono}
      <span className="hidden md:inline">{children}</span>
    </button>
  );
}

function Fila({ comercio, alAbrir }: { comercio: Comercio; alAbrir: () => void }) {
  const estado = estadoDe(comercio);
  const vence = comercio.suscripcion?.vigenteHasta ?? 0;
  const inicial = comercio.nombre.trim().charAt(0).toUpperCase() || "·";

  return (
    <li
      role="button"
      tabIndex={0}
      onClick={alAbrir}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") alAbrir();
      }}
      className="group flex cursor-pointer items-center gap-3 px-3 py-3 transition-colors hover:bg-crema sm:px-4"
    >
      <div className="grid h-11 w-11 shrink-0 place-items-center rounded-2xl bg-naranja-suave text-lg font-black text-naranja-hondo">
        {inicial}
      </div>

      <div className="min-w-0 flex-1">
        <p className="truncate font-bold text-azul">{comercio.nombre}</p>
        <p className="truncate text-xs text-texto-medio">
          Código {comercio.codigoVinculacion || "—"}
          {comercio.direccion ? ` · ${comercio.direccion}` : ""}
          {comercio.telefono ? ` · ${comercio.telefono}` : ""}
        </p>
      </div>

      <div className="hidden text-right text-xs text-texto-medio sm:block">
        <p className="font-bold text-azul">{nombrePlan(comercio)}</p>
        <p>
          {estado === "sin-plan"
            ? `Alta ${fechaCorta(comercio.creadoEn)}`
            : `Vence ${haceOEn(vence)}`}
        </p>
      </div>

      <span className={`pastilla shrink-0 ${COLOR_ESTADO[estado]}`}>
        <span className={`h-1.5 w-1.5 rounded-full ${PUNTO_ESTADO[estado]}`} />
        {ETIQUETA_ESTADO[estado]}
      </span>

      <span className="shrink-0 text-texto-tenue transition-transform group-hover:translate-x-0.5 group-hover:text-naranja">
        <IcoChevron />
      </span>
    </li>
  );
}

function FilaEsqueleto() {
  return (
    <div className="flex items-center gap-3 px-3 py-3 sm:px-4">
      <div className="esqueleto h-11 w-11 rounded-2xl" />
      <div className="flex-1 space-y-2">
        <div className="esqueleto h-3.5 w-2/5" />
        <div className="esqueleto h-2.5 w-3/5" />
      </div>
      <div className="esqueleto h-6 w-20 rounded-full" />
    </div>
  );
}

function TarjetaDestacada({ valor }: { valor: number | null }) {
  return (
    <div className="relative overflow-hidden rounded-card bg-azul-relieve p-4 text-white shadow-media">
      {/* onda decorativa */}
      <span
        aria-hidden
        className="absolute -right-6 -top-8 h-24 w-24 rounded-full bg-naranja/25 blur-2xl"
      />
      <p className="text-[11px] font-bold uppercase tracking-wider text-white/60">
        Comercios
      </p>
      {valor === null ? (
        <div className="esqueleto mt-1.5 h-9 w-16 bg-white/20" />
      ) : (
        <p className="text-4xl font-black leading-none">{valor}</p>
      )}
      <p className="mt-1 text-[11px] font-semibold text-white/50">Registrados</p>
    </div>
  );
}

function Tarjeta({
  titulo,
  valor,
  estado,
}: {
  titulo: string;
  valor: number | null;
  estado: EstadoMembresia;
}) {
  return (
    <div className="tarjeta p-4">
      <div className="flex items-center gap-1.5">
        <span className={`h-2 w-2 rounded-full ${PUNTO_ESTADO[estado]}`} />
        <p className="text-[11px] font-bold uppercase tracking-wider text-texto-tenue">
          {titulo}
        </p>
      </div>
      {valor === null ? (
        <div className="esqueleto mt-1.5 h-8 w-12" />
      ) : (
        <p className="text-3xl font-black leading-none text-azul">{valor}</p>
      )}
    </div>
  );
}

function Mensaje({ texto }: { texto: string }) {
  return <p className="px-4 py-12 text-center text-sm text-texto-medio">{texto}</p>;
}

/* ── Íconos (stroke, 1.75) ──────────────────────────────────────────────── */
function base(props: { children: React.ReactNode }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.75}
      strokeLinecap="round"
      strokeLinejoin="round"
      className="h-4 w-4"
      aria-hidden="true"
    >
      {props.children}
    </svg>
  );
}
const IcoEngranaje = () =>
  base({
    children: (
      <>
        <circle cx="12" cy="12" r="3" />
        <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.6a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9c.14.63.66 1.1 1.31 1.13H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
      </>
    ),
  });
const IcoMegafono = () =>
  base({
    children: (
      <>
        <path d="m3 11 15-6v14L3 13z" />
        <path d="M3 11v2a2 2 0 0 0 2 2h1v3a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-3" />
      </>
    ),
  });
const IcoEquipo = () =>
  base({
    children: (
      <>
        <circle cx="9" cy="8" r="3" />
        <path d="M3 20a6 6 0 0 1 12 0" />
        <path d="M16 5.5a3 3 0 0 1 0 5.5M18 20a6 6 0 0 0-3-5" />
      </>
    ),
  });
const IcoSalir = () =>
  base({
    children: (
      <>
        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
        <path d="m16 17 5-5-5-5M21 12H9" />
      </>
    ),
  });
const IcoBuscar = () =>
  base({
    children: (
      <>
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
      </>
    ),
  });
const IcoChevron = () => base({ children: <path d="m9 18 6-6-6-6" /> });
