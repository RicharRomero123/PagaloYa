import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { CajaCta, Relacionadas } from '../../components/Guia';
import { meta, migas, articulo, faq, ld } from '../../lib/seo';
import { SEGMENTOS } from '../../lib/segmentos';

const SEG = SEGMENTOS.find((s) => s.ruta === '/para-delivery/');

export const metadata = meta({
  titulo: 'Delivery: confirma el pago sin tener la cuenta',
  descripcion: SEG.descripcion,
  ruta: SEG.ruta,
});

const PREGUNTAS = [
  {
    p: '¿Mi motorizado puede ver mi saldo o entrar a mi Yape?',
    r: 'No. El repartidor solo escucha el anuncio del pago en su celular: el monto y la hora. No entra a tu cuenta, no ve tu saldo, no puede mover tu dinero y no recibe tu clave. Tu Yape se queda en el celular del negocio, tal cual está hoy.',
  },
  {
    p: '¿Cuántos repartidores puedo conectar?',
    r: 'Depende del plan, y el número siempre incluye el celular del negocio. Con el plan Gratis, 1 celular: el del negocio. Con el plan Caserito, de 2 a 3 en total, así que te alcanza para el negocio y uno o dos repartidores. Con el plan Patrón, de 4 a más. Si tienes una flota grande, escríbenos y lo armamos a tu medida.',
  },
  {
    p: '¿Y si el motorizado no tiene señal justo en ese momento?',
    r: 'El anuncio le llega apenas su celular recupera conexión, y el pago siempre queda en el historial de la app. En zonas sin cobertura conviene que el repartidor abra la app al llegar: ahí ve todos los cobros del día, aunque no haya escuchado el aviso en el momento.',
  },
  {
    p: '¿Funciona si mi repartidor tiene iPhone?',
    r: 'Para escuchar los pagos, sí. La restricción es solo para el celular que RECIBE los Yapes del negocio: ese tiene que ser Android, porque iPhone no permite que ninguna app lea notificaciones de otras. El repartidor puede usar iPhone sin problema.',
  },
  {
    p: '¿Qué pasa si un repartidor deja de trabajar conmigo?',
    r: 'Lo desconectas desde la app y deja de recibir los avisos al instante. Como nunca tuvo acceso a tu cuenta, no hay claves que cambiar ni nada que arreglar con tu banco.',
  },
  {
    p: '¿El cliente puede falsificar el anuncio de pago?',
    r: 'El anuncio nace de la notificación real que tu billetera pone en el celular del negocio, no de una imagen ni de la palabra de nadie. Por eso lo que suena corresponde a un pago que efectivamente entró. Un cliente no puede hacer sonar el celular de tu repartidor desde su propio teléfono.',
  },
];

export default function ParaDelivery() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(articulo({ titulo: metadata.title, descripcion: SEG.descripcion, ruta: SEG.ruta, publicado: SEG.publicado })) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(faq(PREGUNTAS)) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(migas([{ nombre: 'Inicio', ruta: '/' }, { nombre: SEG.corto, ruta: SEG.ruta }])) }} />

      <Cabecera />
      <main className="legal articulo">
        <nav className="migas" aria-label="Migas de pan">
          <a href="/">Inicio</a>
          <span aria-hidden="true">›</span>
          <span aria-current="page">Para delivery y motorizados</span>
        </nav>

        <h1>Delivery: confirma el pago sin tener la cuenta del negocio</h1>
        <p className="vigencia">Actualizado el 3 de agosto de 2026 · Lectura de 6 minutos</p>

        <div className="caja-resumen">
          <p><strong>El problema de siempre, en corto:</strong></p>
          <ul>
            <li>El repartidor <strong>no tiene el Yape del negocio</strong>, así que no puede confirmar nada.</li>
            <li>Hoy toma <strong>captura de pantalla y se la manda al jefe</strong> por WhatsApp… y espera.</li>
            <li>Esa captura es justo lo que se falsifica, y esa espera pasa con el cliente en la puerta.</li>
            <li>Con PagoYa el celular del repartidor <strong>escucha el pago real al instante</strong>, sin entrar a tu cuenta.</li>
          </ul>
        </div>

        <h2>Cómo se confirma hoy un pago en el reparto</h2>
        <p>
          Si tienes motorizados, este baile te va a sonar. El repartidor llega, el
          cliente le dice «ya te yapeé» y le muestra la pantalla. Como el Yape del
          negocio está en el celular de la tienda —no en el suyo—, el repartidor
          hace lo único que puede: <strong>le toma una foto a la pantalla del
          cliente y te la manda por WhatsApp</strong>. Y se queda parado ahí,
          esperando que le contestes.
        </p>
        <p>Ese flujo tiene cuatro agujeros, y los cuatro cuestan plata:</p>
        <ol>
          <li>
            <strong>La espera es en la puerta del cliente.</strong> Con la moto
            prendida, el pedido en la mano y alguien mirándolo. Nadie aguanta ahí
            tres minutos: al final el repartidor entrega y se va confiando.
          </li>
          <li>
            <strong>La señal falla justo donde más se necesita.</strong> Sótanos
            de edificios, quintas, pasajes, zonas altas. Sin señal no hay captura
            que mandar ni respuesta que esperar.
          </li>
          <li>
            <strong>La captura es exactamente lo que se falsifica.</strong> Una
            imagen editada o una app imitadora se ven idénticas a la real. Estás
            usando como prueba justo la cosa más fácil de trucar.
          </li>
          <li>
            <strong>Tú estás ocupado.</strong> En hora punta, atendiendo o
            cocinando, no vas a revisar el celular por cada reparto. Al final el
            repartidor decide solo, y cuando falta plata la discusión es con él.
          </li>
        </ol>
        <p className="destacado">
          El repartidor no necesita tu cuenta. Necesita <em>enterarse</em> del pago.
          Son dos cosas distintas, y hasta ahora se resolvían con la misma llave.
        </p>

        <h2>Qué cambia con PagoYa</h2>
        <p>
          PagoYa separa las dos cosas. <strong>Tu Yape se queda donde está</strong>,
          en el celular del negocio. Lo que viaja es solo el aviso: cuando entra un
          pago de verdad, el celular del repartidor <strong>lo canta al
          instante</strong> —«¡PagoYaaa! Te yapearon 25 soles»— esté donde esté.
        </p>
        <ul>
          <li><strong>Sin captura y sin esperar.</strong> El repartidor escucha el pago mientras el cliente todavía está enfrente.</li>
          <li><strong>Sin darle acceso a nada.</strong> Escucha el monto; no ve tu saldo, no entra a tu cuenta, no tiene tu clave.</li>
          <li><strong>Lo que suena es real.</strong> El aviso nace de la notificación que tu billetera pone en el celular del negocio, no de una imagen. Una captura editada no hace sonar nada.</li>
          <li><strong>Queda registrado.</strong> Cada cobro anunciado entra al historial, con hora y monto. Cuando cuadras al final del día, está todo ahí.</li>
          <li><strong>Sirve para varios a la vez.</strong> Si tienes tres motorizados en la calle, los tres escuchan sus cobros.</li>
        </ul>
        <p>
          Y no cambias nada de cómo cobras: mismo Yape, mismo QR, mismo número. Sin
          afiliarte a otro procesador y sin comisión por venta.
        </p>

        <h2>Cómo lo pones en marcha</h2>
        <ol>
          <li>
            <strong>Instala PagoYa en el celular del negocio</strong>, ese donde te
            caen los Yapes. Cualquier Android desde la versión 8.
          </li>
          <li>
            <strong>Dale el permiso una vez</strong> para que lea las
            notificaciones de tus billeteras. Tu Yape sigue igualito.
          </li>
          <li>
            <strong>Genera un código de 6 dígitos por cada repartidor.</strong> Él
            instala la app en su celular, mete el código y queda conectado a tu
            negocio.
          </li>
          <li>
            <strong>Prueba con un yapeo de S/ 0.10</strong> y comprueba que suena
            en los dos equipos. Listo.
          </li>
        </ol>

        <h2>Lo que conviene tener claro antes</h2>
        <p>Te lo decimos derecho, sin florearte:</p>
        <ul>
          <li>
            <strong>El celular del negocio tiene que quedarse encendido y con
            internet.</strong> Es el que recibe los Yapes y el que dispara los
            avisos. Si se apaga o se queda sin datos, nadie escucha nada. En la
            instalación te ayudamos a dejarlo bien configurado — es el mismo ajuste
            de la guía de{' '}
            <a href="/no-me-llegan-notificaciones-yape/">notificaciones que no llegan</a>.
          </li>
          <li>
            <strong>El repartidor necesita conexión</strong> para escuchar en el
            momento. Sin señal, el aviso le entra al recuperar cobertura y el pago
            igual queda en su historial.
          </li>
          <li>
            <strong>Con casco o con el cliente delante</strong>, no siempre querrás
            que el celular grite el monto. Se puede bajar la voz o dejarlo en aviso
            corto.
          </li>
          <li>
            <strong>Por defecto no se dice el nombre del cliente</strong> en voz
            alta, solo el monto. En la puerta de una casa eso importa.
          </li>
        </ul>

        <h2>A quién le sirve</h2>
        <ul>
          <li><strong>Restaurantes y pollerías con motorizado propio</strong> — el caso más común, y donde más se pierde por confiar en la captura.</li>
          <li><strong>Boticas y farmacias con reparto</strong> — pedidos chicos, muchos al día, márgenes justos.</li>
          <li><strong>Tiendas y minimarkets que reparten</strong> — gas, agua, abarrotes, ferretería.</li>
          <li><strong>Couriers y mensajería</strong> — el mensajero cobra contra entrega para una empresa que no está ahí.</li>
          <li><strong>Repartidores independientes</strong> que cobran a nombre de otro: el Yape es del dueño y ellos solo entregan.</li>
        </ul>

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
          donde="segmento-delivery"
          titulo="Que tus motorizados dejen de mandarte capturas"
          texto="Lo dejamos andando en 5 minutos, con el Yape y los celulares que ya tienen. Empiezas gratis."
          mensaje="Hola PagoYa, tengo repartidores y quiero que confirmen los pagos sin mandarme capturas"
        />

        <Relacionadas actual={SEG.ruta} />
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
