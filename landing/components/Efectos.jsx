'use client';

import { useEffect } from 'react';

/**
 * Efectos globales de la página (sin UI propia):
 * - Animaciones de entrada .reveal con IntersectionObserver.
 * - Evento de conversión "WhatsApp" en cada clic a .js-wsp (Plausible).
 */
export default function Efectos() {
  useEffect(() => {
    const elementos = document.querySelectorAll('.reveal');
    let observador;

    if ('IntersectionObserver' in window) {
      observador = new IntersectionObserver(
        (entradas) => {
          entradas.forEach((e) => {
            if (e.isIntersecting) {
              e.target.classList.add('visible');
              observador.unobserve(e.target);
            }
          });
        },
        { threshold: 0.12, rootMargin: '0px 0px -40px 0px' }
      );
      elementos.forEach((el) => observador.observe(el));
    } else {
      elementos.forEach((el) => el.classList.add('visible'));
    }

    const alHacerClic = (e) => {
      const enlace = e.target.closest ? e.target.closest('.js-wsp') : null;
      if (enlace && window.plausible) {
        window.plausible('WhatsApp', { props: { donde: enlace.dataset.donde || 'otro' } });
      }
    };
    document.addEventListener('click', alHacerClic);

    return () => {
      document.removeEventListener('click', alHacerClic);
      if (observador) observador.disconnect();
    };
  }, []);

  return null;
}
