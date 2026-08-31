"use client";

import Image from "next/image";
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
    <main className="relative flex min-h-[100dvh] items-center justify-center overflow-hidden px-4 py-10">
      {/* Halo cálido detrás de la tarjeta */}
      <div
        aria-hidden
        className="pointer-events-none absolute left-1/2 top-[18%] h-72 w-72 -translate-x-1/2 rounded-full bg-naranja/25 blur-[90px]"
      />

      <div className="w-full max-w-sm animate-subir">
        <div className="mb-7 flex flex-col items-center text-center">
          <div className="mb-4 rounded-[1.4rem] bg-azul-relieve p-2 shadow-alta ring-1 ring-white/10">
            <Image
              src="/icon-192.png"
              alt="PagoYa"
              width={64}
              height={64}
              className="h-16 w-16 rounded-2xl"
              priority
            />
          </div>
          <h1 className="text-3xl font-black tracking-tight text-azul">
            Pago<span className="text-naranja">Ya</span>
          </h1>
          <p className="mt-0.5 text-sm font-semibold text-texto-medio">
            Panel de operador
          </p>
        </div>

        <div className="tarjeta p-6 shadow-alta">
          <button
            type="button"
            disabled={cargando}
            onClick={() =>
              intentar(
                entrarConGoogle,
                "No se pudo entrar con Google. Intenta de nuevo.",
              )
            }
            className="btn-borde h-12 w-full"
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

          <div className="my-5 flex items-center gap-3">
            <span className="h-px flex-1 bg-borde" />
            <span className="text-xs font-bold uppercase tracking-wide text-texto-tenue">
              o con tu correo
            </span>
            <span className="h-px flex-1 bg-borde" />
          </div>

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
              className="campo h-12"
            />
            <input
              type="password"
              value={clave}
              onChange={(e) => setClave(e.target.value)}
              placeholder="Contraseña"
              autoComplete="current-password"
              className="campo h-12"
            />
            <button type="submit" disabled={cargando} className="btn-azul h-12 w-full">
              {cargando ? "Entrando…" : "Entrar"}
            </button>
          </form>

          {error && (
            <p className="mt-4 rounded-xl bg-rojo-suave px-3 py-2 text-center text-sm font-semibold text-rojo-alerta">
              {error}
            </p>
          )}
        </div>

        <p className="mt-6 text-center text-xs font-semibold text-texto-tenue">
          Acceso solo para el equipo de PagoYa.
        </p>
      </div>
    </main>
  );
}
