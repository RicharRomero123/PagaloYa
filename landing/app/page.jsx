import Cabecera from '../components/Cabecera';
import Pie from '../components/Pie';
import WspFlotante from '../components/WspFlotante';
import DemoVoz from '../components/DemoVoz';
import Cifras from '../components/Cifras';
import { IconoWsp } from '../components/Iconos';
import { wsp, MENSAJE_GENERAL } from '../lib/enlaces';

export const metadata = {
  alternates: { canonical: '/' },
};

const datosApp = {
  '@context': 'https://schema.org',
  '@type': 'SoftwareApplication',
  name: 'PagoYa',
  operatingSystem: 'Android',
  applicationCategory: 'FinanceApplication',
  description:
    'Anunciador de voz de pagos Yape y Plin para comercios peruanos. Anuncia cada pago real en el local y en los celulares del dueño y sus trabajadores.',
  offers: { '@type': 'Offer', price: '0', priceCurrency: 'PEN' },
  url: 'https://pagoya.pe/',
};

const datosFaq = {
  '@context': 'https://schema.org',
  '@type': 'FAQPage',
  mainEntity: [
    { '@type': 'Question', name: '¿PagoYa funciona en iPhone?', acceptedAnswer: { '@type': 'Answer', text: 'El celular que recibe los Yapes debe ser Android, porque iPhone no permite leer notificaciones de otras apps (regla de Apple). Para escuchar los pagos a distancia sí puedes usar iPhone.' } },
    { '@type': 'Question', name: '¿PagoYa entra a mi cuenta o ve mi plata?', acceptedAnswer: { '@type': 'Answer', text: 'No. PagoYa nunca pide tu clave ni entra a tu Yape ni a tu banco. Solo lee la notificación de pago que ya llega a tu celular, la misma que tú ves, y la anuncia por voz.' } },
    { '@type': 'Question', name: '¿Necesito internet para que funcione?', acceptedAnswer: { '@type': 'Answer', text: 'El celular donde recibes tus Yapes ya usa internet para eso mismo; PagoYa aprovecha esa conexión y consume muy poco. Para escuchar los pagos a distancia, el otro celular también necesita conexión.' } },
    { '@type': 'Question', name: '¿Gasta mucha batería?', acceptedAnswer: { '@type': 'Answer', text: 'No. PagoYa no mantiene la pantalla prendida ni descarga cosas pesadas: solo escucha las notificaciones de tus billeteras. Además te guiamos para configurar tu celular y que el sistema no duerma la app.' } },
    { '@type': 'Question', name: '¿Funciona con el celular bloqueado?', acceptedAnswer: { '@type': 'Answer', text: 'Sí. El anuncio suena aunque la pantalla esté apagada. En la instalación te ayudamos a configurar los permisos según la marca de tu celular para que nunca se detenga.' } },
    { '@type': 'Question', name: '¿Y si Yape cambia sus notificaciones?', acceptedAnswer: { '@type': 'Answer', text: 'PagoYa se actualiza solo desde nuestro servidor, sin que tengas que reinstalar nada. Si Yape cambia el texto de sus avisos, lo ajustamos por ti.' } },
    { '@type': 'Question', name: '¿Pueden falsificar el sonido de PagoYa?', acceptedAnswer: { '@type': 'Answer', text: 'El anuncio suena en tu equipo, no en el celular del cliente, y cada pago anunciado queda registrado en tu app. Si tu equipo no lo cantó y no aparece en tu historial, ese pago no cayó.' } },
    { '@type': 'Question', name: '¿Cuántos teléfonos pueden escuchar los pagos?', acceptedAnswer: { '@type': 'Answer', text: 'Con el plan Gratis, 1 celular. Con Caserito y Patrón se conectan el celular del negocio, el del dueño y los de los trabajadores.' } },
    { '@type': 'Question', name: '¿En qué se diferencia PagoYa de un QR parlante?', acceptedAnswer: { '@type': 'Answer', text: 'Los QR parlantes te obligan a cobrar con otra empresa y a pagar el aparato. PagoYa funciona con el Yape que ya tienes, empiezas gratis con tu propio celular y además avisa al dueño aunque no esté en la tienda.' } },
    { '@type': 'Question', name: '¿Qué pasa si se acaba mi plan?', acceptedAnswer: { '@type': 'Answer', text: 'No pierdes tu cuenta ni tu historial: tu negocio pasa al plan Gratis (1 celular con anuncio de voz) y reactivas tu plan cuando quieras.' } },
  ],
};

function Check() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M20 6L9 17l-5-5" />
    </svg>
  );
}

export default function Inicio() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(datosApp) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(datosFaq) }} />

      <Cabecera conNav />

      <main>
        {/* ============ HERO ============ */}
        <section className="hero">
          <div className="wrap">
            <div>
              <span className="antetitulo">Funciona con la billetera que ya usas · Hecho en Perú</span>
              <h1>
                Cuando te pagan, <span>tu celular <em className="marcador">lo canta</em>.</span>
              </h1>
              <p className="hero-bajada">
                Apenas te cae un Yape o un Plin, tu celular dice{' '}
                <strong>cuánto te pagaron, en voz alta</strong>. Tú sigues
                atendiendo, sin agarrar nada.
              </p>
              <p className="hero-regla">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.3" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d="M12 3l7 3v5c0 4.5-3 8.5-7 10-4-1.5-7-5.5-7-10V6z" />
                  <path d="M9 12l2 2 4-4" />
                </svg>
                Si no suena, no te pagaron
              </p>
              <div className="hero-botones">
                <a className="btn btn-wsp js-wsp" data-donde="hero" href={wsp(MENSAJE_GENERAL)} target="_blank" rel="noopener">
                  <IconoWsp />
                  Quiero probarlo gratis
                </a>
                <a className="btn btn-claro" href="#como-funciona">Ver cómo funciona</a>
              </div>
              <ul className="hero-sellos">
                <li><Check /> Gratis para empezar</li>
                <li><Check /> Con la billetera que ya usas</li>
                <li><Check /> Sin comisiones por venta</li>
                <li><Check /> Nunca te pedimos tu clave</li>
              </ul>
            </div>

            {/* Mockup del producto: teléfono dibujado en CSS puro */}
            <div className="mockup">
              <div className="mockup-marco">
                <div className="sol" aria-hidden="true"></div>
                <div className="telefono" role="img" aria-label="Celular en el mostrador mostrando una notificación de pago y el anuncio de voz de PagoYa: ¡PagoYa! Te yapearon 25 soles">
                  <div className="telefono-pantalla">
                    <div className="telefono-notch" aria-hidden="true"></div>
                    <div className="pantalla-hora" aria-hidden="true">
                      <strong>12:31</strong>
                      <span>jueves 30 de julio · La bodega está llena</span>
                    </div>
                    <div className="tarjeta-notif" aria-hidden="true">
                      <span className="icono-app">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2.5" y="6" width="19" height="13" rx="3" /><path d="M2.5 10h19" /><path d="M6.5 15h4" /></svg>
                      </span>
                      <div>
                        <h5>Tu billetera <small>· ahora</small></h5>
                        <p>Confirmación de pago: recibiste S/ 25.00</p>
                      </div>
                    </div>
                    <div className="tarjeta-anuncio" aria-hidden="true">
                      <span className="anuncio-cab">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round"><path d="M4 10v4h4l6 5V5l-6 5H4z" fill="currentColor" stroke="none" /><path d="M17.5 9.5a4 4 0 0 1 0 5" /></svg>
                        Anunciando en voz alta
                      </span>
                      <span className="anuncio-marca">¡PagoYaaa!</span>
                      <span className="anuncio-texto">Te yapearon 25 soles</span>
                      <div className="ondas"><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div>
                    </div>
                  </div>
                </div>
                <div className="chip chip--casa" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 11l9-7 9 7" /><path d="M5 10v10h14V10" /></svg>
                  <span>Sonó en casa del dueño<small>a 8 km de la tienda</small></span>
                </div>
                <div className="chip chip--gente" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><circle cx="9" cy="8" r="3" /><circle cx="17" cy="9" r="2.5" /><path d="M3.5 19c.5-3 2.8-5 5.5-5s5 2 5.5 5" /><path d="M15.5 14.5c2.3.2 4 2 4.5 4.5" /></svg>
                  <span>Tu gente también lo escuchó<small>sin tocar tu cuenta</small></span>
                </div>
              </div>
              <DemoVoz />
            </div>
          </div>
        </section>

        {/* Letrero corredizo */}
        <div className="letrero" aria-hidden="true">
          <div className="letrero-tira">
            <span><b>¡PagoYaaa!</b> Te yapearon 25 soles&nbsp;&nbsp;★&nbsp;&nbsp;Si no suena, no te pagaron&nbsp;&nbsp;★&nbsp;&nbsp;Compatible con Yape y Plin&nbsp;&nbsp;★&nbsp;&nbsp;Hecho en Perú, para caseros&nbsp;&nbsp;★&nbsp;&nbsp;</span>
            <span><b>¡PagoYaaa!</b> Te yapearon 25 soles&nbsp;&nbsp;★&nbsp;&nbsp;Si no suena, no te pagaron&nbsp;&nbsp;★&nbsp;&nbsp;Compatible con Yape y Plin&nbsp;&nbsp;★&nbsp;&nbsp;Hecho en Perú, para caseros&nbsp;&nbsp;★&nbsp;&nbsp;</span>
          </div>
        </div>

        {/* ============ CIFRAS: PIZARRA DE BODEGA (se cuentan solas) ============ */}
        <Cifras />

        {/* ============ QUÉ ES ============ */}
        <section className="seccion" id="que-es">
          <div className="wrap">
            <div className="quees-grid">
              <div className="reveal">
                <span className="antetitulo">Qué es PagoYa</span>
                <h2>El vigilante de tu caja, con voz propia</h2>
                <p className="lead">
                  PagoYa es una app para el celular Android donde recibes los Yapes de tu
                  negocio. Cuando cae un pago de verdad, tu billetera manda su notificación
                  de siempre y PagoYa la <strong>canta en voz alta al instante</strong>:
                  «¡PagoYaaa! Te yapearon 25 soles».
                </p>
                <ul className="quees-lista">
                  <li>
                    <span className="quees-icono">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M4 10v4h4l6 5V5l-6 5H4z" /><path d="M17.5 9.5a4 4 0 0 1 0 5M20 7a8 8 0 0 1 0 10" /></svg>
                    </span>
                    <div>
                      <strong>Canta al toque</strong>
                      <p>Tú y el cliente lo escuchan al mismo tiempo, sin agarrar el celular ni cortar la atención.</p>
                    </div>
                  </li>
                  <li>
                    <span className="quees-icono">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M3 11l9-7 9 7" /><path d="M5 10v10h14V10" /><path d="M12 13v4" /></svg>
                    </span>
                    <div>
                      <strong>Te sigue a donde vayas</strong>
                      <p>Si no estás en la tienda, el mismo anuncio suena en tu celular, en tu casa o donde andes.</p>
                    </div>
                  </li>
                  <li>
                    <span className="quees-icono">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M6 2.5h12v19l-2-1.5-2 1.5-2-1.5-2 1.5-2-1.5-2 1.5z" /><path d="M9 7.5h6M9 11h6M9 14.5h3.5" /></svg>
                    </span>
                    <div>
                      <strong>Lleva la cuenta por ti</strong>
                      <p>Cada pago queda anotado, con el total del día listo para cuando cuadres en la noche.</p>
                    </div>
                  </li>
                </ul>
                <p className="quees-regla">
                  Y como el anuncio nace de la notificación real del sistema, ninguna app
                  trucha puede hacerlo sonar: <em className="marcador">si no suena, no te pagaron</em>.
                </p>
              </div>

              {/* La boleta de cierre de caja: el producto en papel */}
              <div className="boleta-zona reveal reveal-tarde">
                <span className="sello-tampon">Todo cantado</span>
                <div className="boleta" role="img" aria-label="La cuenta del día de PagoYa: todos los pagos anunciados por voz, total S/ 214.50">
                  <div className="boleta-cab">
                    <strong>La cuenta del día · PagoYa</strong>
                    <span>Bodega Doña Rosa — jueves 30 de julio</span>
                  </div>
                  <ul className="boleta-filas" aria-hidden="true">
                    <li className="boleta-fila">
                      <span className="hora">07:45</span>
                      <span className="detalle">Yape</span>
                      <span className="monto">S/ 18.50</span>
                      <span className="sono"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6L9 17l-5-5" /></svg> sonó</span>
                    </li>
                    <li className="boleta-fila">
                      <span className="hora">09:12</span>
                      <span className="detalle">Yape</span>
                      <span className="monto">S/ 25.00</span>
                      <span className="sono"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6L9 17l-5-5" /></svg> sonó</span>
                    </li>
                    <li className="boleta-fila">
                      <span className="hora">11:38</span>
                      <span className="detalle">Plin</span>
                      <span className="monto">S/ 12.00</span>
                      <span className="sono"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6L9 17l-5-5" /></svg> sonó</span>
                    </li>
                    <li className="boleta-fila">
                      <span className="hora">12:31</span>
                      <span className="detalle">Yape</span>
                      <span className="monto">S/ 25.00</span>
                      <span className="sono"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6L9 17l-5-5" /></svg> sonó</span>
                    </li>
                  </ul>
                  <div className="boleta-total" aria-hidden="true">
                    <span>14 pagos hoy</span>
                    <strong>S/ 214.50</strong>
                  </div>
                  <p className="boleta-pie" aria-hidden="true">Si no sonó, no está aquí, casero.</p>
                </div>
                <div className="boleta-nota">
                  <span className="nota-mano nota-mano--azul">Así cuadras en la noche: sin sumar a mano</span>
                </div>
              </div>
            </div>

            {/* Sí es / No es, lado a lado */}
            <div className="quees-abajo reveal">
              <div className="tarjeta-si">
                <h3>PagoYa sí es</h3>
                <ul className="lista-check">
                  <li>Un aviso en voz alta de cada Yape o Plin que te cae</li>
                  <li>Una alarma contra el yape falso y las capturas trucadas</li>
                  <li>Los oídos del dueño cuando no está en la tienda</li>
                  <li>La cuenta del día lista, sin sumar a mano</li>
                </ul>
              </div>
              <div className="tarjeta-no">
                <h3>PagoYa no es</h3>
                <ul className="lista-x">
                  <li>Un banco ni una billetera: tu plata nunca pasa por PagoYa</li>
                  <li>Un aparato de tarjetas: no cambias tu forma de cobrar</li>
                  <li>Una app que pide tu clave o entra a tu cuenta</li>
                  <li>Un servicio que te cobra comisión por cada venta</li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* ============ EL PROBLEMA: EL YAPE FALSO ============ */}
        <section className="seccion sobre-azul" id="yape-falso">
          <div className="wrap">
            <div className="sec-cab reveal">
              <span className="antetitulo antetitulo--claro">El problema es real</span>
              <h2>El yape falso ataca cuando estás más ocupado</h2>
              <p>
                Apps truchas y capturas editadas engañan al ojo todos los días en bodegas
                y mercados de todo el Perú. Estas dos escenas te van a sonar:
              </p>
            </div>

            <div className="historias">
              <article className="historia reveal">
                <p className="cita">«Ya te yapeé, casero.» <b>Y nunca llegó.</b></p>
                <p>
                  Hora punta. Un cliente te enseña la pantalla un segundo, tú con tres
                  personas en cola apenas la miras, le entregas la mercadería. En la
                  noche cuadras la caja y ese pago no existe: la captura era editada,
                  igualita a la de verdad.
                </p>
              </article>
              <article className="historia reveal reveal-tarde">
                <p className="cita">«¿Cayó el yapeo?» <b>Y tú, llamando toda la tarde.</b></p>
                <p>
                  El Yape del negocio está en tu celular, pero tú no estás en la tienda.
                  Tu gente tiene que llamarte por cada venta para confirmar si el pago
                  llegó, y mientras contestan el teléfono, la cola crece y las ventas se
                  enfrían.
                </p>
              </article>
            </div>

            <p className="remate reveal">Con PagoYa la regla es una sola: <b>si no suena, no te pagaron.</b></p>

            <div className="escudos">
              <article className="escudo reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M12 3l7 3v5c0 4.5-3 8.5-7 10-4-1.5-7-5.5-7-10V6z" /><path d="M9 12l2 2 4-4" /></svg>
                <h3>Solo pagos de verdad</h3>
                <p>El anuncio nace de la notificación oficial que tu billetera pone en tu celular. Tu parlante no se deja engañar con capturas.</p>
              </article>
              <article className="escudo reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M4 10v4h4l6 5V5l-6 5H4z" /><path d="M17.5 9.5a4 4 0 0 1 0 5M20 7a8 8 0 0 1 0 10" /></svg>
                <h3>Suena para todos</h3>
                <p>Tú y el cliente lo escuchan al mismo tiempo, fuerte y claro. Se acabó el «a ver, muéstrame tu pantalla».</p>
              </article>
              <article className="escudo reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M3 11l9-7 9 7" /><path d="M5 10v10h14V10" /><path d="M10 20v-6h4v6" /></svg>
                <h3>Te avisa donde estés</h3>
                <p>El mismo pago suena en la tienda y en tu celular, en tu casa o en la calle. Tu gente atiende tranquila, sin llamarte.</p>
              </article>
            </div>
          </div>
        </section>

        {/* ============ CÓMO FUNCIONA ============ */}
        <section className="seccion fondo-blanco" id="como-funciona">
          <div className="wrap">
            <div className="sec-cab reveal">
              <span className="antetitulo">Cómo funciona</span>
              <h2>Listo en 5 minutos, sin trámites ni papeleo</h2>
              <p>No necesitas cuenta nueva, ni POS, ni firmar nada con un banco. Solo tu celular de siempre.</p>
            </div>
            <ol className="pasos">
              <li className="reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="7" y="2.5" width="10" height="19" rx="2.5" /><path d="M12 6v6m0 0l-2.5-2.5M12 12l2.5-2.5" /><path d="M10.5 18.5h3" /></svg>
                <h3>Instala la app</h3>
                <p>Descarga PagoYa en el celular donde te llegan los Yapes del negocio. Cualquier Android desde la versión 8, hasta el más sencillo.</p>
                <p>Creas tu cuenta con tu correo de Google y le pones nombre a tu negocio. Nada más.</p>
              </li>
              <li className="reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 8-3 8h18s-3-1-3-8" /><path d="M10.5 20a1.8 1.8 0 0 0 3 0" /></svg>
                <h3>Dale permiso una vez</h3>
                <p>Autorizas que PagoYa lea las notificaciones de tus billeteras. Solo esas: las demás se ignoran y no se guardan.</p>
                <p>Tu Yape sigue igualito: sin claves, sin cambios, sin tocar tu cuenta. La app te guía pasito a paso según la marca de tu celular.</p>
              </li>
              <li className="reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M4 10v4h4l6 5V5l-6 5H4z" /><path d="M17.5 9.5a4 4 0 0 1 0 5M20 7a8 8 0 0 1 0 10" /></svg>
                <h3>Suena solo, para siempre</h3>
                <p>Desde ese momento, cada pago real se anuncia al instante: «¡PagoYaaa! Te yapearon 25 soles». Con la pantalla apagada, con la app cerrada, en plena hora punta.</p>
                <p>Y cada pago queda anotado, con el total del día listo para cuando cuadres.</p>
              </li>
            </ol>

            {/* Modo escucha remoto */}
            <div className="remoto reveal">
              <span className="antetitulo">Cuando no estás en la tienda</span>
              <h3>¿Saliste? Igual te enteras primero</h3>
              <p>
                Si tienes trabajadores, conectas <strong>sus celulares también</strong>.
                Cuando cae un pago en el celular del negocio, en segundos suena en todos
                los celulares que tú autorizaste. Tu gente cobra tranquila sin llamarte,
                y tú escuchas tus ventas desde tu casa. Todos escuchan el aviso, pero
                nadie entra a tu cuenta.
              </p>
              <div className="diagrama">
                <div className="nodo">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="6" y="2.5" width="10" height="19" rx="2.5" /><path d="M19 9a5.5 5.5 0 0 1 0 6" /><path d="M9.5 18.5h3" /></svg>
                  <strong>El celular del negocio</strong>
                  <span>Recibe el Yape y lo anuncia en la tienda</span>
                </div>
                <div className="flecha" aria-hidden="true">→</div>
                <div className="nodo">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M17.5 19a4.5 4.5 0 0 0 .4-9A6.5 6.5 0 0 0 5.6 8.4 4.8 4.8 0 0 0 6.5 18z" /></svg>
                  <strong>La nube segura</strong>
                  <span>Reenvía el pago cifrado en menos de 2 segundos</span>
                </div>
                <div className="flecha" aria-hidden="true">→</div>
                <div className="nodo">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="9" cy="8" r="3" /><circle cx="17" cy="9" r="2.5" /><path d="M3.5 19c.5-3 2.8-5 5.5-5s5 2 5.5 5" /><path d="M15.5 14.5c2.3.2 4 2 4.5 4.5" /></svg>
                  <strong>Tu celular y los de tu gente</strong>
                  <span>El mismo anuncio suena donde cada uno esté</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ============ BILLETERAS Y REQUISITOS ============ */}
        <section className="seccion" id="compatibilidad">
          <div className="wrap">
            <div className="sec-cab reveal">
              <span className="antetitulo">Compatibilidad</span>
              <h2>¿Funciona con mi billetera?</h2>
              <p>
                Es lo primero que todos preguntan. La respuesta corta: casi siempre sí,
                y ahora te explicamos por qué.
              </p>
            </div>

            {/* El mecanismo explicado en criollo: es el diferenciador de verdad */}
            <div className="compat-clave reveal">
              <h3>PagoYa no entra a ninguna billetera</h3>
              <p>
                No se conecta a Yape, ni a tu banco, ni a nada. Lo único que hace es{' '}
                <strong>escuchar el avisito</strong> que tu billetera ya te manda al
                celular cuando te pagan, y decirlo en voz alta.
              </p>
              <p className="compat-regla">
                Si tu billetera te avisa en el celular, PagoYa puede cantarlo.
              </p>
            </div>

            <div className="compat-lista">
              <article className="compat-item compat-item--listo reveal">
                <span className="compat-estado">Listo</span>
                <h3>Yape</h3>
                <p>
                  Funciona desde el día uno. Y si alguien te manda un Plin a tu número o
                  a tu QR de Yape, ese pago también suena.
                </p>
              </article>
              <article className="compat-item compat-item--listo reveal reveal-tarde">
                <span className="compat-estado">Listo</span>
                <h3>Plin</h3>
                <p>
                  Funciona con los avisos de Plin que te llegan en la app de tu banco.
                </p>
              </article>
              <article className="compat-item reveal">
                <span className="compat-estado compat-estado--cola">La agregamos</span>
                <h3>La app de tu banco</h3>
                <p>
                  BCP, BBVA, Interbank, Scotiabank y las demás avisan por notificación.
                  Escríbenos con una foto del aviso y la dejamos andando.
                </p>
              </article>
              <article className="compat-item reveal reveal-tarde">
                <span className="compat-estado compat-estado--cola">La agregamos</span>
                <h3>Cualquier otra</h3>
                <p>
                  Agora, Tunki, Ligo, billeteras nuevas… Si te avisa en el celular,
                  la podemos hacer sonar. Mándanos cuál usas.
                </p>
              </article>
            </div>

            <div className="compat-cierre reveal">
              <p>
                <strong>Y se agregan sin que tú hagas nada.</strong> Cuando sumamos una
                billetera nueva —o cuando alguna cambia el texto de sus avisos— tu app lo
                aprende sola desde nuestro servidor. No reinstalas, no actualizas, no vas
                a ninguna tienda de apps.
              </p>
              <p className="compat-remate">
                Un parlante comprado no puede hacer eso: viene amarrado a una sola empresa
                de cobro y ahí se queda.
              </p>
              <a className="btn btn-wsp js-wsp" data-donde="compatibilidad" href={wsp('Hola PagoYa, quiero saber si funciona con mi billetera. Yo cobro con: ')} target="_blank" rel="noopener">
                <IconoWsp />
                Pregúntanos por la tuya
              </a>
            </div>

            <div className="sec-cab reveal" style={{ marginTop: '72px' }}>
              <h2 style={{ fontSize: 'clamp(1.4rem,3.6vw,1.9rem)' }}>¿Qué necesitas? Casi nada</h2>
            </div>
            <div className="requisitos">
              <article className="requisito reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="7" y="2.5" width="10" height="19" rx="2.5" /><path d="M10.5 18.5h3" /></svg>
                <h3>Un Android cualquiera</h3>
                <p>Desde Android 8 (2017 en adelante). No necesitas un celular caro: el de la caja sirve.</p>
              </article>
              <article className="requisito reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="2.5" y="6" width="19" height="13" rx="3" /><path d="M2.5 10h19" /><path d="M6.5 15h4" /></svg>
                <h3>Tu billetera de siempre</h3>
                <p>La app de Yape o del banco con Plin, la misma que ya usas para cobrar. Sin abrir cuentas nuevas.</p>
              </article>
              <article className="requisito reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M2 9a15 15 0 0 1 20 0" /><path d="M5.5 12.5a10 10 0 0 1 13 0" /><path d="M9 16a5.5 5.5 0 0 1 6 0" /><circle cx="12" cy="19.3" r="1.3" fill="currentColor" stroke="none" /></svg>
                <h3>El internet que ya usas</h3>
                <p>Los mismos datos o WiFi con los que te llegan los Yapes. PagoYa consume muy poco: puro texto.</p>
              </article>
              <article className="requisito reveal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3.5 2" /></svg>
                <h3>5 minutos, una vez</h3>
                <p>Instalar, dar el permiso y probar con un yapeo de S/ 0.10. Después, PagoYa trabaja solo.</p>
              </article>
            </div>
          </div>
        </section>

        {/* ============ DIFERENCIALES ============ */}
        <section className="seccion fondo-blanco" id="diferenciales">
          <div className="wrap">
            <div className="sec-cab reveal">
              <span className="antetitulo">Por qué PagoYa</span>
              <h2>Hecho para el casero, no para el banco</h2>
            </div>
            <div className="difs">
              <article className="dif reveal">
                <span className="dif-icono">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="7" y="2.5" width="10" height="19" rx="2.5" /><path d="M9.5 12.5l2 2 3.5-3.5" /></svg>
                </span>
                <h3>Con tu Yape de siempre</h3>
                <p>No cambias de banco, no te cambias de empresa para cobrar, no compras ningún aparato. PagoYa trabaja con la cuenta que ya tienes y no te cobra comisión por venta: lo que te yapean es tuyo completito.</p>
              </article>
              <article className="dif reveal reveal-tarde">
                <span className="dif-icono">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M3 11l9-7 9 7" /><path d="M5 10v10h14V10" /><path d="M12 13v4" /></svg>
                </span>
                <h3>Tú tranquilo en tu casa</h3>
                <p>Cada pago de tu tienda también suena en tu celular, estés donde estés. Almuerzas tranquilo, haces tus trámites, y tu caja te va cantando las ventas.</p>
              </article>
              <article className="dif reveal">
                <span className="dif-icono">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="9" cy="8" r="3" /><circle cx="17" cy="9" r="2.5" /><path d="M3.5 19c.5-3 2.8-5 5.5-5s5 2 5.5 5" /><path d="M15.5 14.5c2.3.2 4 2 4.5 4.5" /></svg>
                </span>
                <h3>Tu gente conectada</h3>
                <p>Tus trabajadores escuchan cada pago en sus propios celulares, sin entrar a tu cuenta ni ver tu saldo. Atienden, cobran y despachan sin llamarte por cada venta.</p>
              </article>
              <article className="dif reveal reveal-tarde">
                <span className="dif-icono">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M12 3l7 3v5c0 4.5-3 8.5-7 10-4-1.5-7-5.5-7-10V6z" /><path d="M9 12l2 2 4-4" /></svg>
                </span>
                <h3>Solo pagos reales</h3>
                <p>El anuncio nace de la notificación oficial del sistema, no de capturas ni de la palabra del cliente. Es tu mejor defensa contra el yape falso, y de yapa te ordena la caja.</p>
              </article>
            </div>
          </div>
        </section>

        {/* ============ COMPARACIÓN HONESTA ============ */}
        <section className="seccion" id="comparacion">
          <div className="wrap">
            <div className="sec-cab reveal">
              <span className="antetitulo">Comparación honesta</span>
              <h2>¿Y qué otras opciones tengo, casero?</h2>
              <p>Te lo decimos derecho, sin florearte. Estas son tus alternativas hoy:</p>
            </div>
            <div className="comparacion">
              <article className="opcion reveal">
                <h3>Seguir como estás</h3>
                <p className="opcion-sub">Revisar el celular a cada rato o confiar en la captura</p>
                <ul className="lista-x">
                  <li>El yape falso te agarra justo en hora punta</li>
                  <li>Paras de atender por cada cobro, para revisar</li>
                  <li>Si no estás, tu gente te llama por cada venta</li>
                  <li>En la noche cuadras a puro ojo</li>
                </ul>
              </article>
              <article className="opcion opcion--pagoya reveal reveal-tarde">
                <span className="badge-flotante">Así trabaja PagoYa</span>
                <h3>PagoYa en tu celular</h3>
                <p className="opcion-sub">Gratis para empezar, con tu mismo Yape</p>
                <ul className="lista-check">
                  <li>Cada pago real suena solo, sin mirar la pantalla</li>
                  <li>Sigues cobrando con tu mismo Yape y tu mismo QR</li>
                  <li>S/ 0 de equipo: usas el celular que ya tienes</li>
                  <li>El dueño y su gente escuchan a distancia</li>
                  <li>Te lleva la cuenta del día, lista para cuadrar</li>
                </ul>
              </article>
              <article className="opcion reveal">
                <h3>QR con parlante de un procesador</h3>
                <p className="opcion-sub">Por ejemplo, el QR parlante de Izipay</p>
                <ul className="lista-x">
                  <li>Pagas el equipo (alrededor de S/ 129)</li>
                  <li>Tienes que cobrar con esa empresa, no con tu Yape</li>
                  <li>Suena en la tienda, pero no le avisa al dueño que está lejos</li>
                </ul>
                <ul className="lista-check" style={{ marginTop: '10px' }}>
                  <li>Buena opción si ya cobras con esa empresa</li>
                </ul>
              </article>
            </div>
            <p className="comparacion-nota reveal">
              Y cuando salga nuestro plan Patrón, el parlante de mostrador PagoYa viene
              incluido en la membresía, sin pagar el equipo y sin cambiar tu Yape.
              Datos referenciales a julio de 2026; las marcas mencionadas pertenecen a sus titulares.
            </p>
          </div>
        </section>

        {/* ============ PLANES ============ */}
        <section className="seccion fondo-blanco" id="planes">
          <div className="wrap">
            <div className="sec-cab reveal">
              <span className="antetitulo">Planes y precios</span>
              <h2>Empieza gratis. Crece cuando quieras.</h2>
              <p>Precios por comercio, sin contratos amarrados. Se paga fácil: en efectivo o con tu mismo Yape.</p>
            </div>
            <div className="planes">
              <article className="plan reveal">
                <h3 className="plan-nombre">Gratis</h3>
                <p className="plan-precio">S/ 0</p>
                <p className="plan-para">Para probar hoy mismo</p>
                <ul>
                  <li>1 celular</li>
                  <li>Anuncio de voz de cada pago</li>
                  <li>Protección anti yape falso</li>
                </ul>
                <a className="btn btn-claro js-wsp" data-donde="plan-gratis" href={wsp('Hola PagoYa, quiero empezar con el plan Gratis')} target="_blank" rel="noopener">
                  Empezar gratis
                </a>
              </article>

              <article className="plan plan--favorito reveal">
                <span className="sticker">El favorito</span>
                <h3 className="plan-nombre">Caserito</h3>
                <p className="plan-precio">S/ 12.90 <small>/mes</small></p>
                <p className="plan-para">Para negocios con equipo</p>
                <ul>
                  <li>Todo lo del plan Gratis</li>
                  <li>Varios celulares conectados</li>
                  <li>El dueño escucha desde su casa</li>
                  <li>La cuenta del día lista para cuadrar</li>
                </ul>
                <a className="btn btn-naranja js-wsp" data-donde="plan-caserito" href={wsp('Hola PagoYa, me interesa el plan Caserito')} target="_blank" rel="noopener">
                  Lo quiero
                </a>
              </article>

              <article className="plan plan--pronto reveal">
                <span className="sticker sticker--azul">Próximamente</span>
                <h3 className="plan-nombre">Patrón</h3>
                <p className="plan-precio">S/ 24.90 <small>/mes</small></p>
                <p className="plan-para">Para el mostrador que manda</p>
                <ul>
                  <li>Todo lo del plan Caserito</li>
                  <li>Parlante PagoYa de mostrador incluido</li>
                  <li>Sin pagar el equipo: S/ 0 de entrada</li>
                  <li>Siempre encendido, suena fuerte</li>
                </ul>
                <a className="btn btn-claro js-wsp" data-donde="plan-patron" href={wsp('Hola PagoYa, avísenme cuando salga el plan Patrón con parlante')} target="_blank" rel="noopener">
                  Avísame cuando salga
                </a>
              </article>
            </div>
            <p className="planes-nota reveal">
              Con el plan Patrón el parlante te lo prestamos nosotros mientras tu plan
              esté activo: no lo compras. Y si un mes no puedes pagar, no pierdes nada:
              vuelves al plan Gratis y lo reactivas cuando quieras.
            </p>
          </div>
        </section>

        {/* ============ FAQ ============ */}
        <section className="seccion" id="preguntas">
          <div className="wrap">
            <div className="sec-cab reveal">
              <span className="antetitulo">Preguntas frecuentes</span>
              <h2>Lo que todo casero pregunta</h2>
            </div>
            <div className="faq reveal">
              <details>
                <summary>¿Funciona en iPhone?</summary>
                <div>
                  <p>
                    El celular que <strong>recibe</strong> los Yapes debe ser Android: iPhone no
                    permite que ninguna app lea las notificaciones de otras (es regla de Apple,
                    no nuestra). Pero para <strong>escuchar</strong> los pagos a distancia sí
                    puedes usar iPhone sin problema.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿PagoYa entra a mi cuenta o ve mi plata?</summary>
                <div>
                  <p>
                    No. Nunca te pedimos tu clave, no entramos a tu Yape ni a tu banco y no
                    podemos mover tu dinero. PagoYa solo lee la notificación de pago que ya
                    llega a tu celular —la misma que tú ves— y la anuncia por voz. Las demás
                    notificaciones de tu celular se ignoran y no se guardan.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿Necesito internet para que funcione?</summary>
                <div>
                  <p>
                    El celular donde recibes tus Yapes ya usa internet para eso mismo, así que
                    no necesitas nada extra: PagoYa aprovecha esa conexión y consume muy poco
                    (puro texto, nada de videos). Para escuchar los pagos a distancia, el
                    celular del dueño o del trabajador también necesita estar conectado.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿Gasta mucha batería?</summary>
                <div>
                  <p>
                    No. PagoYa no mantiene la pantalla prendida ni descarga cosas pesadas:
                    se queda quietito escuchando las notificaciones de tus billeteras.
                    Además, en la instalación te guiamos para configurar tu celular (sobre
                    todo Xiaomi, Samsung, Huawei y Oppo) y que el sistema no duerma ni a
                    PagoYa ni a tu Yape. Ese ajuste hasta te salva de perder avisos que hoy
                    ya se te pierden sin darte cuenta.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿Funciona con el celular bloqueado o la app cerrada?</summary>
                <div>
                  <p>
                    Sí. El anuncio suena con la pantalla apagada y sin tener la app abierta:
                    trabaja como un servicio en segundo plano, igual que tu despertador.
                    Si tu celular es de los que &quot;matan&quot; apps para ahorrar batería, la app te
                    avisa y te enseña a darle permiso para que nunca se detenga.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿Y si Yape cambia sus notificaciones?</summary>
                <div>
                  <p>
                    Nos actualizamos solos desde nuestro servidor, sin que tengas que
                    reinstalar ni tocar nada. Si Yape o Plin cambian el texto de sus avisos,
                    lo ajustamos por ti y tu caja sigue hablando.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿No pueden falsificar el sonido, poniendo un audio?</summary>
                <div>
                  <p>
                    Buena pregunta, de bodeguero vivo. La diferencia está en <strong>dónde</strong>{' '}
                    suena: el anuncio sale de tu equipo, no del celular del cliente. Un audio
                    ajeno no hace sonar tu caja ni aparece en tu app. Y ante cualquier duda,
                    abres PagoYa y el pago tiene que estar en tu historial: si tu equipo no lo
                    cantó y no está registrado, no cayó.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿Dice el nombre del cliente en voz alta?</summary>
                <div>
                  <p>
                    Por defecto no: la voz solo canta el monto, para no ventilar el nombre de
                    tu cliente delante de todo el mercado. El nombre siempre lo ves en la
                    pantalla, y si quieres que también se anuncie, tú lo activas.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿Cuántos teléfonos pueden escuchar los pagos?</summary>
                <div>
                  <p>
                    Con el plan Gratis, 1 celular (el del negocio). Con Caserito y Patrón
                    conectas además el celular del dueño y los de tus trabajadores, cada uno
                    con su propia cuenta y sin acceso a tu Yape. ¿Tienes un negocio grande o
                    varios locales? Escríbenos y lo armamos a tu medida.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿En qué se diferencia de un QR parlante?</summary>
                <div>
                  <p>
                    Los QR parlantes que se venden te obligan a cobrar con otra empresa y a
                    pagar por el aparato. PagoYa funciona con el Yape que ya tienes, empiezas
                    gratis con tu propio celular, y además le avisa al dueño aunque no esté en
                    la tienda. Y cuando salga el plan Patrón, el parlante te lo prestamos
                    nosotros sin que lo compres.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿Qué pasa si se me acaba el plan?</summary>
                <div>
                  <p>
                    Nada grave, casero: no pierdes tu cuenta ni tu historial. Tu negocio pasa
                    al plan Gratis (1 celular con anuncio de voz) y reactivas tu plan cuando
                    quieras, por WhatsApp, en efectivo o con tu mismo Yape.
                  </p>
                </div>
              </details>
              <details>
                <summary>¿PagoYa es de Yape o del BCP?</summary>
                <div>
                  <p>
                    No. PagoYa es una app independiente, hecha en Perú, compatible con Yape y
                    Plin. No pertenece a ningún banco ni billetera, y justo por eso funciona
                    sin que cambies nada de lo que ya usas.
                  </p>
                </div>
              </details>
            </div>
          </div>
        </section>

        {/* ============ CTA FINAL ============ */}
        <section className="seccion cta-final">
          <div className="wrap">
            <h2>¿Listo para que tu caja hable?</h2>
            <p>
              Estamos abriendo los primeros cupos. Escríbenos por WhatsApp y te ayudamos
              a instalar PagoYa en tu negocio en 5 minutos. Gratis para empezar.
            </p>
            <a className="btn js-wsp" data-donde="cta-final" href={wsp(MENSAJE_GENERAL)} target="_blank" rel="noopener">
              <IconoWsp ancho={24} />
              Escríbenos por WhatsApp
            </a>
            <small>Respondemos rapidito. Sin compromiso y sin letra chiquita.</small>
          </div>
        </section>
      </main>

      <Pie />
      <WspFlotante />
    </>
  );
}
