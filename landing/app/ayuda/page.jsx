import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { IconoWsp } from '../../components/Iconos';
import { meta, migas, ld } from '../../lib/seo';
import { wsp } from '../../lib/enlaces';

export const metadata = meta({
  titulo: 'Centro de ayuda y soporte de PagoYa',
  descripcion:
    'Centro de ayuda de PagoYa: escríbenos por WhatsApp, correo de soporte, preguntas frecuentes y guías para que tu negocio escuche cada Yape y Plin que te cae.',
  ruta: '/ayuda/',
  tipo: 'website',
});

export default function Ayuda() {
  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{
          __html: ld(
            migas([
              { nombre: 'Inicio', ruta: '/' },
              { nombre: 'Ayuda', ruta: '/ayuda/' },
            ]),
          ),
        }}
      />

      <Cabecera />
      <main className="legal articulo">
        <p className="miga"><a href="/">← Volver a pagoya.pe</a></p>

        <h1>¿En qué te ayudamos, casero?</h1>
        <p className="vigencia">Soporte de PagoYa · Te respondemos rápido y en cristiano</p>

        <p>
          Acá encuentras todo para sacarle jugo a PagoYa: cómo escribirnos cuando algo
          no suena, dónde resolver tus dudas y las guías que te dejan la caja
          sonando. Nada de vueltas: si tienes un problema, lo vemos contigo.
        </p>

        <div className="caja-resumen">
          <p><strong>La forma más rápida de que te atendamos:</strong></p>
          <ul>
            <li>Escríbenos por <strong>WhatsApp</strong>: es nuestro canal principal y contestamos ahí mismo.</li>
            <li>Cuéntanos qué celular usas (marca y modelo) y qué pasa exactamente. Con eso te ayudamos al toque.</li>
            <li>¿Es una duda general? Míratela primero en las <a href="/preguntas-frecuentes/">preguntas frecuentes</a>: seguro ya está resuelta.</li>
          </ul>
        </div>

        <h2>Escríbenos por WhatsApp</h2>
        <p>
          En el Perú todo se arregla por WhatsApp, y PagoYa no es la excepción. Es la
          vía más rápida: nos mandas tu mensaje y te respondemos una persona de
          verdad, no un robot.
        </p>
        <p className="destacado">
          {/* TODO(whatsapp): reemplazar por el número real. El enlace ya usa el número
              central de lib/enlaces.js (NUMERO_WSP); este texto es solo para mostrarlo. */}
          WhatsApp de soporte: <strong>+51 9XX XXX XXX</strong>
        </p>
        <p>
          <a
            className="btn btn-wsp js-wsp"
            data-donde="ayuda-wsp"
            href={wsp('Hola PagoYa, necesito ayuda con la app: ')}
            target="_blank"
            rel="noopener"
          >
            <IconoWsp />
            Escríbenos por WhatsApp
          </a>
        </p>
        <p>
          Horario de atención: todos los días, de <strong>8:00 a. m. a 9:00 p. m.</strong>
          Si escribes fuera de ese horario, te respondemos apenas abrimos.
        </p>

        <h2>Correo de soporte</h2>
        <p>
          Si prefieres el correo (por ejemplo para mandarnos capturas o un asunto más
          largo), escríbenos a:
        </p>
        <p className="destacado">
          <a href="mailto:pimentel@inklop.com?subject=Ayuda%20PagoYa">pimentel@inklop.com</a>
        </p>
        <p>
          Cuéntanos tu problema con el mayor detalle posible: qué celular tienes, qué
          billetera usas (Yape, Plin u otra) y qué esperabas que pasara. Así lo
          resolvemos de una.
        </p>

        <h2>Resuelve tú mismo las dudas más comunes</h2>
        <p>
          Muchas cosas las puedes destrabar en un minuto sin escribirnos. Empieza por acá:
        </p>
        {/* h3, no h2: estas tarjetas cuelgan del h2 de arriba. Saltarse un nivel
            rompe el esquema que leen Google y los lectores de pantalla. */}
        <ul className="lista-guias">
          <li>
            <h3><a href="/preguntas-frecuentes/">Preguntas frecuentes</a></h3>
            <p>Qué es PagoYa, cómo funciona, si es seguro darle acceso a tus notificaciones, cuánto cuesta y cómo sumar a tu trabajador.</p>
            <a className="enlace-seguir" href="/preguntas-frecuentes/">Ver las preguntas →</a>
          </li>
          <li>
            <h3><a href="/no-me-llegan-notificaciones-yape/">No me suenan los pagos</a></h3>
            <p>Si PagoYa no está anunciando tus Yapes, casi siempre es un permiso o el ahorro de batería del celular. Te lo arreglamos paso a paso por marca.</p>
            <a className="enlace-seguir" href="/no-me-llegan-notificaciones-yape/">Cómo arreglarlo →</a>
          </li>
          <li>
            <h3><a href="/yape-falso/">Me quieren pasar un yape falso</a></h3>
            <p>Cómo confirmar un pago sin mirar la pantalla del cliente y por qué la regla es simple: si no suena, no te pagaron.</p>
            <a className="enlace-seguir" href="/yape-falso/">Leer la guía →</a>
          </li>
          <li>
            <h3><a href="/guias/">Todas las guías</a></h3>
            <p>Guías prácticas para cobrar seguro con Yape y Plin. Gratis y sin registrarte.</p>
            <a className="enlace-seguir" href="/guias/">Ver todas →</a>
          </li>
        </ul>

        <h2>¿Quieres darnos de baja tu cuenta?</h2>
        <p>
          Puedes pedir la eliminación de tu cuenta y de tus datos cuando quieras, sin
          dar explicaciones. Te contamos cómo en{' '}
          <a href="/eliminar-datos/">Eliminar mi cuenta y mis datos</a>.
        </p>

        <aside className="caja-cta">
          <h2>¿Tu duda no está resuelta?</h2>
          <p>Escríbenos por WhatsApp y lo vemos contigo. Si es una consulta que se repite, hasta la convertimos en guía.</p>
          <a
            className="btn btn-wsp js-wsp"
            data-donde="ayuda-cta"
            href={wsp('Hola PagoYa, tengo una consulta y necesito ayuda: ')}
            target="_blank"
            rel="noopener"
          >
            <IconoWsp />
            Escríbenos por WhatsApp
          </a>
          <small>Contestamos rápido, de 8 a. m. a 9 p. m.</small>
        </aside>
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
