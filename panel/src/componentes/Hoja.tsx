"use client";

import { useEffect } from "react";

/**
 * Panel deslizante (drawer) compartido por Ficha, Equipo, Config y Campañas.
 *
 * Centraliza el chrome: backdrop con desenfoque, drawer con sombra y esquina
 * redondeada en desktop, header glass con título + botón cerrar, tecla Escape y
 * bloqueo del scroll de fondo. El contenido va como children, ya con el padding
 * estándar y separación entre secciones.
 */
export function Hoja({
  titulo,
  subtitulo,
  alCerrar,
  children,
}: {
  titulo: string;
  subtitulo?: string;
  alCerrar: () => void;
  children: React.ReactNode;
}) {
  useEffect(() => {
    const alTeclear = (e: KeyboardEvent) => {
      if (e.key === "Escape") alCerrar();
    };
    window.addEventListener("keydown", alTeclear);
    const previo = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", alTeclear);
      document.body.style.overflow = previo;
    };
  }, [alCerrar]);

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button
        type="button"
        aria-label="Cerrar"
        onClick={alCerrar}
        className="absolute inset-0 animate-aparecer bg-azul-hondo/40 backdrop-blur-sm"
      />

      <aside className="relative flex h-full w-full max-w-md animate-deslizar flex-col overflow-hidden bg-crema shadow-alta sm:rounded-l-[1.75rem]">
        <header className="flex items-start justify-between gap-3 border-b border-borde/60 bg-crema/85 px-4 py-3.5 backdrop-blur-xl">
          <div className="min-w-0">
            <h2 className="truncate text-lg font-black tracking-tight text-azul">
              {titulo}
            </h2>
            {subtitulo && (
              <p className="truncate text-xs font-semibold text-texto-medio">
                {subtitulo}
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={alCerrar}
            aria-label="Cerrar"
            className="grid h-9 w-9 shrink-0 place-items-center rounded-full border border-borde bg-white text-texto-medio transition hover:border-naranja hover:text-naranja active:scale-95"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth={2}
              strokeLinecap="round"
              className="h-4 w-4"
              aria-hidden="true"
            >
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        </header>

        <div className="flex-1 overflow-y-auto px-4 pb-24 pt-3">
          <div className="space-y-3">{children}</div>
        </div>
      </aside>
    </div>
  );
}
