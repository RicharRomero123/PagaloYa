'use client';

import { useEffect, useState } from 'react';

/**
 * Demo de voz: el sonido ES el producto. Usa el TTS del navegador
 * (0 KB de audio). Si el navegador no lo soporta, no se muestra.
 */
export default function DemoVoz() {
  const [soportado, setSoportado] = useState(false);
  const [notaVisible, setNotaVisible] = useState(false);

  useEffect(() => {
    if ('speechSynthesis' in window) setSoportado(true);
  }, []);

  const hablar = () => {
    const u = new SpeechSynthesisUtterance('¡Pago ya! Te yapearon 25 soles.');
    u.lang = 'es-PE';
    u.rate = 1.02;
    u.pitch = 1.1;
    const voces = window.speechSynthesis.getVoices();
    const voz =
      voces.find((v) => v.lang && v.lang.indexOf('es-PE') === 0) ||
      voces.find((v) => v.lang && v.lang.indexOf('es') === 0);
    if (voz) u.voice = voz;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(u);
    setNotaVisible(true);
    if (window.plausible) window.plausible('DemoVoz');
  };

  if (!soportado) return null;

  return (
    <div className="mockup-pie">
      <span className="nota-mano">¡Dale, escúchalo, casero!</span>
      <button className="btn btn-naranja" type="button" onClick={hablar}>
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <path d="M4 10v4h4l6 5V5l-6 5H4z" fill="currentColor" stroke="none" />
          <path d="M17.5 9.5a4 4 0 0 1 0 5M20 7a8 8 0 0 1 0 10" />
        </svg>
        Escúchalo
      </button>
      {notaVisible && (
        <small>Esta es la voz de tu navegador. La voz real de PagoYa suena más bacán.</small>
      )}
    </div>
  );
}
