import { IconoLogo } from './Iconos';
import { wsp, MENSAJE_GENERAL } from '../lib/enlaces';

/** Cabecera con toldo de mercado. `conNav` muestra las anclas (solo en la home). */
export default function Cabecera({ conNav = false }) {
  return (
    <>
      <header className="cabecera">
        <div className="wrap">
          <a className="logo" href="/" aria-label="PagoYa, inicio">
            <IconoLogo />
            Pago<b>Ya</b>
          </a>
          {conNav && (
            <nav className="nav-anclas" aria-label="Secciones">
              <a href="#que-es">Qué es</a>
              <a href="#como-funciona">Cómo funciona</a>
              <a href="#comparacion">Comparar</a>
              <a href="#planes">Planes</a>
              <a href="#preguntas">Preguntas</a>
            </nav>
          )}
          <a
            className="btn btn-wsp btn-chico js-wsp"
            data-donde="cabecera"
            href={wsp(MENSAJE_GENERAL)}
            target="_blank"
            rel="noopener"
          >
            Escríbenos
          </a>
        </div>
      </header>
      <div className="toldo" aria-hidden="true"></div>
    </>
  );
}
