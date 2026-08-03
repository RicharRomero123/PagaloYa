import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { CajaCta } from '../../components/Guia';
import { meta, migas, ld } from '../../lib/seo';
import { GUIAS } from '../../lib/guias';

export const metadata = meta({
  titulo: 'Guías para cobrar seguro con Yape y Plin',
  descripcion:
    'Guías prácticas para negocios peruanos: cómo detectar un Yape falso, qué parlante conviene para anunciar tus pagos y cómo arreglar las notificaciones de Yape que no llegan.',
  ruta: '/guias/',
  tipo: 'website',
});

export default function Guias() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(migas([{ nombre: 'Inicio', ruta: '/' }, { nombre: 'Guías', ruta: '/guias/' }])) }} />

      <Cabecera />
      <main className="legal articulo">
        <p className="miga"><a href="/">← Volver a pagoya.pe</a></p>

        <h1>Guías para cobrar seguro con Yape y Plin</h1>
        <p className="vigencia">Lo que preguntan los caseros, respondido derecho</p>

        <p>
          Escribimos estas guías con lo que nos preguntan todos los días bodegueros,
          puesteros y dueños de negocio del Perú. Son gratis, no hay que registrarse y
          sirven aunque nunca instales PagoYa.
        </p>

        <ul className="lista-guias">
          {GUIAS.map((g) => (
            <li key={g.ruta}>
              <h2><a href={g.ruta}>{g.titulo}</a></h2>
              <p>{g.descripcion}</p>
              <a className="enlace-seguir" href={g.ruta}>Leer la guía →</a>
            </li>
          ))}
        </ul>

        <CajaCta
          donde="guias-hub"
          titulo="¿Tu duda no está aquí?"
          texto="Escríbenos por WhatsApp y te respondemos. Si la pregunta se repite, la convertimos en guía."
          mensaje="Hola PagoYa, tengo una consulta sobre cobros con Yape: "
        />
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
