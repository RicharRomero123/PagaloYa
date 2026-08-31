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
  establecerSilenciado,
  listarCobros,
  listarMiembros,
  minutosAHora,
  quitarMiembro,
  type Cobro,
  type Miembro,
} from "@/lib/membresia";
import { AjusteManual } from "./AjusteManual";
import { FormularioMembresia } from "./FormularioMembresia";
import { Hoja } from "./Hoja";

/**
 * Ficha del comercio como panel deslizante (Hoja), no como página aparte.
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
  const [silenciandoUid, setSilenciandoUid] = useState<string | null>(null);
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
        setMiembros((previos) => (previos ?? []).filter((x) => x.uid !== m.uid));
      })
      .catch(() => {
        // quitarMiembro borra el miembro y baja numDispositivos en un lote
        // atómico (las reglas ya permiten al operador quitar trabajadores). Si
        // aún así falla, suele ser conexión o que las reglas no están al día.
        setErrorMiembro(
          "No se pudo quitar el teléfono. Revisa tu conexión e inténtalo de nuevo.",
        );
      })
      .finally(() => setQuitandoUid(null));
  }

  function alternarSilenciado(m: Miembro) {
    const nuevo = !m.silenciado;
    // Optimista: pinto el cambio de una vez y lo revierto solo si la escritura
    // falla. Así el toggle se siente instantáneo aunque la red esté lenta.
    setSilenciandoUid(m.uid);
    setErrorMiembro(null);
    setMiembros((previos) =>
      (previos ?? []).map((x) =>
        x.uid === m.uid ? { ...x, silenciado: nuevo } : x,
      ),
    );
    void establecerSilenciado(comercio.id, m.uid, nuevo)
      .catch(() => {
        // Revierto al estado que tenía antes de pulsar.
        setMiembros((previos) =>
          (previos ?? []).map((x) =>
            x.uid === m.uid ? { ...x, silenciado: m.silenciado } : x,
          ),
        );
        setErrorMiembro(
          "No se pudo cambiar la voz de ese teléfono. Revisa tu conexión e inténtalo de nuevo.",
        );
      })
      .finally(() => setSilenciandoUid(null));
  }

  return (
    <Hoja
      titulo={comercio.nombre}
      subtitulo={`Código ${comercio.codigoVinculacion || "—"} · alta ${fechaCorta(comercio.creadoEn)}`}
      alCerrar={alCerrar}
    >
      {/* Estado actual — tarjeta protagonista */}
      <div className="relative overflow-hidden rounded-card bg-azul-relieve p-4 text-white shadow-media">
        <span
          aria-hidden
          className="absolute -right-8 -top-10 h-28 w-28 rounded-full bg-naranja/25 blur-2xl"
        />
        <p className="text-[11px] font-bold uppercase tracking-wider text-white/60">
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
        <p
          className={`pastilla mt-2.5 ${
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

      {/* Código de vinculación */}
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
              className="btn-borde h-10 shrink-0 px-3"
            >
              Copiar
            </button>
          )}
        </div>
        <p className="mt-1.5 text-xs text-texto-tenue">
          Es lo que el dueño escribe en la app para vincular otro teléfono.
        </p>
      </Bloque>

      {/* Contacto */}
      {(comercio.telefono || comercio.direccion) && (
        <Bloque titulo="Contacto">
          {comercio.telefono && (
            <a
              href={`https://wa.me/51${comercio.telefono.replace(/\D/g, "")}`}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1.5 font-bold text-naranja underline underline-offset-4"
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

      {/* Salud de los teléfonos */}
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
                  <p className="truncate text-sm font-bold text-azul">
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
                <span className={`pastilla shrink-0 ${COLOR_SALUD[salud]}`}>
                  {ETIQUETA_SALUD[salud]}
                </span>
              </li>
            );
          })}
        </ul>
      </Bloque>

      {/* Teléfonos vinculados */}
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
          <p className="mb-2 rounded-lg bg-rojo-suave px-2.5 py-1.5 text-xs font-semibold text-rojo-alerta">
            {errorMiembro}
          </p>
        )}
        <ul className="space-y-2.5">
          {(miembros ?? []).map((m) => (
            <li
              key={m.uid}
              className="flex flex-wrap items-center justify-between gap-x-3 gap-y-1.5"
            >
              <span className="min-w-0 truncate text-sm text-azul">
                {m.nombre}
                <span className="text-texto-tenue">
                  {" "}
                  · {m.rol === "dueno" ? "Dueño" : "Trabajador"}
                </span>
              </span>
              <div className="flex shrink-0 items-center gap-2">
                <span
                  className={`pastilla ${
                    m.puedeCapturar
                      ? "bg-naranja-suave text-naranja-hondo"
                      : "bg-verde-suave text-verde-ok"
                  }`}
                >
                  {m.puedeCapturar ? "Captura" : "Escucha"}
                </span>
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
              {/* Cómo anuncia este teléfono. El silenciado es accionable desde
                  el panel (cortesía del operador); el trabajador puede volver a
                  activar la voz desde su app. El horario es solo lectura. */}
              <div className="flex w-full flex-wrap items-center gap-1.5">
                <button
                  type="button"
                  disabled={silenciandoUid !== null}
                  onClick={() => alternarSilenciado(m)}
                  aria-pressed={m.silenciado}
                  title={
                    m.silenciado
                      ? "Encender la voz de este teléfono. El trabajador también puede activarla desde su app."
                      : "Apagar la voz de este teléfono. El trabajador puede volver a activarla desde su app."
                  }
                  className={`pastilla transition disabled:opacity-50 ${
                    m.silenciado
                      ? "bg-rojo-suave text-rojo-alerta hover:bg-rojo-alerta hover:text-white"
                      : "bg-humo text-texto-medio hover:bg-borde"
                  }`}
                >
                  {silenciandoUid === m.uid
                    ? "Guardando…"
                    : m.silenciado
                      ? "🔇 Silenciado"
                      : "🔊 Suena"}
                </button>
                <span
                  className={`pastilla ${
                    m.horarioActivo
                      ? "bg-ambar-suave text-ambar-aviso"
                      : "bg-humo text-texto-medio"
                  }`}
                >
                  {m.horarioActivo
                    ? `🕐 ${minutosAHora(m.horarioInicio)}–${minutosAHora(m.horarioFin)}`
                    : "🕐 A toda hora"}
                </span>
              </div>
            </li>
          ))}
        </ul>
      </Bloque>

      {/* Historial de cobros */}
      <Bloque titulo="Historial de cobros">
        {cobros === null && <Tenue texto="Cargando…" />}
        {cobros?.length === 0 && <Tenue texto="Todavía no se le ha cobrado." />}
        <ul className="divide-y divide-borde/70">
          {(cobros ?? []).map((c) => (
            <li key={c.id} className="flex items-center justify-between py-2.5">
              <div className="min-w-0">
                <p className="text-sm font-black text-azul">{soles(c.monto)}</p>
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
          <p className="mt-3 border-t border-borde pt-3 text-sm text-texto-medio">
            Total cobrado:{" "}
            <strong className="text-azul">
              {soles(cobros.reduce((t, c) => t + c.monto, 0))}
            </strong>
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
          className="btn w-full border border-rojo-alerta/25 bg-rojo-suave text-rojo-alerta hover:bg-rojo-alerta hover:text-white disabled:opacity-50"
        >
          {cortando ? "Cortando…" : "Cortar plan ahora"}
        </button>
      )}
    </Hoja>
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
    <section className="tarjeta p-4">
      <h3 className="mb-2 text-[11px] font-bold uppercase tracking-wider text-texto-tenue">
        {titulo}
      </h3>
      {children}
    </section>
  );
}

function Tenue({ texto }: { texto: string }) {
  return <p className="text-sm text-texto-medio">{texto}</p>;
}
