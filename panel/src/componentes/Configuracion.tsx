"use client";

import { useEffect, useMemo, useState } from "react";
import {
  CAMPOS_ENLACES,
  ENLACES_VACIOS,
  guardarEnlaces,
  leerEnlaces,
  type Enlaces,
} from "@/lib/enlaces";
import { Hoja } from "./Hoja";

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
    <Hoja titulo="Configuración" alCerrar={alCerrar}>
      <section className="tarjeta p-4">
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
                        className={`campo mt-1 ${
                          errCampo ? "!border-rojo-alerta focus:!ring-rojo-alerta/15" : ""
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
                  className="btn-primario h-12 w-full"
                >
                  {guardando ? "Guardando…" : "Guardar enlaces"}
                </button>
              </div>
            )}
      </section>

      <p className="px-1 text-xs text-texto-tenue">
        Estos enlaces son globales: valen para toda la app. Si el guardado falla
        por permisos, tu cuenta no está dada de alta como operador.
      </p>
    </Hoja>
  );
}
