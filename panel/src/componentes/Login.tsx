"use client";

import { useState } from "react";
import { entrarConCorreo, entrarConGoogle } from "@/lib/sesion";

export function Login() {
  const [correo, setCorreo] = useState("");
  const [clave, setClave] = useState("");
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function intentar(accion: () => Promise<void>, mensajeError: string) {
    setCargando(true);
    setError(null);
    try {
      await accion();
    } catch {
      setError(mensajeError);
    } finally {
      setCargando(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-naranja-suave text-4xl">
            🔊
          </div>
          <h1 className="text-3xl font-black text-naranja">PagoYa</h1>
          <p className="text-sm text-texto-medio">Panel de operador</p>
        </div>

        <div className="rounded-2xl bg-white p-6 shadow-sm">
          <button
            type="button"
            disabled={cargando}
            onClick={() =>
              intentar(
                entrarConGoogle,
                "No se pudo entrar con Google. Intenta de nuevo.",
              )
            }
            className="h-12 w-full rounded-xl bg-azul font-bold text-white transition hover:bg-azul-claro disabled:opacity-50"
          >
            Entrar con Google
          </button>

          <p className="my-4 text-center text-sm text-texto-tenue">
            — o con tu correo —
          </p>

          <form
            onSubmit={(e) => {
              e.preventDefault();
              if (!correo || clave.length < 6) {
                setError("Pon tu correo y una contraseña de al menos 6 caracteres.");
                return;
              }
              void intentar(
                () => entrarConCorreo(correo, clave),
                "Correo o contraseña incorrectos.",
              );
            }}
            className="space-y-3"
          >
            <input
              type="email"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              placeholder="Correo"
              autoComplete="username"
              className="h-12 w-full rounded-xl border border-borde px-4 outline-none focus:border-naranja"
            />
            <input
              type="password"
              value={clave}
              onChange={(e) => setClave(e.target.value)}
              placeholder="Contraseña"
              autoComplete="current-password"
              className="h-12 w-full rounded-xl border border-borde px-4 outline-none focus:border-naranja"
            />
            <button
              type="submit"
              disabled={cargando}
              className="h-12 w-full rounded-xl border-2 border-borde font-bold text-azul transition hover:border-naranja disabled:opacity-50"
            >
              {cargando ? "Entrando…" : "Entrar"}
            </button>
          </form>

          {error && (
            <p className="mt-4 text-center text-sm text-rojo-alerta">{error}</p>
          )}
        </div>

        <p className="mt-6 text-center text-xs text-texto-tenue">
          Acceso solo para el equipo de PagoYa.
        </p>
      </div>
    </main>
  );
}
