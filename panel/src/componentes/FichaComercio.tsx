"use client";

import { useEffect, useState } from "react";
import {
  ETIQUETA_ESTADO,
  estadoDe,
  maxDispositivosDe,
  nombrePlan,
  type Comercio,
  type Suscripcion,
} from "@/lib/comercios";
import {
  COLOR_SALUD,
  ETIQUETA_SALUD,
  dispositivosDe,
  saludDe,
  type Dispositivo,
} from "@/lib/dispositivos";
import { fechaCorta, haceOEn, haceRato, soles } from "@/lib/formato";
import {
  ETIQUETA_METODO,
  cortarPlan,
  listarCobros,
  listarMiembros,
  quitarMiembro,
  type Cobro,
  type Miembro,
} from "@/lib/membresia";
import { AjusteManual } from "./AjusteManual";
import { FormularioMembresia } from "./FormularioMembresia";

/**
 * Ficha del comercio como panel deslizante, no como página aparte.
 *
 * Con `output: "export"` no existen rutas dinámicas (Next necesitaría conocer
 * los ids al compilar), y de paso en celular un panel se cierra más rápido que
 * navegar y volver.
 */
export function FichaComercio({
  comercio,
  operadorUid,
  alCerrar,
  alActualizar,
}: {
  comercio: Comercio;
  operadorUid: string;
  alCerrar: () => void;
  alActualizar: (s: Suscripcion) => void;
}) {
  const [cobros, setCobros] = useState<Cobro[] | null>(null);
  const [miembros, setMiembros] = useState<Miembro[] | null>(null);
  const [telefonos, setTelefonos] = useState<Dispositivo[] | null>(null);
  const [cortando, setCortando] = useState(false);
  const [quitandoUid, setQuitandoUid] = useState<string | null>(null);
  const [errorMiembro, setErrorMiembro] = useState<string | null>(null);

  useEffect(() => {
    let vigente = true;
    void listarCobros(comercio.id).then((c) => vigente && setCobros(c));
    void listarMiembros(comercio.id).then((m) => vigente && setMiembros(m));
    void dispositivosDe(comercio.id).then((t) => vigente && setTelefonos(t));
    return () => {
      vigente = false;
    };
  }, [comercio.id]);

  useEffect(() => {
    const alTeclear = (e: KeyboardEvent) => {
      if (e.key === "Escape") alCerrar();
    };
    window.addEventListener("keydown", alTeclear);
    return () => window.removeEventListener("keydown", alTeclear);
  }, [alCerrar]);

  const estado = estadoDe(comercio);
  const vence = comercio.suscripcion?.vigenteHasta ?? 0;
  const enPrueba = comercio.suscripcion?.estado === "prueba";
  const tope = maxDispositivosDe(comercio);
  const vinculados = miembros?.length ?? 0;

  function desvincular(m: Miembro) {
    if (
      !window.confirm(
        `¿Quitar a ${m.nombre} del comercio? Ese teléfono dejará de estar vinculado.`,
      )
    ) {
      return;
    }
    setQuitandoUid(m.uid);
    setErrorMiembro(null);
    void quitarMiembro(comercio.id, m.uid)
      .then(() => {
        setMiembros((previos) =>
          (previos ?? []).filter((x) => x.uid !== m.uid),
        );
      })
      .catch(() => {
        // El caso más probable es permission-denied: las reglas hoy no dejan
        // al operador borrar miembros (ver quitarMiembro en lib/membresia).
        setErrorMiembro(
          "No se pudo quitar. Las reglas de Firestore todavía no permiten al operador desvincular teléfonos (falta agregar || esOperador() al delete de miembros).",
        );
      })
      .finally(() => setQuitandoUid(null));
  }

  return (
    <div className="fixed inset-0 z-50 flex">
      <button
        type="button"
        aria-label="Cerrar"
        onClick={alCerrar}
        className="flex-1 bg-azul/30"
      />
      <aside className="h-full w-full overflow-y-auto bg-crema sm:max-w-md">
        <header className="sticky top-0 flex items-start justify-between gap-3 bg-crema px-4 py-4">
          <div className="min-w-0">
            <h2 className="truncate text-xl font-black">{comercio.nombre}</h2>
            <p className="text-xs text-texto-medio">
              Código {comercio.codigoVinculacion || "—"} · alta{" "}
              {fechaCorta(comercio.creadoEn)}
            </p>
          </div>
          <button
            type="button"
            onClick={alCerrar}
            className="rounded-full bg-white px-3 py-1.5 text-sm font-bold text-texto-medio"
          >
            Cerrar
          </button>
        </header>

        <div className="space-y-3 px-4 pb-16">
          {/* Estado actual */}
          <div className="rounded-2xl bg-azul p-4 text-white">
            <p className="text-xs font-bold uppercase tracking-wide text-white/60">
              Membresía
            </p>
            <p className="text-2xl font-black">
              {nombrePlan(comercio)}
              {enPrueba && (
                <span className="ml-2 align-middle text-xs font-bold text-white/70">
                  en prueba
                </span>
              )}
            </p>
            <p className="text-sm text-white/70">
              {estado === "sin-plan"
                ? "Sin plan activo"
                : `${ETIQUETA_ESTADO[estado]} · vence ${haceOEn(vence)} (${fechaCorta(vence)})`}
            </p>
            {/* Cuántos teléfonos usa vs. cuántos le permite el plan */}
            <p
              className={`mt-2 inline-block rounded-full px-2.5 py-1 text-xs font-bold ${
                miembros === null
                  ? "bg-white/10 text-white/60"
                  : vinculados > tope
                    ? "bg-rojo-alerta text-white"
                    : "bg-white/15 text-white"
              }`}
            >
              {miembros === null
                ? "Contando teléfonos…"
                : `${vinculados} de ${tope} ${tope === 1 ? "teléfono" : "teléfonos"}`}
              {miembros !== null && vinculados > tope && " · pasado del tope"}
            </p>
          </div>

          {/* Código de vinculación: para reenviárselo al comercio por WhatsApp */}
          <Bloque titulo="Código de vinculación">
            <div className="flex items-center justify-between gap-3">
              <p className="font-mono text-2xl font-black tracking-widest text-azul">
                {comercio.codigoVinculacion || "—"}
              </p>
              {comercio.codigoVinculacion && (
                <button
                  type="button"
                  onClick={() => {
                    void navigator.clipboard
                      ?.writeText(comercio.codigoVinculacion)
                      .catch(() => {});
                  }}
                  className="shrink-0 rounded-xl bg-humo px-3 py-2 text-sm font-bold text-azul transition hover:bg-naranja-suave"
                >
                  Copiar
                </button>
              )}
            </div>
            <p className="mt-1 text-xs text-texto-tenue">
              Es lo que el dueño escribe en la app para vincular otro teléfono.
            </p>
          </Bloque>

          {/* Contacto: el teléfono con enlace directo a WhatsApp */}
          {(comercio.telefono || comercio.direccion) && (
            <Bloque titulo="Contacto">
              {comercio.telefono && (
                <a
                  href={`https://wa.me/51${comercio.telefono.replace(/\D/g, "")}`}
                  target="_blank"
                  rel="noreferrer"
                  className="block font-bold text-naranja underline underline-offset-4"
                >
                  {comercio.telefono}
                </a>
              )}
              {comercio.direccion && (
                <p className="text-sm text-texto-medio">{comercio.direccion}</p>
              )}
            </Bloque>
          )}

          <FormularioMembresia
            comercio={comercio}
            operadorUid={operadorUid}
            alActualizar={(s) => {
              alActualizar(s);
              void listarCobros(comercio.id).then(setCobros);
            }}
          />

          <AjusteManual comercio={comercio} alActualizar={alActualizar} />

          {/* Salud de los teléfonos: por qué "no suena" antes de que llamen */}
          <Bloque titulo="Teléfonos">
            {telefonos === null && <Tenue texto="Cargando…" />}
            {telefonos?.length === 0 && (
              <Tenue texto="Ningún teléfono ha reportado todavía." />
            )}
            <ul className="space-y-2.5">
              {(telefonos ?? []).map((t) => {
                const { salud, detalle } = saludDe(t);
                return (
                  <li key={t.uid} className="flex items-center justify-between gap-3">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-bold">
                        {t.nombre}
                        <span className="font-normal text-texto-tenue">
                          {" "}
                          · {t.marca} {t.modelo}
                        </span>
                      </p>
                      <p className="truncate text-xs text-texto-medio">
                        {detalle} · latido {haceRato(t.ultimoLatido)} · v{t.versionApp}
                      </p>
                    </div>
                    <span
                      className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-bold ${COLOR_SALUD[salud]}`}
                    >
                      {ETIQUETA_SALUD[salud]}
                    </span>
                  </li>
                );
              })}
            </ul>
          </Bloque>

          {/* Quién usa la app en este comercio: teléfonos vinculados */}
          <Bloque
            titulo={
              miembros === null
                ? "Teléfonos vinculados"
                : `Teléfonos vinculados · ${vinculados} de ${tope}`
            }
          >
            {miembros === null && <Tenue texto="Cargando…" />}
            {miembros?.length === 0 && <Tenue texto="Nadie vinculado todavía." />}
            {errorMiembro && (
              <p className="mb-2 text-xs text-rojo-alerta">{errorMiembro}</p>
            )}
            <ul className="space-y-2.5">
              {(miembros ?? []).map((m) => (
                <li
                  key={m.uid}
                  className="flex items-center justify-between gap-3"
                >
                  <span className="min-w-0 truncate text-sm">
                    {m.nombre}
                    <span className="text-texto-tenue">
                      {" "}
                      · {m.rol === "dueno" ? "Dueño" : "Trabajador"}
                    </span>
                  </span>
                  <div className="flex shrink-0 items-center gap-2">
                    <span
                      className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                        m.puedeCapturar
                          ? "bg-naranja-suave text-naranja-hondo"
                          : "bg-verde-suave text-verde-ok"
                      }`}
                    >
                      {m.puedeCapturar ? "Captura" : "Escucha"}
                    </span>
                    {/* El dueño no se saca desde aquí: se borraría el comercio
                        entero de hecho. Solo trabajadores. */}
                    {m.rol !== "dueno" && (
                      <button
                        type="button"
                        disabled={quitandoUid !== null}
                        onClick={() => desvincular(m)}
                        className="text-xs font-bold text-rojo-alerta underline underline-offset-4 disabled:opacity-40"
                      >
                        {quitandoUid === m.uid ? "Quitando…" : "Quitar"}
                      </button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          </Bloque>

          {/* Contabilidad */}
          <Bloque titulo="Historial de cobros">
            {cobros === null && <Tenue texto="Cargando…" />}
            {cobros?.length === 0 && <Tenue texto="Todavía no se le ha cobrado." />}
            <ul className="divide-y divide-borde">
              {(cobros ?? []).map((c) => (
                <li key={c.id} className="flex items-center justify-between py-2.5">
                  <div className="min-w-0">
                    <p className="text-sm font-bold">{soles(c.monto)}</p>
                    <p className="truncate text-xs text-texto-medio">
                      {ETIQUETA_METODO[c.metodo]} · {fechaCorta(c.creadoEn)}
                      {c.nota ? ` · ${c.nota}` : ""}
                    </p>
                  </div>
                  <p className="shrink-0 text-xs text-texto-tenue">
                    hasta {fechaCorta(c.periodoHasta)}
                  </p>
                </li>
              ))}
            </ul>
            {cobros && cobros.length > 0 && (
              <p className="mt-3 border-t border-borde pt-3 text-sm">
                Total cobrado:{" "}
                <strong>{soles(cobros.reduce((t, c) => t + c.monto, 0))}</strong>
              </p>
            )}
          </Bloque>

          {estado !== "sin-plan" && (
            <button
              type="button"
              disabled={cortando}
              onClick={() => {
                if (!window.confirm(`¿Cortar el plan de ${comercio.nombre}?`)) return;
                setCortando(true);
                void cortarPlan(comercio.id)
                  .then(alActualizar)
                  .finally(() => setCortando(false));
              }}
              className="w-full py-3 text-sm font-bold text-rojo-alerta"
            >
              Cortar plan ahora
            </button>
          )}
        </div>
      </aside>
    </div>
  );
}

function Bloque({
  titulo,
  children,
}: {
  titulo: string;
  children: React.ReactNode;
}) {
  return (
    <section className="rounded-2xl bg-white p-4">
      <h3 className="mb-2 text-xs font-bold uppercase tracking-wide text-texto-tenue">
        {titulo}
      </h3>
      {children}
    </section>
  );
}

function Tenue({ texto }: { texto: string }) {
  return <p className="text-sm text-texto-medio">{texto}</p>;
}
