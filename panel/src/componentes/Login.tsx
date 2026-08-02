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
            className="flex h-12 w-full items-center justify-center gap-3 rounded-xl border border-borde bg-white font-bold text-azul transition hover:bg-humo disabled:opacity-50"
          >
            <svg className="h-5 w-5" viewBox="0 0 48 48" aria-hidden="true">
              <path
                fill="#4285F4"
                d="M45.12 24.5c0-1.56-.14-3.06-.4-4.5H24v8.51h11.84c-.51 2.75-2.06 5.08-4.39 6.64v5.52h7.11c4.16-3.83 6.56-9.47 6.56-16.17z"
              />
              <path
                fill="#34A853"
                d="M24 46c5.94 0 10.92-1.97 14.56-5.33l-7.11-5.52c-1.97 1.32-4.49 2.1-7.45 2.1-5.73 0-10.58-3.87-12.31-9.07H4.34v5.7C7.96 41.07 15.4 46 24 46z"
              />
              <path
                fill="#FBBC05"
                d="M11.69 28.18C11.25 26.86 11 25.45 11 24s.25-2.86.69-4.18v-5.7H4.34C2.85 17.09 2 20.45 2 24s.85 6.91 2.34 9.88l7.35-5.7z"
              />
              <path
                fill="#EA4335"
                d="M24 10.75c3.23 0 6.13 1.11 8.41 3.29l6.31-6.31C34.91 4.18 29.93 2 24 2 15.4 2 7.96 6.93 4.34 14.12l7.35 5.7c1.73-5.2 6.58-9.07 12.31-9.07z"
              />
            </svg>
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
