import { IconoWsp } from './Iconos';
import { wsp, MENSAJE_GENERAL } from '../lib/enlaces';

/** Botón de WhatsApp flotante, siempre visible. */
export default function WspFlotante() {
  return (
    <a
      className="wsp-flotante js-wsp"
      data-donde="flotante"
      href={wsp(MENSAJE_GENERAL)}
      target="_blank"
      rel="noopener"
      aria-label="Escríbenos por WhatsApp"
    >
      <IconoWsp ancho={31} />
    </a>
  );
}
