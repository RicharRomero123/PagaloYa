"use client";

import { useEffect, useState } from "react";
import {
  PLANTILLAS,
  TOPICS,
  enviarCampana,
  listarCampanas,
  type Campana,
  type Plantilla,
  type Topic,
} from "@/lib/campanas";
import { fechaCorta, haceRato } from "@/lib/formato";
import { Hoja } from "./Hoja";

const MAX_TITULO = 60;
const MAX_CUERPO = 180;

/** Etiqueta corta de un topic para pintarla en el historial. */
function etiquetaTopic(topic: Topic): string {
  return TOPICS.find((t) => t.id === topic)?.etiqueta ?? topic;
}

/**
 * Panel de campañas push. Mismo patrón deslizable que Configuracion/Equipo.
 * El envío real corre en la Cloud Function enviarCampana; aquí armamos el
 * mensaje, elegimos topic y confirmamos. Enviar a `todos` exige un segundo
 * paso: es toda la base y no se quema la lista por gusto (PUSH-CAMPANAS.md §3).
 */
export function Campanas({ alCerrar }: { alCerrar: () => void }) {
  const [titulo, setTitulo] = useState("");
  const [cuerpo, setCuerpo] = useState("");
  const [topic, setTopic] = useState<Topic>("todos");
  const [confirmandoTodos, setConfirmandoTodos] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [ok, setOk] = useState(false);
  const [historial, setHistorial] = useState<Campana[] | null>(null);

  async function recargarHistorial() {
    try {
      setHistorial(await listarCampanas());
    } catch {
      setHistorial([]);
    }
  }

  useEffect(() => {
    void recargarHistorial();
  }, []);

  const tituloLimpio = titulo.trim();
  const cuerpoLimpio = cuerpo.trim();
  const listoParaEnviar =
    tituloLimpio.length >= 1 &&
    tituloLimpio.length <= MAX_TITULO &&
    cuerpoLimpio.length >= 1 &&
    cuerpoLimpio.length <= MAX_CUERPO;

  function usarPlantilla(p: Plantilla) {
    setOk(false);
    setError(null);
    setConfirmandoTodos(false);
    setTitulo(p.titulo);
    setCuerpo(p.cuerpo);
    setTopic(p.topicSugerido);
  }

  function cambiarTopic(nuevo: Topic) {
    setConfirmandoTodos(false);
    setTopic(nuevo);
  }

  async function despachar() {
    setEnviando(true);
    setError(null);
    setOk(false);
    try {
      await enviarCampana(tituloLimpio, cuerpoLimpio, topic);
      setOk(true);
      setConfirmandoTodos(false);
      setTitulo("");
      setCuerpo("");
      await recargarHistorial();
    } catch (e) {
      const codigo = (e as { code?: string })?.code ?? "";
      const mensaje = (e as { message?: string })?.message ?? "";
      if (codigo === "functions/permission-denied" || codigo === "permission-denied") {
        setError("Solo un operador puede enviar campañas.");
      } else if (
        codigo === "functions/invalid-argument" ||
        codigo === "invalid-argument"
      ) {
        setError(mensaje || "El título, el cuerpo o el destino no son válidos.");
      } else if (
        codigo === "functions/unauthenticated" ||
        codigo === "unauthenticated"
      ) {
        setError("Tu sesión venció. Vuelve a entrar e intenta de nuevo.");
      } else if (codigo === "functions/not-found" || codigo === "not-found") {
        setError(
          "La función de campañas no está desplegada todavía. Corre " +
            "«firebase deploy --only functions» y vuelve a intentar.",
        );
      } else {
        // Deja ver el detalle real (código FCM/servidor) en vez de esconderlo:
        // clave para diagnosticar. También va completo a la consola del navegador.
        console.error("enviarCampana falló:", codigo, mensaje, e);
        setError(
          `No se pudo enviar la campaña${codigo ? ` (${codigo})` : ""}. ` +
            (mensaje || "Revisa tu conexión e intenta de nuevo."),
        );
      }
    } finally {
      setEnviando(false);
    }
  }

  function alPresionarEnviar() {
    if (!listoParaEnviar || enviando) return;
    // "todos" va a toda la base: pedimos un segundo OK explícito.
    if (topic === "todos" && !confirmandoTodos) {
      setConfirmandoTodos(true);
      return;
    }
    void despachar();
  }

  return (
    <Hoja titulo="Campañas" alCerrar={alCerrar}>
      <section className="tarjeta p-4">
        <h3 className="text-base font-black text-azul">Nueva campaña</h3>
            <p className="mt-1 text-sm text-texto-medio">
              Un aviso push a los teléfonos con PagoYa. No es un pago: es un
              mensaje del equipo. Título corto, una sola idea, tono criollo.
            </p>

            {/* Plantillas rápidas */}
            <div className="mt-4 flex flex-wrap gap-2">
              {PLANTILLAS.map((p) => (
                <button
                  key={p.etiqueta}
                  type="button"
                  onClick={() => usarPlantilla(p)}
                  className="rounded-full border border-borde bg-white px-3 py-1.5 text-xs font-bold text-texto-medio transition-all hover:border-naranja/50 hover:text-azul active:scale-95"
                >
                  {p.etiqueta}
                </button>
              ))}
            </div>

            <div className="mt-4 space-y-4">
              {/* Título */}
              <label className="block">
                <span className="flex items-center justify-between text-xs font-bold uppercase text-texto-tenue">
                  Título
                  <span
                    className={
                      titulo.length > MAX_TITULO ? "text-rojo-alerta" : "text-texto-tenue"
                    }
                  >
                    {titulo.length}/{MAX_TITULO}
                  </span>
                </span>
                <input
                  value={titulo}
                  maxLength={MAX_TITULO}
                  onChange={(e) => {
                    setOk(false);
                    setConfirmandoTodos(false);
                    setTitulo(e.target.value);
                  }}
                  placeholder="¡PagoYa está fino!"
                  className="campo mt-1"
                />
              </label>

              {/* Cuerpo */}
              <label className="block">
                <span className="flex items-center justify-between text-xs font-bold uppercase text-texto-tenue">
                  Cuerpo
                  <span
                    className={
                      cuerpo.length > MAX_CUERPO ? "text-rojo-alerta" : "text-texto-tenue"
                    }
                  >
                    {cuerpo.length}/{MAX_CUERPO}
                  </span>
                </span>
                <textarea
                  value={cuerpo}
                  maxLength={MAX_CUERPO}
                  rows={3}
                  onChange={(e) => {
                    setOk(false);
                    setConfirmandoTodos(false);
                    setCuerpo(e.target.value);
                  }}
                  placeholder="Actualizamos la app pa' que tus pagos suenen más rápido, casero."
                  className="mt-1 w-full resize-none rounded-xl border border-borde bg-white px-3 py-2.5 text-azul outline-none transition placeholder:text-texto-tenue focus:border-naranja focus:ring-4 focus:ring-naranja/15"
                />
              </label>

              {/* Topic */}
              <div>
                <span className="text-xs font-bold uppercase text-texto-tenue">
                  A quién le llega
                </span>
                <div className="mt-2 space-y-2">
                  {TOPICS.map((t) => {
                    const activo = topic === t.id;
                    return (
                      <button
                        key={t.id}
                        type="button"
                        onClick={() => cambiarTopic(t.id)}
                        className={`block w-full rounded-xl border px-3 py-2.5 text-left transition-all active:scale-[0.99] ${
                          activo
                            ? "border-naranja bg-naranja-suave shadow-suave ring-1 ring-naranja/30"
                            : "border-borde bg-white hover:border-naranja/40 hover:bg-crema"
                        }`}
                      >
                        <span className="text-sm font-bold text-azul">
                          {t.etiqueta}
                        </span>
                        <span className="block text-xs text-texto-medio">
                          {t.descripcion}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {error && (
                <p className="rounded-xl bg-rojo-suave px-3 py-2 text-sm font-bold text-rojo-alerta">
                  {error}
                </p>
              )}
              {ok && (
                <p className="rounded-xl bg-verde-suave px-3 py-2 text-sm font-bold text-verde-ok">
                  ¡Campaña enviada, casero!
                </p>
              )}

              {/* Confirmación extra para "todos" */}
              {topic === "todos" && confirmandoTodos && (
                <div className="rounded-xl bg-ambar-suave px-3 py-3">
                  <p className="text-sm font-bold text-ambar-aviso">
                    Esto va a TODA la base de teléfonos. No quemes la lista:
                    manda solo si vale la pena.
                  </p>
                  <div className="mt-3 flex gap-2">
                    <button
                      type="button"
                      disabled={enviando}
                      onClick={() => void despachar()}
                      className="btn-primario h-11 flex-1"
                    >
                      {enviando ? "Enviando…" : "Sí, enviar a todos"}
                    </button>
                    <button
                      type="button"
                      disabled={enviando}
                      onClick={() => setConfirmandoTodos(false)}
                      className="btn-borde h-11"
                    >
                      Mejor no
                    </button>
                  </div>
                </div>
              )}

              {!(topic === "todos" && confirmandoTodos) && (
                <button
                  type="button"
                  disabled={!listoParaEnviar || enviando}
                  onClick={alPresionarEnviar}
                  className="btn-primario h-12 w-full"
                >
                  {enviando ? "Enviando…" : "Enviar campaña"}
                </button>
              )}
            </div>
          </section>

      {/* Historial */}
      <section className="tarjeta p-4">
        <h3 className="mb-3 text-[11px] font-bold uppercase tracking-wider text-texto-tenue">
              Últimas campañas
            </h3>
            {historial === null && (
              <p className="text-sm text-texto-medio">Cargando…</p>
            )}
            {historial && historial.length === 0 && (
              <p className="text-sm text-texto-tenue">
                Todavía no se ha enviado ninguna campaña.
              </p>
            )}
            <ul className="divide-y divide-borde">
              {(historial ?? []).map((c) => (
                <li key={c.id} className="py-3">
                  <div className="flex items-center justify-between gap-2">
                    <span className="rounded-full bg-humo px-2.5 py-0.5 text-xs font-bold text-texto-medio">
                      {etiquetaTopic(c.topic)}
                    </span>
                    <span className="shrink-0 text-xs text-texto-tenue">
                      {c.enviadoEn
                        ? `${fechaCorta(c.enviadoEn)} · ${haceRato(c.enviadoEn)}`
                        : "—"}
                    </span>
                  </div>
                  <p className="mt-1 font-bold">{c.titulo}</p>
                  <p className="text-sm text-texto-medio">{c.cuerpo}</p>
                  {c.operadorNombre && (
                    <p className="mt-0.5 text-xs text-texto-tenue">
                      por {c.operadorNombre}
                    </p>
                  )}
                </li>
              ))}
            </ul>
      </section>

      <p className="px-1 text-xs text-texto-tenue">
        El envío corre en el servidor (Cloud Function). Si falla por permisos,
        tu cuenta no está dada de alta como operador.
      </p>
    </Hoja>
  );
}
