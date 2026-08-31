"use client";

import { useEffect } from "react";

/**
 * Registra el service worker (PWA). Solo en producción: en `next dev` cachear
 * el shell estorba el hot-reload. No pinta nada.
 */
export function RegistrarSW() {
  useEffect(() => {
    if (process.env.NODE_ENV !== "production") return;
    if (typeof navigator === "undefined" || !("serviceWorker" in navigator)) return;
    const registrar = () => {
      navigator.serviceWorker.register("/sw.js").catch(() => {});
    };
    if (document.readyState === "complete") registrar();
    else window.addEventListener("load", registrar, { once: true });
  }, []);
  return null;
}
