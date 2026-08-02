import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { CajaCta } from '../../components/Guia';
import { meta, migas, faq, ld } from '../../lib/seo';

export const metadata = meta({
  titulo: 'Preguntas frecuentes de PagoYa',
  descripcion:
    'Respuestas a las dudas más comunes sobre PagoYa: qué es, cómo funciona, si necesitas internet, si es seguro darle acceso a tus notificaciones, compatibilidad con Yape y Plin, precios y cómo sumar a tu trabajador.',
  ruta: '/preguntas-frecuentes/',
  tipo: 'website',
});

// Fuente única: el mismo arreglo alimenta el JSON-LD (para Google) y la lista
// visible de abajo. Nunca se escriben las preguntas dos veces.
const PREGUNTAS = [
  {
    p: '¿Qué es PagoYa?',
    r: 'PagoYa es una app para tu celular Android que anuncia en voz alta cada pago de Yape o Plin que te cae en el negocio. Cuando entra un pago, tu caja lo canta: «¡PagoYaaa! Te yapearon 25 soles». Sirve para confirmar cobros sin mirar la pantalla del cliente y para estar tranquilo contra el yape falso: si no suena, no te pagaron.',
  },
  {
    p: '¿Cómo funciona?',
    r: 'Instalas PagoYa en el celular donde recibes tus Yapes, le das permiso para leer las notificaciones de tus billeteras y listo. Cada vez que Yape o Plin te muestra una notificación de pago, PagoYa la escucha y la anuncia por voz. El anuncio nace de la notificación real del sistema, no de una captura ni de la palabra de nadie, por eso es confiable.',
  },
  {
    p: '¿Necesito internet para que suene?',
    r: 'Para que suene en el mismo celular donde caen los pagos, no necesitas internet: PagoYa lee la notificación de tu billetera y la anuncia ahí mismo. Solo necesitas internet para que el pago también suene en otros celulares (el del dueño en su casa o el de tus trabajadores) y para que quede guardado en tu historial y tu cierre de caja.',
  },
  {
    p: '¿Es seguro darle acceso a mis notificaciones?',
    r: 'Sí. PagoYa solo lee las notificaciones de las apps de billeteras (Yape, Plin y bancos compatibles), nunca tus chats, tus fotos ni tus correos: esas notificaciones se ignoran y no se guardan. Tampoco entra a tu cuenta bancaria, no ve tu saldo y no puede mover tu dinero: solo escucha los pagos que te avisan por notificación. Y recuerda la regla de oro: si no suena, no te pagaron.',
  },
  {
    p: '¿Funciona con Yape y Plin?',
    r: 'Sí, PagoYa es compatible con Yape y Plin, y con varias apps de bancos peruanos. Ojo: PagoYa es un producto independiente, NO pertenece a Yape ni al BCP ni a Plin, ni está afiliado a ellos. Solo lee la notificación de pago que esas apps ya te muestran en el celular para anunciártela por voz.',
  },
  {
    p: '¿Qué pasa si mi Yape se apaga o se cierra?',
    r: 'PagoYa vigila que tu celular siga escuchando los pagos. Si el sistema cierra el servicio para ahorrar batería, la app lo vuelve a levantar sola para que no te pierdas ningún cobro. Igual, conviene dejar bien puestos los permisos y quitarle a PagoYa el ahorro de batería: te explicamos cómo en la guía de notificaciones que no llegan.',
  },
  {
    p: '¿Cuánto cuesta?',
    r: 'Empiezas gratis. El plan Gratis anuncia por voz cada pago en 1 celular, con protección anti yape falso. El plan Caserito cuesta S/ 12.90 al mes y suma varios celulares conectados, el dueño escuchando desde su casa y la cuenta del día lista para cuadrar. El plan Patrón (próximamente) cuesta S/ 24.90 al mes e incluye un parlante PagoYa de mostrador prestado, sin pagar el equipo.',
  },
  {
    p: '¿Cómo sumo a mi trabajador?',
    r: 'Desde la app generas un código de 6 dígitos y se lo pasas a tu trabajador. Él instala PagoYa en su celular, mete el código y queda conectado a tu negocio: desde ese momento escucha los mismos pagos que tú, esté donde esté. Así todos confirman los cobros al toque, sin llamarte por cada venta. Sumar trabajadores es parte de los planes Caserito y Patrón.',
  },
];

export default function PreguntasFrecuentes() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(faq(PREGUNTAS)) }} />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{
          __html: ld(
            migas([
              { nombre: 'Inicio', ruta: '/' },
              { nombre: 'Preguntas frecuentes', ruta: '/preguntas-frecuentes/' },
            ]),
          ),
        }}
      />

      <Cabecera />
      <main className="legal articulo">
        <p className="miga"><a href="/">← Volver a pagoya.pe</a></p>

        <h1>Preguntas frecuentes</h1>
        <p className="vigencia">Lo que todo casero pregunta, respondido derecho</p>

        <p>
          Reunimos acá las dudas que más nos llegan sobre PagoYa. Si la tuya no está,
          escríbenos por WhatsApp desde el <a href="/ayuda/">centro de ayuda</a> y te
          respondemos.
        </p>

        <div className="faq">
          {PREGUNTAS.map((q) => (
            <details key={q.p}>
              <summary>{q.p}</summary>
              <div><p>{q.r}</p></div>
            </details>
          ))}
        </div>

        <CajaCta
          donde="faq"
          titulo="¿Te quedó otra duda?"
          texto="Escríbenos por WhatsApp y lo vemos contigo. Empezar con PagoYa es gratis, sin compromiso y sin letra chiquita."
          mensaje="Hola PagoYa, tengo una pregunta sobre la app: "
        />
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
