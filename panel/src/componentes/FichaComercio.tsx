"use client";

import { useEffect, useState } from "react";
import {
  ETIQUETA_ESTADO,
  estadoDe,
  nombrePlan,
  type Comercio,
  type Suscripcion,
} from "@/lib/comercios";
import { fechaCorta, haceOEn, soles } from "@/lib/formato";
import {
  ETIQUETA_METODO,
  cortarPlan,
  listarCobros,
  listarMiembros,
  type Cobro,
  type Miembro,
} from "@/lib/membresia";
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
  const [cortando, setCortando] = useState(false);

  useEffect(() => {
    let vigente = true;
    void listarCobros(comercio.id).then((c) => vigente && setCobros(c));
    void listarMiembros(comercio.id).then((m) => vigente && setMiembros(m));
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
          </div>

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

          {/* Quién usa la app en este comercio */}
          <Bloque titulo="Equipo">
            {miembros === null && <Tenue texto="Cargando…" />}
            {miembros?.length === 0 && <Tenue texto="Nadie vinculado todavía." />}
            <ul className="space-y-2">
              {(miembros ?? []).map((m) => (
                <li key={m.uid} className="flex items-center justify-between">
                  <span className="truncate text-sm">
                    {m.nombre}
                    <span className="text-texto-tenue">
                      {" "}
                      · {m.rol === "dueno" ? "Dueño" : "Trabajador"}
                    </span>
                  </span>
                  <span
                    className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-bold ${
                      m.puedeCapturar
                        ? "bg-naranja-suave text-naranja-hondo"
                        : "bg-verde-suave text-verde-ok"
                    }`}
                  >
                    {m.puedeCapturar ? "Captura" : "Escucha"}
                  </span>
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
