/** Iconos SVG inline reutilizados en toda la página. */

/** Ícono oficial de PagoYa: el parlantito sonriente (generado desde icono-pagoya.png). */
export function IconoLogo({ ancho = 36 }) {
  return (
    <img
      src="/assets/icono-96.png"
      alt=""
      aria-hidden="true"
      width={ancho}
      height={ancho}
      className="logo-icono"
    />
  );
}

export function IconoWsp({ ancho = 22 }) {
  return (
    <svg width={ancho} height={ancho} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M12 2a10 10 0 0 0-8.6 15.1L2 22l5-1.3A10 10 0 1 0 12 2Zm5.4 14.1c-.2.7-1.3 1.3-1.9 1.4-.5.1-1.1.2-3.6-.8-3-1.2-4.9-4.3-5.1-4.5-.1-.2-1.2-1.6-1.2-3.1s.8-2.2 1-2.5c.3-.3.6-.4.8-.4h.6c.2 0 .4-.1.7.5l1 2.3c0 .2.1.4 0 .6l-.4.6-.5.5c-.2.2-.3.4-.1.7.1.3.8 1.3 1.7 2.1 1.2 1 2.1 1.4 2.4 1.5.3.1.5.1.7-.1l1-1.2c.2-.3.4-.2.7-.1l2.2 1c.3.2.5.3.6.4 0 .2 0 .7-.2 1.1Z" />
    </svg>
  );
}
