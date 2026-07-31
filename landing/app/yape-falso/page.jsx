import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { Migas, CajaCta, Relacionadas } from '../../components/Guia';
import { meta, migas, articulo, faq, ld } from '../../lib/seo';
import { GUIAS } from '../../lib/guias';

const GUIA = GUIAS.find((g) => g.ruta === '/yape-falso/');

export const metadata = meta({
  titulo: 'Yape falso: cómo saber si de verdad te pagaron',
  descripcion: GUIA.descripcion,
  ruta: GUIA.ruta,
});

const PREGUNTAS = [
  {
    p: '¿Cómo sé si un Yape es falso?',
    r: 'Nunca te fíes de la pantalla del cliente. Un Yape es real solo si el pago aparece en TU celular: en tu notificación de Yape o en el historial de tu propia app. Las capturas y las apps que imitan la pantalla de Yape se editan en segundos, pero nadie puede hacer sonar una notificación en tu equipo.',
  },
  {
    p: '¿Existen apps para hacer Yapes falsos?',
    r: 'Sí. Circulan aplicaciones que imitan la pantalla de confirmación de Yape con el monto, el nombre y la hora que el estafador escriba, e incluso reproducen un sonido parecido. Se ven idénticas a la real en la pantalla del cliente, por eso mirar su celular no sirve como verificación.',
  },
  {
    p: '¿Qué hago si ya me estafaron con un Yape falso?',
    r: 'Reúne la evidencia (hora, monto, nombre mostrado, grabación de la cámara si tienes) y denuncia el hecho ante la Policía Nacional; puedes hacerlo en una comisaría o por la vía de denuncia digital. Reporta también el caso al canal de atención de tu billetera. Recuperar el dinero es difícil, por eso lo que más rinde es evitar que vuelva a pasar.',
  },
  {
    p: '¿Sirve pedirle al cliente que me muestre la pantalla?',
    r: 'No es una verificación segura. La pantalla del cliente es justamente lo que el estafador controla. La verificación real es al revés: el pago tiene que aparecer en tu equipo, no en el suyo.',
  },
  {
    p: '¿Cómo evito el yape falso en hora punta?',
    r: 'Necesitas confirmar sin mirar la pantalla, porque en hora punta nadie revisa el celular por cada venta. La forma práctica es que el pago se anuncie en voz alta apenas cae: si suena, entró; si no suena, no te pagaron. Eso es lo que hace PagoYa con la notificación real de tu billetera.',
  },
];

export default function YapeFalso() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(articulo({ titulo: metadata.title, descripcion: GUIA.descripcion, ruta: GUIA.ruta, publicado: GUIA.publicado })) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(faq(PREGUNTAS)) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(migas([{ nombre: 'Inicio', ruta: '/' }, { nombre: 'Guías', ruta: '/guias/' }, { nombre: GUIA.corto, ruta: GUIA.ruta }])) }} />

      <Cabecera />
      <main className="legal articulo">
        <Migas titulo="Yape falso" />

        <h1>Yape falso: cómo saber si de verdad te pagaron</h1>
        <p className="vigencia">Actualizado el 31 de julio de 2026 · Lectura de 6 minutos</p>

        <div className="caja-resumen">
          <p><strong>La respuesta corta:</strong></p>
          <ul>
            <li>La pantalla del cliente <strong>no prueba nada</strong>: es exactamente lo que el estafador controla.</li>
            <li>Un pago es real solo si aparece en <strong>tu</strong> celular: tu notificación o tu historial.</li>
            <li>En hora punta nadie revisa el celular por cada venta, y ahí es donde te agarran.</li>
            <li>Por eso la regla más simple que existe: <strong>si no suena, no te pagaron</strong>.</li>
          </ul>
        </div>

        <h2>Qué es exactamente el «yape falso»</h2>
        <p>
          El yape falso no es un hackeo ni un robo de tu cuenta. Es mucho más simple, y
          por eso funciona tan bien: el estafador <strong>te muestra una pantalla que
          parece una confirmación de pago</strong> y se lleva la mercadería antes de que
          tú puedas comprobar nada.
        </p>
        <p>Hay dos versiones que verás en cualquier bodega o puesto de mercado:</p>
        <ul>
          <li>
            <strong>La captura editada.</strong> Una foto real de un yapeo anterior, con
            el monto, el nombre y la hora cambiados en cualquier editor de imágenes. Toma
            menos de un minuto y queda perfecta.
          </li>
          <li>
            <strong>La app imitadora.</strong> Aplicaciones que reproducen la pantalla de
            confirmación de Yape con los datos que el estafador escriba, e incluso un
            sonido parecido al de la app real. En la pantalla del cliente se ve idéntica.
          </li>
        </ul>
        <p>
          Las dos comparten la misma debilidad, y ahí está tu defensa: <strong>ninguna
          puede hacer aparecer un pago en tu equipo</strong>. Solo pueden mentirle a
          tus ojos, no a tu celular.
        </p>

        <h2>Las 7 señales de un Yape falso</h2>
        <p>
          Si estás obligado a mirar la pantalla del cliente, al menos revisa esto. Ojo:
          esto es un parche, no una verificación —más abajo está la forma segura.
        </p>
        <ol>
          <li>
            <strong>El apuro.</strong> «Ya te yapeé, tengo que irme corriendo». La prisa
            es la herramienta principal del estafador: necesita que no revises.
          </li>
          <li>
            <strong>Te muestra la pantalla y la guarda al toque.</strong> Un segundo de
            vista y el celular vuelve al bolsillo.
          </li>
          <li>
            <strong>El nombre del destinatario no es el tuyo</strong>, está incompleto o
            tiene una letra distinta. Es el error más común de las capturas editadas.
          </li>
          <li>
            <strong>La hora no cuadra</strong> con el reloj de tu celular, o la
            confirmación dice una fecha que no es la de hoy.
          </li>
          <li>
            <strong>Los tipos de letra bailan.</strong> Números con distinto grosor,
            tamaño o alineación dentro de la misma pantalla: señal de montaje.
          </li>
          <li>
            <strong>Es una foto de una foto.</strong> No es la app abierta sino una imagen
            en la galería, un pantallazo enviado por WhatsApp o una captura reenviada.
          </li>
          <li>
            <strong>No hay notificación en tu celular.</strong> Esta es la única señal que
            de verdad decide: si en tu equipo no entró nada, no entró nada.
          </li>
        </ol>

        <h2>La única verificación que sirve</h2>
        <p>
          Todo lo anterior es leer el trabajo del estafador buscándole errores. Es
          cansado, es lento y en hora punta simplemente no lo vas a hacer.
        </p>
        <p>
          Da vuelta la lógica: <strong>no verifiques en el celular del cliente, verifica
          en el tuyo.</strong> Un pago real de Yape o Plin siempre produce una
          notificación en el equipo que recibe el dinero. Esa notificación la genera el
          sistema, no el cliente, y nadie que esté al otro lado del mostrador puede
          fabricarla.
        </p>
        <p className="destacado">
          Si el pago no está en tu celular, no existe. No importa qué tan bonita se vea
          la pantalla que te muestran.
        </p>

        <h2>El problema real: nadie revisa el celular en hora punta</h2>
        <p>
          Todo bodeguero sabe que debería revisar. El asunto es que a las siete de la
          noche, con cinco personas en cola, agarrar el celular, desbloquearlo, abrir
          Yape y buscar el movimiento por cada venta de S/ 8 es imposible. Y el estafador
          lo sabe: por eso llega justo a esa hora.
        </p>
        <p>Y hay una segunda versión del problema, igual de común:</p>
        <p>
          <strong>El dueño no está en la tienda.</strong> El Yape del negocio está en su
          celular, pero él está en su casa o haciendo trámites. Sus trabajadores no
          pueden confirmar ningún pago sin llamarlo, y mientras contestan el teléfono la
          cola crece. Al final terminan aceptando capturas «de confianza», que es
          exactamente donde entra el fraude.
        </p>

        <h2>Cómo confirmar cada pago sin agarrar el celular</h2>
        <p>
          La solución práctica es que el pago <strong>se anuncie solo, en voz alta</strong>,
          apenas llega. Tú sigues atendiendo, el cliente escucha lo mismo que tú, y no
          hay nada que revisar.
        </p>
        <p>
          Eso es <a href="/">PagoYa</a>: una app para el celular Android donde recibes los
          Yapes del negocio. Cuando la notificación real de tu billetera entra, PagoYa la
          canta: <em>«¡PagoYaaa! Te yapearon 25 soles»</em>. Y como el anuncio nace de esa
          notificación del sistema —no de una captura ni de la palabra de nadie— ninguna
          app trucha puede hacerlo sonar.
        </p>
        <ul>
          <li><strong>Suena para los dos.</strong> Tú y el cliente lo escuchan al mismo tiempo. Se acabó el «muéstrame tu pantalla».</li>
          <li><strong>Suena aunque no estés.</strong> El mismo pago se anuncia en el celular del dueño y en los de sus trabajadores, estén donde estén.</li>
          <li><strong>Queda registrado.</strong> Si tienes dudas, abres la app: si el pago no está en tu historial, no cayó.</li>
          <li><strong>Con tu Yape de siempre.</strong> No cambias de banco, no compras ningún aparato, no pagas comisión por venta.</li>
        </ul>
        <p>
          Un detalle que vale la pena entender: el anuncio sale de <strong>tu</strong>
          equipo, no del celular del cliente. Aunque alguien reprodujera un audio grabado
          desde su teléfono, tu caja no habría cantado y el pago no estaría en tu
          historial. Por eso la regla funciona en los dos sentidos.
        </p>

        <h2>Qué hacer si ya te estafaron</h2>
        <ol>
          <li>Anota todo mientras está fresco: hora exacta, monto, nombre que mostraba la pantalla, descripción de la persona.</li>
          <li>Guarda el video de tu cámara de seguridad si la tienes; suele ser lo único aprovechable.</li>
          <li>Denuncia el hecho ante la Policía Nacional del Perú, en comisaría o por denuncia digital.</li>
          <li>Reporta el caso al canal oficial de atención de tu billetera.</li>
          <li>Avisa a los negocios vecinos: estas estafas se hacen en ruta, de puesto en puesto, el mismo día.</li>
        </ol>
        <p>
          Recuperar el dinero es difícil y honestamente poco probable. Lo que sí está en
          tu mano es que no vuelva a pasar.
        </p>

        <h2>Preguntas frecuentes sobre el yape falso</h2>
        <div className="faq">
          {PREGUNTAS.map((q) => (
            <details key={q.p}>
              <summary>{q.p}</summary>
              <div><p>{q.r}</p></div>
            </details>
          ))}
        </div>

        <CajaCta
          donde="guia-yape-falso"
          titulo="Que tu caja te avise, no tus ojos"
          texto="Instalamos PagoYa contigo en 5 minutos, con el celular y el Yape que ya tienes. Empiezas gratis."
          mensaje="Hola PagoYa, me interesa protegerme del yape falso en mi negocio"
        />

        <Relacionadas actual={GUIA.ruta} />
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
