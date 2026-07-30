"use client";

import { useState } from "react";
import { salir } from "@/lib/sesion";
import type { User } from "firebase/auth";

/**
 * No es un error: es la puerta cerrada funcionando. Pasa cuando un comerciante
 * (o cualquiera con cuenta de PagoYa) abre la URL del panel. Se le dice claro
 * y se le ofrece la salida, sin filtrar nada del negocio.
 */
export function SinAcceso({ usuario }: { usuario: User | null }) {
  const [copiado, setCopiado] = useState(false);

  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <div className="w-full max-w-sm text-center">
        <div className="mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-humo text-4xl">
          🔒
        </div>
        <h1 className="text-2xl font-black text-azul">Esta cuenta no tiene acceso</h1>
        <p className="mt-2 text-sm text-texto-medio">
          El panel es solo para el equipo de PagoYa. Si eres comerciante, todo lo
          tuyo está en la app: tus pagos, tu caja y tu equipo.
        </p>

        {usuario && (
          <div className="mt-6 rounded-xl bg-white p-4 text-left text-sm">
            <p className="text-texto-tenue">Entraste como</p>
            <p className="font-bold">{usuario.email ?? usuario.uid}</p>

            <p className="mt-4 text-texto-tenue">
              Si eres del equipo, pásale este código a quien te dio el acceso:
            </p>
            <code className="mt-1 block break-all rounded-lg bg-humo p-2 font-mono text-xs">
              {usuario.uid}
            </code>
            <button
              type="button"
              onClick={() => {
                void navigator.clipboard.writeText(usuario.uid).then(() => {
                  setCopiado(true);
                  window.setTimeout(() => setCopiado(false), 2000);
                });
              }}
              className="mt-2 text-xs font-bold text-naranja underline underline-offset-4"
            >
              {copiado ? "¡Copiado!" : "Copiar código"}
            </button>
          </div>
        )}

        <button
          type="button"
          onClick={() => void salir()}
          className="mt-6 h-12 w-full rounded-xl bg-naranja font-bold text-white transition hover:bg-naranja-hondo"
        >
          Salir
        </button>
      </div>
    </main>
  );
}
