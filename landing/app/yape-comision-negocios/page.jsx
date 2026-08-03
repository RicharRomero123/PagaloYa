import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { Migas, CajaCta, Relacionadas } from '../../components/Guia';
import { meta, migas, articulo, faq, ld } from '../../lib/seo';
import { GUIAS } from '../../lib/guias';

const GUIA = GUIAS.find((g) => g.ruta === '/yape-comision-negocios/');

export const metadata = meta({
  titulo: '¿Yape cobra comisión a los negocios?',
  descripcion: GUIA.descripcion,
  ruta: GUIA.ruta,
});

const PREGUNTAS = [
  {
    p: '¿Yape cobra comisión por recibir pagos?',
    r: 'Entre personas, no: yapear y recibir con una cuenta personal sigue siendo gratis. La comisión aparece cuando el negocio usa el perfil Yape Empresa: ahí Yape cobra un 2.95 % sobre lo que se cobra con ese perfil. Empezó a aplicarse a fines de abril de 2024. Confirma siempre las condiciones vigentes en el centro de ayuda de Yape, porque pueden cambiar.',
  },
  {
    p: '¿Desde qué monto Yape me obliga a pasar al perfil de negocio?',
    r: 'Cuando la cuenta recibe más de 5 UIT al mes. El equivalente en soles cambia cada año, porque la UIT se actualiza: por eso conviene revisar el monto vigente en la propia app o en el centro de ayuda de Yape en vez de guiarse por una cifra vista en una noticia antigua.',
  },
  {
    p: '¿Necesito RUC para usar Yape Empresa?',
    r: 'Sí. Yape Empresa es un perfil de negocio y está pensado para negocios formalizados: se afilia con RUC y una cuenta a nombre del negocio. Si todavía no estás formalizado, no es una opción disponible para ti.',
  },
  {
    p: '¿Cómo cobro con Yape si no tengo RUC?',
    r: 'Sigues cobrando con tu Yape personal, como siempre, mientras no superes el límite mensual que obliga al perfil de negocio. Lo que no vas a tener son las funciones del perfil de negocio, como los ayudantes que ven los pagos en otros celulares. Para eso hay alternativas que no tocan tu dinero ni te piden RUC.',
  },
  {
    p: '¿PagoYa me cobra comisión por venta?',
    r: 'No, y no es una promoción: es estructural. Tu dinero nunca pasa por PagoYa, así que no hay ninguna transacción de la que descontar un porcentaje. PagoYa solo lee la notificación de pago que tu billetera ya te manda al celular y la anuncia en voz alta. Tampoco te pide RUC, porque no emite comprobantes ni procesa cobros.',
  },
  {
    p: '¿PagoYa reemplaza a Yape?',
    r: 'No. PagoYa no es una billetera ni un banco y no mueve dinero. Sigues cobrando con tu Yape, tu Plin o lo que uses; PagoYa solo se encarga de que te enteres del pago al instante, en tu local y en los celulares de tu gente.',
  },
];

export default function YapeComisionNegocios() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(articulo({ titulo: metadata.title, descripcion: GUIA.descripcion, ruta: GUIA.ruta, publicado: GUIA.publicado })) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(faq(PREGUNTAS)) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(migas([{ nombre: 'Inicio', ruta: '/' }, { nombre: 'Guías', ruta: '/guias/' }, { nombre: GUIA.corto, ruta: GUIA.ruta }])) }} />

      <Cabecera />
      <main className="legal articulo">
        <Migas titulo="¿Yape cobra comisión?" />

        <h1>¿Yape cobra comisión a los negocios?</h1>
        <p className="vigencia">Actualizado el 3 de agosto de 2026 · Lectura de 5 minutos</p>

        <div className="caja-resumen">
          <p><strong>La respuesta corta:</strong></p>
          <ul>
            <li><strong>Entre personas, no.</strong> Yapear y recibir con tu cuenta personal sigue siendo gratis.</li>
            <li><strong>Con perfil de negocio, sí.</strong> Yape Empresa cobra <strong>2.95 %</strong> sobre lo que cobras con ese perfil, desde fines de abril de 2024.</li>
            <li><strong>Te obliga a pasar al perfil de negocio</strong> cuando recibes más de <strong>5 UIT al mes</strong>.</li>
            <li><strong>Yape Empresa pide RUC.</strong> Si todavía no estás formalizado, no es una opción para ti.</li>
          </ul>
        </div>

        <p className="destacado">
          Las condiciones y los precios de terceros cambian. Antes de decidir,
          confírmalo en el{' '}
          <a href="https://www.yape.com.pe/preguntas-frecuentes/yape-empresa/yape-empresa-tiene-algun-costo" target="_blank" rel="noopener nofollow">
            centro de ayuda oficial de Yape
          </a>.
        </p>

        <h2>Yape personal: gratis, pero con un techo</h2>
        <p>
          Si cobras con tu Yape de siempre —tu número, tu QR personal— no pagas
          comisión. Eso es lo que hoy hacen cientos de miles de bodegas, puestos de
          mercado, taxistas y negocios chicos del Perú, y sigue siendo gratis.
        </p>
        <p>
          El detalle está en el techo: cuando una cuenta empieza a recibir{' '}
          <strong>más de 5 UIT al mes</strong>, Yape te lleva al perfil de negocio.
          Ojo con este número: se expresa en UIT y la UIT se actualiza cada año, así
          que el monto en soles no es el mismo que leíste en una noticia de hace dos
          años. Revísalo en la app.
        </p>

        <h2>Yape Empresa: qué te da y qué te cuesta</h2>
        <p>
          Yape Empresa es el perfil de negocio. Da funciones que la cuenta personal
          no tiene —reportes de ventas, montos más altos, y ayudantes que pueden ver
          los cobros desde otros celulares— y a cambio cobra un{' '}
          <strong>2.95 % sobre lo que cobras con ese perfil</strong>. La comisión
          entró en vigencia a fines de abril de 2024, después de un periodo de prueba.
        </p>
        <p>Traducido a la caja de un negocio chico:</p>
        <ul>
          <li>De cada <strong>S/ 100</strong> cobrados, se van <strong>S/ 2.95</strong>.</li>
          <li>Con <strong>S/ 3,000</strong> al mes en ventas por Yape, son unos <strong>S/ 88</strong> mensuales.</li>
          <li>Con <strong>S/ 10,000</strong>, unos <strong>S/ 295</strong> al mes.</li>
        </ul>
        <p>
          Para un negocio con márgenes anchos puede tener todo el sentido. Para una
          bodega, donde el margen por producto es de céntimos, ese porcentaje pesa.
        </p>

        <h2>¿Y si no tengo RUC?</h2>
        <p>
          Entonces Yape Empresa no está disponible para ti, y no es un trámite que
          se resuelva en una tarde: formalizarse trae obligaciones tributarias y
          contables que hay que poder sostener.
        </p>
        <p>
          Mientras tanto sigues cobrando con tu Yape personal sin comisión. Lo que
          no tienes son las funciones del perfil de negocio —sobre todo la de que{' '}
          <strong>otra persona vea los pagos desde su propio celular</strong>, que es
          justo la que necesitan las bodegas con trabajadores, los taxistas que usan
          el Yape de su esposa y los negocios con motorizados.
        </p>

        <h2>Cómo decidir, en corto</h2>
        <ul>
          <li>
            <strong>Cobras poco y no tienes RUC:</strong> Yape personal, gratis. No
            hay nada que decidir todavía.
          </li>
          <li>
            <strong>Ya facturas con RUC y todo tu cobro es por Yape:</strong> Yape
            Empresa es coherente. Saca la cuenta del 2.95 % contra tu margen real.
          </li>
          <li>
            <strong>Cobras por Yape, Plin y efectivo:</strong> ningún perfil de una
            sola billetera te va a dar la foto completa del día.
          </li>
          <li>
            <strong>Lo que necesitas es que tu gente se entere del pago:</strong> eso
            se resuelve sin cambiar de perfil, sin comisión y sin RUC.
          </li>
        </ul>

        <h2>Dónde entra PagoYa</h2>
        <p>
          Vale la pena decirlo derecho: <strong>PagoYa no compite con Yape.</strong>{' '}
          Yape mueve el dinero; PagoYa solo se encarga de que te enteres. Son dos
          capas distintas.
        </p>
        <p>
          Todos —Yape, Plin, los procesadores de tarjeta— pelean por ser el camino
          por donde pasa tu plata, y por eso cobran. PagoYa vive un piso más arriba,
          en la pregunta que nadie está resolviendo: <em>¿me pagaron de verdad, ya
          entró, y quién más lo sabe?</em>
        </p>
        <p>
          De esa decisión salen las tres cosas que nos preguntan siempre, y no son
          oferta de lanzamiento: son consecuencia de no tocar el dinero.
        </p>
        <ul>
          <li><strong>Sin comisión por venta.</strong> Tu plata nunca pasa por PagoYa, así que no hay porcentaje que descontar.</li>
          <li><strong>Sin RUC.</strong> No emitimos comprobantes ni procesamos cobros: no hay nada que formalizar para usarnos.</li>
          <li><strong>Con la cuenta que ya tienes.</strong> Mismo Yape, mismo QR, mismo número. No te mudas de nada.</li>
        </ul>
        <p className="destacado">Cobra como quieras. Entérate siempre.</p>
        <p>
          Lo que hace PagoYa es leer la notificación de pago que tu billetera ya te
          manda al celular y <a href="/">anunciarla en voz alta</a> al instante, en tu
          local y en los celulares de tu gente. Empiezas gratis.
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

        <p className="nota-tabla">
          Datos referenciales a agosto de 2026, tomados de comunicaciones públicas de
          Yape y prensa económica peruana. Las condiciones pueden cambiar: verifícalas
          en los canales oficiales de Yape antes de tomar una decisión. PagoYa es un
          producto independiente y no pertenece a Yape, al BCP ni a ningún banco; las
          marcas mencionadas pertenecen a sus respectivos titulares.
        </p>

        <CajaCta
          donde="guia-comision"
          titulo="Sin comisión, sin RUC y con tu Yape de siempre"
          texto="Te ayudamos a dejarlo andando en 5 minutos con el celular que ya tienes. Empiezas gratis."
          mensaje="Hola PagoYa, quiero enterarme de mis pagos sin cambiar mi Yape ni pagar comisión"
        />

        <Relacionadas actual={GUIA.ruta} />
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
