'use client';

import { useEffect, useRef, useState } from 'react';

/**
 * Pizarra de bodega. Los números que representan una cantidad real se
 * cuentan solos al entrar en pantalla (0 → 5 min, 0 → 2 seg, 0 → 24/7).
 * El "S/ 0" de comisión NO se anima: el cero se muestra fijo, no se cuenta.
 *
 * En el HTML del servidor van los valores finales (SEO y sin-JS intactos);
 * la animación solo corre al verse la sección y respeta reduced-motion.
 */
const CIFRAS = [
  { fijo: 'S/ 0', texto: 'de comisión por venta' },
  { fin: 5, sufijo: ' min', texto: 'para dejarlo instalado' },
  { fin: 2, sufijo: ' seg', texto: 'para avisarle a tu gente' },
  { fin: 24, sufijo: '/7', texto: 'tu caja atenta a cada pago' },
];

const DURACION_MS = 1200;

export default function Cifras() {
  const seccionRef = useRef(null);
  const [valores, setValores] = useState(CIFRAS.map((c) => c.fin ?? 0));
  const [terminado, setTerminado] = useState(false);
  const arrancoRef = useRef(false);

  useEffect(() => {
    const seccion = seccionRef.current;
    if (!seccion) return;

    const reducido =
      window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reducido || !('IntersectionObserver' in window)) return; // se quedan los valores finales

    const arrancar = () => {
      if (arrancoRef.current) return;
      arrancoRef.current = true;

      const t0 = performance.now();
      const paso = (t) => {
        const avance = Math.min((t - t0) / DURACION_MS, 1);
        const suavizado = 1 - Math.pow(1 - avance, 3); // easeOutCubic
        setValores(CIFRAS.map((c) => Math.round((c.fin ?? 0) * suavizado)));
        if (avance < 1) {
          requestAnimationFrame(paso);
        } else {
          setTerminado(true);
        }
      };

      setValores(CIFRAS.map(() => 0));
      requestAnimationFrame(paso);
    };

    const obs = new IntersectionObserver(
      (entradas) => {
        entradas.forEach((e) => {
          if (e.isIntersecting) {
            arrancar();
            obs.disconnect();
          }
        });
      },
      { threshold: 0.4 }
    );
    obs.observe(seccion);
    return () => obs.disconnect();
  }, []);

  return (
    <section className="cifras" aria-label="PagoYa en cifras" ref={seccionRef}>
      <div className="wrap">
        {CIFRAS.map((c, i) => (
          <div className="cifra" key={c.texto}>
            {c.fijo ? (
              <strong className={terminado ? 'tiza-pop' : undefined}>{c.fijo}</strong>
            ) : (
              <strong className={terminado ? 'tiza-pop' : undefined} suppressHydrationWarning>
                {valores[i]}
                {c.sufijo}
              </strong>
            )}
            <span>{c.texto}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
