import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { Migas, CajaCta, Relacionadas } from '../../components/Guia';
import { meta, migas, articulo, faq, ld } from '../../lib/seo';
import { GUIAS } from '../../lib/guias';

const GUIA = GUIAS.find((g) => g.ruta === '/parlante-para-yape/');

export const metadata = meta({
  titulo: 'Parlante para Yape: opciones y precios en Perú',
  descripcion: GUIA.descripcion,
  ruta: GUIA.ruta,
});

const PREGUNTAS = [
  {
    p: '¿Cuánto cuesta un parlante para Yape en Perú?',
    r: 'Los QR parlantes que venden los procesadores de pago cuestan alrededor de S/ 129 como pago único, pero obligan a cobrar a través de esa empresa. Una app que usa el celular que ya tienes cuesta S/ 0 de equipo. Los altavoces genéricos importados van de US$ 15 a US$ 35, pero suelen estar amarrados a la nube de su fabricante y no funcionan con Yape.',
  },
  {
    p: '¿Puedo comprar un parlante que anuncie mis Yapes sin cambiar de procesador?',
    r: 'Hoy en Perú no se vende un parlante independiente que escuche directamente los pagos de Yape: los QR parlantes que existen están atados a la plataforma del procesador que los vende. La alternativa que no te amarra es usar el celular donde ya recibes tus Yapes con una app que los anuncie en voz alta.',
  },
  {
    p: '¿El QR parlante de un procesador sirve para mi Yape personal?',
    r: 'No. Ese equipo anuncia los pagos hechos al QR de ese procesador, no los que te caen a tu Yape. Si tu cliente te yapea a tu número o a tu QR de Yape, el aparato no lo canta porque ese pago nunca pasó por su plataforma.',
  },
  {
    p: '¿Mi celular suena lo suficientemente fuerte para el mostrador?',
    r: 'En una bodega o un puesto, sí: el anuncio se escucha claro al volumen normal del celular. Si tu local es muy ruidoso, puedes conectar el celular a cualquier parlante Bluetooth o con cable que ya tengas y el anuncio sale por ahí, más fuerte.',
  },
  {
    p: '¿Qué pasa si el dueño no está en la tienda?',
    r: 'Un parlante de mostrador solo suena en el local. Una app puede además reenviar el anuncio a los celulares del dueño y de sus trabajadores, así que el pago se escucha en la tienda y donde cada uno esté.',
  },
];

export default function ParlanteParaYape() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(articulo({ titulo: metadata.title, descripcion: GUIA.descripcion, ruta: GUIA.ruta, publicado: GUIA.publicado })) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(faq(PREGUNTAS)) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(migas([{ nombre: 'Inicio', ruta: '/' }, { nombre: 'Guías', ruta: '/guias/' }, { nombre: GUIA.corto, ruta: GUIA.ruta }])) }} />

      <Cabecera />
      <main className="legal articulo">
        <Migas titulo="Parlante para Yape" />

        <h1>Parlante para Yape: opciones y precios en Perú</h1>
        <p className="vigencia">Actualizado el 31 de julio de 2026 · Lectura de 5 minutos</p>

        <div className="caja-resumen">
          <p><strong>La respuesta corta:</strong></p>
          <ul>
            <li>Los <strong>QR parlantes</strong> cuestan alrededor de S/ 129 y te obligan a cobrar con esa empresa, no con tu Yape.</li>
            <li>Los <strong>altavoces importados</strong> vienen amarrados a la nube de su fabricante: no escuchan Yape.</li>
            <li>Tu <strong>propio celular</strong> puede anunciar los pagos en voz alta con una app, sin comprar nada.</li>
            <li>Y solo la app te avisa <strong>también cuando no estás en la tienda</strong>.</li>
          </ul>
        </div>

        <h2>Por qué todo el mundo quiere un parlante</h2>
        <p>
          Porque escuchar el pago resuelve dos problemas a la vez. Primero, corta el
          <strong> yape falso</strong>: si la caja canta, el pago entró de verdad, y ya no
          hay que revisar la pantalla que te muestra el cliente. Segundo, te devuelve el
          ritmo: sigues atendiendo, cobrando y despachando sin agarrar el celular por
          cada venta.
        </p>
        <p>
          El asunto es que «parlante para Yape» significa cosas muy distintas según a
          quién le preguntes, y las diferencias te cuestan plata. Vamos una por una.
        </p>

        <h2>Opción 1: el QR parlante de un procesador de pagos</h2>
        <p>
          Es el aparato que ya se ve en varias tiendas: un dispositivo de mostrador con
          un QR impreso que anuncia en voz alta cada cobro. En Perú el caso conocido es
          el QR parlante de Izipay, con un precio de referencia alrededor de{' '}
          <strong>S/ 129</strong> como pago único.
        </p>
        <p><strong>A favor:</strong></p>
        <ul>
          <li>Es un equipo dedicado: siempre encendido en el mostrador y suena fuerte.</li>
          <li>No depende de que el celular de nadie tenga batería.</li>
          <li>Si ya cobras con ese procesador, es una buena compra.</li>
        </ul>
        <p><strong>En contra —y esto es lo importante:</strong></p>
        <ul>
          <li>
            <strong>Solo canta los pagos hechos a SU QR.</strong> Si tu casero te yapea a
            tu número de Yape de siempre, el aparato no dice nada: ese pago nunca pasó
            por su plataforma.
          </li>
          <li>
            <strong>Te amarra a ese procesador.</strong> Cambias tu forma de cobrar, y con
            eso entran las comisiones y los plazos de abono de esa empresa.
          </li>
          <li><strong>Pagas el equipo</strong> por adelantado.</li>
          <li>
            <strong>Suena solo en la tienda.</strong> Si el dueño está en su casa, no se
            entera de nada.
          </li>
        </ul>

        <h2>Opción 2: comprar un altavoz de pagos importado</h2>
        <p>
          Buscando aparecen los llamados <em>payment soundbox</em> o <em>cloud speaker</em>:
          los altavoces genéricos que se fabrican en Shenzhen y que usan las plataformas
          de pago de India y China. Se consiguen desde <strong>US$ 15 a US$ 35</strong> por
          unidad en las plataformas de importación.
        </p>
        <p>
          Suena tentador y es una mala idea para un comercio. El equipo no es autónomo:
          <strong> viene configurado para hablar con la nube de su fabricante</strong>, no
          con Yape ni con ninguna billetera peruana. Sin esa integración es un parlante
          bonito que nunca va a decir nada. Súmale que es un equipo que emite WiFi o 4G,
          así que importarlo formalmente implica homologación ante el MTC.
        </p>
        <p className="destacado">
          Hoy en Perú no existe un parlante independiente que escuche tu Yape directamente.
          Si lo encuentras «suelto», casi seguro no va a funcionar con tus cobros.
        </p>

        <h2>Opción 3: usar el celular que ya tienes</h2>
        <p>
          Aquí está el atajo que casi nadie considera: <strong>el celular donde recibes
          los Yapes ya recibe la notificación de cada pago</strong>. Lo único que falta es
          que alguien la diga en voz alta.
        </p>
        <p>
          Eso hace <a href="/">PagoYa</a>. Es una app para ese mismo celular Android:
          cuando cae la notificación real de tu billetera, la canta al instante —
          <em>«¡PagoYaaa! Te yapearon 25 soles»</em>— con la pantalla apagada y en plena
          hora punta.
        </p>
        <ul>
          <li><strong>S/ 0 de equipo.</strong> No compras nada; usas el celular de la caja.</li>
          <li><strong>Con tu Yape de siempre.</strong> No cambias de procesador ni de QR, y no hay comisión por venta.</li>
          <li><strong>Funciona con Yape y con Plin.</strong> Y si te mandan un Plin a tu número de Yape, también suena.</li>
          <li><strong>Suena donde estés.</strong> El mismo pago se anuncia en el celular del dueño y en los de sus trabajadores.</li>
          <li><strong>Te lleva la cuenta del día.</strong> Cada pago anunciado queda anotado para cuando cuadres en la noche.</li>
        </ul>
        <p>
          ¿Y si tu local es ruidoso? Conectas el celular a cualquier parlante Bluetooth o
          con cable que ya tengas, y el anuncio sale por ahí. Tienes tu parlante de
          mostrador sin comprar un aparato nuevo.
        </p>

        <h2>Comparación rápida</h2>
        <div className="tabla-scroll">
          <table>
            <thead>
              <tr>
                <th>&nbsp;</th>
                <th>QR parlante</th>
                <th>Altavoz importado</th>
                <th>PagoYa en tu celular</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <th scope="row">Costo del equipo</th>
                <td>~S/ 129</td>
                <td>US$ 15–35 + importación</td>
                <td>S/ 0</td>
              </tr>
              <tr>
                <th scope="row">¿Anuncia tu Yape de siempre?</th>
                <td>No</td>
                <td>No</td>
                <td>Sí</td>
              </tr>
              <tr>
                <th scope="row">¿Cambias de procesador?</th>
                <td>Sí</td>
                <td>—</td>
                <td>No</td>
              </tr>
              <tr>
                <th scope="row">¿Avisa al dueño fuera de la tienda?</th>
                <td>No</td>
                <td>No</td>
                <td>Sí</td>
              </tr>
              <tr>
                <th scope="row">¿Cuenta del día?</th>
                <td>Según la plataforma</td>
                <td>No</td>
                <td>Sí</td>
              </tr>
              <tr>
                <th scope="row">Listo para usar</th>
                <td>Tras afiliarte</td>
                <td>Requiere integración propia</td>
                <td>5 minutos</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p className="nota-tabla">
          Datos referenciales a julio de 2026. Las marcas mencionadas pertenecen a sus
          respectivos titulares y se citan solo para comparación.
        </p>

        <h2>Entonces, ¿cuál te conviene?</h2>
        <ul>
          <li><strong>Si ya cobras con un procesador y estás conforme:</strong> su QR parlante es coherente con lo que ya tienes.</li>
          <li><strong>Si cobras con tu Yape y no quieres cambiar nada:</strong> la app en tu propio celular, sin costo de equipo.</li>
          <li><strong>Si el dueño no está en el local:</strong> la app es la única de las tres que resuelve eso.</li>
          <li><strong>Si te ofrecen un altavoz importado «que anuncia Yape»:</strong> pide una demostración con un pago real antes de pagar nada.</li>
        </ul>
        <p>
          Y sobre el parlante de mostrador propio: está en nuestros planes para el
          plan Patrón, y la idea es prestártelo con la membresía activa en vez de
          vendértelo. Mientras tanto, tu celular ya hace el trabajo.
        </p>

        <h2>Preguntas frecuentes</h2>
        <div className="faq">
          {PREGUNTAS.map((q) => (
            <details key={q.p}>
              <summary>{q.p}</summary>
              <div><p>{q.r}</p></div>
            </details>
          ))}
        </div>

        <CajaCta
          donde="guia-parlante"
          titulo="Prueba con tu celular antes de comprar nada"
          texto="Te ayudamos a dejarlo andando en 5 minutos, con el Yape y el celular que ya tienes. Si no te sirve, no compraste nada."
          mensaje="Hola PagoYa, quiero que mis pagos Yape suenen en voz alta en mi negocio"
        />

        <Relacionadas actual={GUIA.ruta} />
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
