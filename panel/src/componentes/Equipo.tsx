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
  const [uid, setUid] = useState("");
  const [nombre, setNombre] = useState("");
  const [nivel, setNivel] = useState<NivelOperador>("operador");
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function recargar() {
    setEquipo(await listarOperadores());
  }

  useEffect(() => {
    void listarOperadores().then(setEquipo).catch(() => setEquipo([]));
  }, []);

  useEffect(() => {
    const alTeclear = (e: KeyboardEvent) => {
      if (e.key === "Escape") alCerrar();
    };
    window.addEventListener("keydown", alTeclear);
    return () => window.removeEventListener("keydown", alTeclear);
  }, [alCerrar]);

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
    <div className="fixed inset-0 z-50 flex">
      <button
        type="button"
        aria-label="Cerrar"
        onClick={alCerrar}
        className="flex-1 bg-azul/30"
      />
      <aside className="h-full w-full overflow-y-auto bg-crema sm:max-w-md">
        <header className="sticky top-0 flex items-center justify-between bg-crema px-4 py-4">
          <h2 className="text-xl font-black">Equipo de PagoYa</h2>
          <button
            type="button"
            onClick={alCerrar}
            className="rounded-full bg-white px-3 py-1.5 text-sm font-bold text-texto-medio"
          >
            Cerrar
          </button>
        </header>

        <div className="space-y-3 px-4 pb-16">
          {soyDueno && (
            <section className="rounded-2xl bg-white p-4">
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
                    className="mt-1 h-11 w-full rounded-xl border border-borde px-3 font-mono text-sm outline-none focus:border-naranja"
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
                    className="mt-1 h-11 w-full rounded-xl border border-borde px-3 outline-none focus:border-naranja"
                  />
                </label>

                <div className="flex gap-2">
                  {(["operador", "dueno"] as NivelOperador[]).map((n) => (
                    <button
                      key={n}
                      type="button"
                      onClick={() => setNivel(n)}
                      className={`flex-1 rounded-xl px-3 py-2.5 text-sm font-bold transition ${
                        nivel === n
                          ? "bg-naranja text-white"
                          : "bg-humo text-texto-medio hover:bg-naranja-suave"
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
                  className="h-12 w-full rounded-xl bg-naranja font-bold text-white transition hover:bg-naranja-hondo disabled:opacity-50"
                >
                  {guardando ? "Guardando…" : "Dar acceso"}
                </button>
              </div>
            </section>
          )}

          <section className="rounded-2xl bg-white p-4">
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
            para que el último dueño no se quede fuera por error. Si hace falta,
            se arregla desde la consola de Firebase.
          </p>
        </div>
      </aside>
    </div>
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
