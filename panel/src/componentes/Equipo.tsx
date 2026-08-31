"use client";

import { useEffect, useState } from "react";
import {
  ETIQUETA_NIVEL,
  agregarOperador,
  cambiarNivel,
  definirActivo,
  eliminarOperador,
  listarOperadores,
  type NivelOperador,
  type Operador,
} from "@/lib/operadores";
import {
  ReglasNoListas,
  aceptarSolicitud,
  listarSolicitudesPendientes,
  rechazarSolicitud,
  type Solicitud,
} from "@/lib/solicitudes";
import { Hoja } from "./Hoja";

export function Equipo({
  miUid,
  soyDueno,
  alCerrar,
}: {
  miUid: string;
  soyDueno: boolean;
  alCerrar: () => void;
}) {
  const [equipo, setEquipo] = useState<Operador[] | null>(null);
  const [solicitudes, setSolicitudes] = useState<Solicitud[] | null>(null);
  const [avisoSolicitudes, setAvisoSolicitudes] = useState<string | null>(null);
  const [uid, setUid] = useState("");
  const [nombre, setNombre] = useState("");
  const [nivel, setNivel] = useState<NivelOperador>("operador");
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function recargar() {
    setEquipo(await listarOperadores());
  }

  async function recargarSolicitudes() {
    setAvisoSolicitudes(null);
    try {
      setSolicitudes(await listarSolicitudesPendientes());
    } catch (e) {
      setSolicitudes([]);
      setAvisoSolicitudes(
        e instanceof ReglasNoListas
          ? "Las reglas de acceso aún no están activas, así que no se pueden leer las postulaciones todavía."
          : "No se pudieron cargar las postulaciones.",
      );
    }
  }

  useEffect(() => {
    void listarOperadores().then(setEquipo).catch(() => setEquipo([]));
  }, []);

  useEffect(() => {
    if (soyDueno) void recargarSolicitudes();
  }, [soyDueno]);

  async function ejecutar(accion: () => Promise<void>, falla: string) {
    setGuardando(true);
    setError(null);
    try {
      await accion();
      await recargar();
    } catch {
      setError(falla);
    } finally {
      setGuardando(false);
    }
  }

  return (
    <Hoja titulo="Equipo de PagoYa" alCerrar={alCerrar}>
      {soyDueno && (
        <section className="tarjeta p-4">
              <div className="flex items-center justify-between">
                <h3 className="font-black">Solicitudes de acceso</h3>
                {solicitudes && solicitudes.length > 0 && (
                  <span className="rounded-full bg-naranja px-2.5 py-1 text-xs font-bold text-white">
                    {solicitudes.length}
                  </span>
                )}
              </div>
              <p className="mt-1 text-sm text-texto-medio">
                Gente que postuló desde la pantalla de acceso. Tú decides quién entra.
              </p>

              {avisoSolicitudes && (
                <p className="mt-3 rounded-xl bg-humo p-3 text-sm text-texto-medio">
                  {avisoSolicitudes}
                </p>
              )}

              {solicitudes === null && !avisoSolicitudes && (
                <p className="mt-3 text-sm text-texto-medio">Cargando…</p>
              )}

              {solicitudes && solicitudes.length === 0 && !avisoSolicitudes && (
                <p className="mt-3 text-sm text-texto-tenue">
                  No hay postulaciones pendientes por ahora.
                </p>
              )}

              <ul className="mt-3 space-y-3">
                {(solicitudes ?? []).map((s) => (
                  <FilaSolicitud
                    key={s.uid}
                    solicitud={s}
                    ocupado={guardando}
                    alAceptar={(nivelElegido) =>
                      void ejecutar(async () => {
                        await aceptarSolicitud(s, nivelElegido);
                        await recargarSolicitudes();
                      }, "No se pudo aceptar la solicitud.")
                    }
                    alRechazar={() =>
                      void ejecutar(async () => {
                        await rechazarSolicitud(s.uid);
                        await recargarSolicitudes();
                      }, "No se pudo rechazar la solicitud.")
                    }
                  />
                ))}
              </ul>
            </section>
          )}

          {soyDueno && (
            <section className="tarjeta p-4">
              <h3 className="font-black">Dar de alta a alguien</h3>
              <p className="mt-1 text-sm text-texto-medio">
                Pídele que entre al panel y te dicte el código que le sale en
                pantalla. Sin ese código no se le puede dar acceso.
              </p>

              <div className="mt-4 space-y-3">
                <label className="block">
                  <span className="text-xs font-bold uppercase text-texto-tenue">
                    Su código de usuario (UID)
                  </span>
                  <input
                    value={uid}
                    onChange={(e) => setUid(e.target.value)}
                    placeholder="Ej. k3Jd8fH2mNq..."
                    className="campo mt-1 font-mono text-sm"
                  />
                </label>
                <label className="block">
                  <span className="text-xs font-bold uppercase text-texto-tenue">
                    Nombre
                  </span>
                  <input
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    placeholder="María Quispe"
                    className="campo mt-1"
                  />
                </label>

                <div className="flex gap-2">
                  {(["operador", "dueno"] as NivelOperador[]).map((n) => (
                    <button
                      key={n}
                      type="button"
                      onClick={() => setNivel(n)}
                      className={`flex-1 rounded-xl px-3 py-2.5 text-sm font-bold transition-all active:scale-95 ${
                        nivel === n
                          ? "bg-naranja-relieve text-white shadow-naranja"
                          : "border border-borde bg-white text-texto-medio hover:border-naranja/50 hover:text-azul"
                      }`}
                    >
                      {ETIQUETA_NIVEL[n]}
                    </button>
                  ))}
                </div>
                <p className="text-xs text-texto-tenue">
                  {nivel === "dueno"
                    ? "⚠️ Un dueño puede dar de alta y quitar a cualquiera del equipo, incluido a ti."
                    : "Cobra membresías y ve los comercios. No puede tocar el equipo."}
                </p>

                {error && <p className="text-sm text-rojo-alerta">{error}</p>}

                <button
                  type="button"
                  disabled={guardando}
                  onClick={() => {
                    if (uid.trim().length < 10) {
                      setError("Ese código no parece válido. Debe tener más de 10 caracteres.");
                      return;
                    }
                    if (!nombre.trim()) {
                      setError("Ponle un nombre para reconocerlo después.");
                      return;
                    }
                    void ejecutar(async () => {
                      await agregarOperador(uid, nombre, nivel);
                      setUid("");
                      setNombre("");
                      setNivel("operador");
                    }, "No se pudo dar de alta. Revisa que el código esté bien copiado.");
                  }}
                  className="btn-primario h-12 w-full"
                >
                  {guardando ? "Guardando…" : "Dar acceso"}
                </button>
              </div>
            </section>
          )}

          <section className="tarjeta p-4">
            <h3 className="mb-3 text-xs font-bold uppercase tracking-wide text-texto-tenue">
              Con acceso al panel
            </h3>
            {equipo === null && (
              <p className="text-sm text-texto-medio">Cargando…</p>
            )}
            <ul className="divide-y divide-borde">
              {(equipo ?? []).map((o) => (
                <Fila
                  key={o.uid}
                  operador={o}
                  esYo={o.uid === miUid}
                  puedoEditar={soyDueno && o.uid !== miUid}
                  ocupado={guardando}
                  alCambiarActivo={(activo) =>
                    void ejecutar(
                      () => definirActivo(o.uid, activo),
                      "No se pudo cambiar el acceso.",
                    )
                  }
                  alCambiarNivel={(n) =>
                    void ejecutar(
                      () => cambiarNivel(o.uid, n),
                      "No se pudo cambiar el nivel.",
                    )
                  }
                  alEliminar={() => {
                    if (!window.confirm(`¿Quitar a ${o.nombre} del equipo?`)) return;
                    void ejecutar(
                      () => eliminarOperador(o.uid),
                      "No se pudo eliminar.",
                    );
                  }}
                />
              ))}
            </ul>
          </section>

      <p className="px-1 text-xs text-texto-tenue">
        Nadie puede editarse ni borrarse a sí mismo, ni siquiera un dueño. Es
        para que el último dueño no se quede fuera por error. Si hace falta, se
        arregla desde la consola de Firebase.
      </p>
    </Hoja>
  );
}

function FilaSolicitud({
  solicitud,
  ocupado,
  alAceptar,
  alRechazar,
}: {
  solicitud: Solicitud;
  ocupado: boolean;
  alAceptar: (nivel: NivelOperador) => void;
  alRechazar: () => void;
}) {
  const [nivel, setNivel] = useState<NivelOperador>("operador");

  return (
    <li className="rounded-xl border border-borde bg-white p-3 shadow-suave">
      <p className="font-bold text-azul">{solicitud.nombre}</p>
      {solicitud.email && (
        <p className="truncate text-xs text-texto-tenue">{solicitud.email}</p>
      )}
      <p className="truncate font-mono text-[11px] text-texto-tenue">
        {solicitud.uid}
      </p>
      {solicitud.notaRol && (
        <p className="mt-1 text-sm text-texto-medio">“{solicitud.notaRol}”</p>
      )}

      <div className="mt-3 flex gap-2">
        {(["operador", "dueno"] as NivelOperador[]).map((n) => (
          <button
            key={n}
            type="button"
            onClick={() => setNivel(n)}
            className={`flex-1 rounded-lg px-2 py-1.5 text-xs font-bold transition-all active:scale-95 ${
              nivel === n
                ? "bg-naranja-relieve text-white shadow-naranja"
                : "border border-borde bg-crema text-texto-medio hover:text-azul"
            }`}
          >
            {ETIQUETA_NIVEL[n]}
          </button>
        ))}
      </div>

      <div className="mt-2 flex gap-2">
        <button
          type="button"
          disabled={ocupado}
          onClick={() => alAceptar(nivel)}
          className="btn-primario h-10 flex-1"
        >
          Aceptar
        </button>
        <button
          type="button"
          disabled={ocupado}
          onClick={() => {
            if (!window.confirm(`¿Rechazar la postulación de ${solicitud.nombre}?`))
              return;
            alRechazar();
          }}
          className="btn-borde h-10 text-rojo-alerta hover:border-rojo-alerta hover:bg-rojo-suave"
        >
          Rechazar
        </button>
      </div>
    </li>
  );
}

function Fila({
  operador,
  esYo,
  puedoEditar,
  ocupado,
  alCambiarActivo,
  alCambiarNivel,
  alEliminar,
}: {
  operador: Operador;
  esYo: boolean;
  puedoEditar: boolean;
  ocupado: boolean;
  alCambiarActivo: (activo: boolean) => void;
  alCambiarNivel: (nivel: NivelOperador) => void;
  alEliminar: () => void;
}) {
  return (
    <li className="py-3">
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate font-bold">
            {operador.nombre}
            {esYo && <span className="text-texto-tenue"> · tú</span>}
          </p>
          <p className="truncate font-mono text-xs text-texto-tenue">
            {operador.uid}
          </p>
        </div>
        <span
          className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-bold ${
            !operador.activo
              ? "bg-humo text-texto-tenue"
              : operador.nivel === "dueno"
                ? "bg-naranja-suave text-naranja-hondo"
                : "bg-verde-suave text-verde-ok"
          }`}
        >
          {operador.activo ? ETIQUETA_NIVEL[operador.nivel] : "Sin acceso"}
        </span>
      </div>

      {puedoEditar && (
        <div className="mt-2 flex flex-wrap gap-3 text-xs font-bold">
          <button
            type="button"
            disabled={ocupado}
            onClick={() => alCambiarActivo(!operador.activo)}
            className="text-azul underline underline-offset-4 disabled:opacity-50"
          >
            {operador.activo ? "Quitar acceso" : "Devolver acceso"}
          </button>
          <button
            type="button"
            disabled={ocupado}
            onClick={() =>
              alCambiarNivel(operador.nivel === "dueno" ? "operador" : "dueno")
            }
            className="text-azul underline underline-offset-4 disabled:opacity-50"
          >
            {operador.nivel === "dueno" ? "Bajar a operador" : "Subir a dueño"}
          </button>
          <button
            type="button"
            disabled={ocupado}
            onClick={alEliminar}
            className="text-rojo-alerta underline underline-offset-4 disabled:opacity-50"
          >
            Eliminar
          </button>
        </div>
      )}
    </li>
  );
}
