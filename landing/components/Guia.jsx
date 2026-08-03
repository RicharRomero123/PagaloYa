import { IconoWsp } from './Iconos';
import { wsp, MENSAJE_GENERAL } from '../lib/enlaces';
import { otrasGuias } from '../lib/guias';

/** Migas de pan visibles (las de JSON-LD van aparte, en cada página). */
export function Migas({ titulo }) {
  return (
    <nav className="migas" aria-label="Migas de pan">
      <a href="/">Inicio</a>
      <span aria-hidden="true">›</span>
      <a href="/guias/">Guías</a>
      <span aria-hidden="true">›</span>
      <span aria-current="page">{titulo}</span>
    </nav>
  );
}

/** Caja de conversión al final de cada guía. `donde` alimenta el analytics. */
export function CajaCta({ donde, titulo, texto, mensaje = MENSAJE_GENERAL }) {
  return (
    <aside className="caja-cta">
      <h2>{titulo}</h2>
      <p>{texto}</p>
      <a className="btn btn-wsp js-wsp" data-donde={donde} href={wsp(mensaje)} target="_blank" rel="noopener">
        <IconoWsp />
        Escríbenos por WhatsApp
      </a>
      <small>Gratis para empezar. Sin compromiso y sin letra chiquita.</small>
    </aside>
  );
}

/** Enlaces internos al resto de guías: reparte autoridad y retiene al lector. */
export function Relacionadas({ actual }) {
  const guias = otrasGuias(actual);
  if (!guias.length) return null;

  return (
    <section className="relacionadas">
      <h2>Sigue leyendo</h2>
      <ul>
        {guias.map((g) => (
          <li key={g.ruta}>
            <a href={g.ruta}>{g.titulo}</a>
            <span>{g.gancho}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
