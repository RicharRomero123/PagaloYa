"use client";

import { useEffect, useMemo, useState } from "react";
import {
  CAMPOS_ENLACES,
  ENLACES_VACIOS,
  guardarEnlaces,
  leerEnlaces,
  type Enlaces,
} from "@/lib/enlaces";

/**
 * Configuración global del producto. Hoy solo edita los enlaces
 * (config/enlaces) que la app Android lee en caliente, pero está armada para
 * sumar más bloques de ajustes sin rehacer la vista.
 */
export function Configuracion({ alCerrar }: { alCerrar: () => void }) {
  const [enlaces, setEnlaces] = useState<Enlaces | null>(null);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [ok, setOk] = useState(false);

  useEffect(() => {
    let vigente = true;
    leerEnlaces()
      .then((e) => {
        if (vigente) setEnlaces(e);
      })
      .catch(() => {
        // Si falla la lectura arrancamos con campos vacíos: el operador igual
        // puede escribir y guardar.
        if (vigente) {
          setEnlaces({ ...ENLACES_VACIOS });
          setError("No se pudieron cargar los enlaces guardados. Revisa tu conexión.");
        }
      });
    return () => {
      vigente = false;
    };
  }, []);

  useEffect(() => {
    const alTeclear = (e: KeyboardEvent) => {
      if (e.key === "Escape") alCerrar();
    };
    window.addEventListener("keydown", alTeclear);
    return () => window.removeEventListener("keydown", alTeclear);
  }, [alCerrar]);

  // Errores de validación por campo, recalculados en vivo.
  const erroresCampo = useMemo(() => {
    const mapa: Partial<Record<keyof Enlaces, string>> = {};
    if (!enlaces) return mapa;
    for (const campo of CAMPOS_ENLACES) {
      const msg = campo.validar?.(enlaces[campo.clave].trim());
      if (msg) mapa[campo.clave] = msg;
    }
    return mapa;
  }, [enlaces]);

  const hayErrores = Object.keys(erroresCampo).length > 0;

  function cambiar(clave: keyof Enlaces, valor: string) {
    setOk(false);
    setEnlaces((previo) => ({ ...(previo ?? ENLACES_VACIOS), [clave]: valor }));
  }

  async function guardar() {
    if (!enlaces || hayErrores) return;
    setGuardando(true);
    setError(null);
    setOk(false);
    try {
      await guardarEnlaces(enlaces);
      setOk(true);
    } catch (e) {
      // permission-denied = no es operador (o las reglas no están desplegadas).
      const codigo = (e as { code?: string })?.code ?? "";
      if (codigo === "permission-denied") {
        setError(
          "No tienes permiso para guardar. Solo un operador puede editar los enlaces.",
        );
      } else {
        setError("No se pudo guardar. Revisa tu conexión e intenta de nuevo.");
      }
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
          <h2 className="text-xl font-black">Configuración</h2>
          <button
            type="button"
            onClick={alCerrar}
            className="rounded-full bg-white px-3 py-1.5 text-sm font-bold text-texto-medio"
          >
            Cerrar
          </button>
        </header>

        <div className="space-y-3 px-4 pb-16">
          <section className="rounded-2xl bg-white p-4">
            <h3 className="font-black">Enlaces del producto</h3>
            <p className="mt-1 text-sm text-texto-medio">
              WhatsApp de ventas y redes que la app muestra a los comercios. Se
              guardan en <code className="font-mono text-xs">config/enlaces</code>{" "}
              y la app los lee en caliente: no hace falta actualizar el APK.
            </p>

            {enlaces === null ? (
              <p className="mt-4 text-sm text-texto-medio">Cargando enlaces…</p>
            ) : (
              <div className="mt-4 space-y-4">
                {CAMPOS_ENLACES.map((campo) => {
                  const errCampo = erroresCampo[campo.clave];
                  return (
                    <label key={campo.clave} className="block">
                      <span className="text-xs font-bold uppercase text-texto-tenue">
                        {campo.etiqueta}
                      </span>
                      <input
                        value={enlaces[campo.clave]}
                        onChange={(e) => cambiar(campo.clave, e.target.value)}
                        placeholder={campo.placeholder}
                        inputMode={
                          campo.clave === "whatsappVentas" ? "numeric" : "text"
                        }
                        className={`mt-1 h-11 w-full rounded-xl border px-3 outline-none focus:border-naranja ${
                          errCampo ? "border-rojo-alerta" : "border-borde"
                        }`}
                      />
                      <span
                        className={`mt-1 block text-xs ${
                          errCampo ? "text-rojo-alerta" : "text-texto-tenue"
                        }`}
                      >
                        {errCampo ?? campo.ayuda}
                      </span>
                    </label>
                  );
                })}

                {error && (
                  <p className="rounded-xl bg-rojo-suave px-3 py-2 text-sm font-bold text-rojo-alerta">
                    {error}
                  </p>
                )}
                {ok && (
                  <p className="rounded-xl bg-verde-suave px-3 py-2 text-sm font-bold text-verde-ok">
                    Enlaces guardados. La app los tomará en su próxima lectura.
                  </p>
                )}

                <button
                  type="button"
                  disabled={guardando || hayErrores}
                  onClick={() => void guardar()}
                  className="h-12 w-full rounded-xl bg-naranja font-bold text-white transition hover:bg-naranja-hondo disabled:opacity-50"
                >
                  {guardando ? "Guardando…" : "Guardar enlaces"}
                </button>
              </div>
            )}
          </section>

          <p className="px-1 text-xs text-texto-tenue">
            Estos enlaces son globales: valen para toda la app. Si el guardado
            falla por permisos, tu cuenta no está dada de alta como operador.
          </p>
        </div>
      </aside>
    </div>
  );
}
