'use client';

import { useEffect, useState } from 'react';
import { IconoLogo, IconoWsp } from './Iconos';
import { wsp, MENSAJE_GENERAL } from '../lib/enlaces';

const SECCIONES = [
  { id: 'que-es', texto: 'Qué es' },
  { id: 'como-funciona', texto: 'Cómo funciona' },
  // "¿Funciona con mi billetera?" es la primera pregunta de todos: va en el menú.
  // "Para quién" entra en lugar de "Comparar": con el mercado abierto a taxis,
  // delivery y transporte, saber si aplica a TU caso pesa más que comparar.
  // La sección de comparación sigue en la página, solo sale del menú (a 8
  // enlaces la fila ya no entra junto al logo y el botón).
  { id: 'para-quien', texto: 'Para quién' },
  { id: 'compatibilidad', texto: 'Compatibilidad' },
  { id: 'planes', texto: 'Planes' },
  { id: 'preguntas', texto: 'Preguntas' },
];

// Las guías son páginas propias, no anclas: van aparte en el menú.
const GUIAS_ENLACE = { href: '/guias/', texto: 'Guías' };

/** Cabecera con toldo de mercado. `conNav` muestra las anclas (solo en la home). */
export default function Cabecera({ conNav = false }) {
  const [abierto, setAbierto] = useState(false);

  // Cerrar con Escape y no dejar que el fondo se mueva con el menú abierto
  useEffect(() => {
    if (!abierto) return;
    const alTeclear = (e) => {
      if (e.key === 'Escape') setAbierto(false);
    };
    window.addEventListener('keydown', alTeclear);
    return () => window.removeEventListener('keydown', alTeclear);
  }, [abierto]);

  return (
    <>
      <header className={`cabecera${conNav ? ' cabecera--con-nav' : ''}`}>
        <div className="wrap">
          <a className="logo" href="/" aria-label="PagoYa, inicio">
            <IconoLogo />
            {/* Wordmark oficial: "Pago" azul + "Ya" naranja. El alto lo fija el CSS. */}
            <img
              className="logo-wordmark"
              src="/assets/wordmark-pagoya.png"
              alt="PagoYa"
              width="480"
              height="173"
            />
          </a>

          {conNav && (
            <nav className="nav-anclas" aria-label="Secciones">
              {SECCIONES.map((s) => (
                <a key={s.id} href={`#${s.id}`}>
                  {s.texto}
                </a>
              ))}
              <a href={GUIAS_ENLACE.href}>{GUIAS_ENLACE.texto}</a>
            </nav>
          )}

          <a
            className="btn btn-wsp btn-chico js-wsp cab-cta"
            data-donde="cabecera"
            href={wsp(MENSAJE_GENERAL)}
            target="_blank"
            rel="noopener"
          >
            Escríbenos
          </a>

          {conNav && (
            <button
              type="button"
              className="hamburguesa"
              aria-expanded={abierto}
              aria-controls="menu-movil"
              aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'}
              onClick={() => setAbierto((v) => !v)}
            >
              <span aria-hidden="true"></span>
              <span aria-hidden="true"></span>
              <span aria-hidden="true"></span>
            </button>
          )}
        </div>

        {conNav && abierto && (
          <nav id="menu-movil" className="menu-movil" aria-label="Secciones">
            <div className="wrap">
              {SECCIONES.map((s) => (
                <a key={s.id} href={`#${s.id}`} onClick={() => setAbierto(false)}>
                  {s.texto}
                </a>
              ))}
              <a href={GUIAS_ENLACE.href} onClick={() => setAbierto(false)}>
                {GUIAS_ENLACE.texto}
              </a>
              <a
                className="btn btn-wsp js-wsp"
                data-donde="menu-movil"
                href={wsp(MENSAJE_GENERAL)}
                target="_blank"
                rel="noopener"
                onClick={() => setAbierto(false)}
              >
                <IconoWsp />
                Escríbenos por WhatsApp
              </a>
            </div>
          </nav>
        )}
      </header>
      <div className="toldo" aria-hidden="true"></div>
    </>
  );
}
