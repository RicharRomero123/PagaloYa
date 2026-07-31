import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import { Migas, CajaCta, Relacionadas } from '../../components/Guia';
import { meta, migas, articulo, faq, ld } from '../../lib/seo';
import { GUIAS } from '../../lib/guias';

const GUIA = GUIAS.find((g) => g.ruta === '/no-me-llegan-notificaciones-yape/');

export const metadata = meta({
  titulo: 'No me llegan las notificaciones de Yape: cómo arreglarlo',
  descripcion: GUIA.descripcion,
  ruta: GUIA.ruta,
});

const PREGUNTAS = [
  {
    p: '¿Por qué no me llegan las notificaciones de Yape?',
    r: 'Casi siempre es una de cuatro cosas: el permiso de notificaciones de la app está apagado, el ahorro de batería del celular está durmiendo la app en segundo plano, el modo No molestar está activo, o el celular perdió la conexión a internet. En celulares Xiaomi, Huawei, Oppo y Samsung la causa más común es el ahorro de batería.',
  },
  {
    p: '¿Cómo activo las notificaciones de Yape en Android?',
    r: 'Entra a Ajustes → Aplicaciones → Yape → Notificaciones y actívalas. Luego, en la misma pantalla de la app, busca Batería o Uso de batería y elige "Sin restricciones" o "Permitir en segundo plano". Ese segundo paso es el que casi todos se saltan.',
  },
  {
    p: '¿Por qué mi Xiaomi no me avisa de los Yapes?',
    r: 'MIUI cierra las apps en segundo plano de forma muy agresiva. Además del permiso de notificaciones necesitas entrar a Ajustes → Aplicaciones → Yape → Ahorro de batería y elegir "Sin restricciones", y activar el bloqueo de la app en la pantalla de apps recientes (deslizar hacia abajo sobre la tarjeta y tocar el candado).',
  },
  {
    p: 'Me llegan las notificaciones pero no las escucho, ¿qué hago?',
    r: 'Revisa que la notificación de Yape tenga sonido asignado (Ajustes → Aplicaciones → Yape → Notificaciones → categoría de pagos) y que el modo No molestar esté desactivado. Si el problema es que en hora punta no las oyes entre el ruido, la solución práctica es que el pago se anuncie en voz alta y no con un tono corto.',
  },
  {
    p: '¿Puedo hacer que los pagos se anuncien en voz alta?',
    r: 'Sí. PagoYa es una app que lee la notificación de pago que ya llega a tu celular y la dice en voz alta al instante, con el monto. No reemplaza la notificación de Yape: la aprovecha, y por eso primero tienes que tener las notificaciones funcionando.',
  },
];

export default function NotificacionesYape() {
  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(articulo({ titulo: metadata.title, descripcion: GUIA.descripcion, ruta: GUIA.ruta, publicado: GUIA.publicado })) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(faq(PREGUNTAS)) }} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(migas([{ nombre: 'Inicio', ruta: '/' }, { nombre: 'Guías', ruta: '/guias/' }, { nombre: GUIA.corto, ruta: GUIA.ruta }])) }} />

      <Cabecera />
      <main className="legal articulo">
        <Migas titulo="No me llegan las notificaciones de Yape" />

        <h1>No me llegan las notificaciones de Yape: cómo arreglarlo</h1>
        <p className="vigencia">Actualizado el 31 de julio de 2026 · Lectura de 5 minutos</p>

        <div className="caja-resumen">
          <p><strong>La causa suele ser una de estas cuatro:</strong></p>
          <ul>
            <li>El <strong>permiso de notificaciones</strong> de la app está apagado.</li>
            <li>El <strong>ahorro de batería</strong> está durmiendo la app en segundo plano. <em>(La más común.)</em></li>
            <li>El <strong>modo No molestar</strong> o el silencio están activos.</li>
            <li>El celular <strong>se quedó sin datos ni WiFi</strong>.</li>
          </ul>
        </div>

        <p>
          Para un negocio esto no es un fastidio: es plata. Si no te avisa, no sabes si
          te pagaron, y terminas revisando la app a cada rato o aceptando la captura que
          te muestra el cliente. Vamos a arreglarlo en orden, de lo más probable a lo
          menos probable.
        </p>

        <h2>Paso 1: revisa el permiso de notificaciones</h2>
        <p>Es lo básico, y a veces se apaga solo tras una actualización del sistema.</p>
        <ol>
          <li>Entra a <strong>Ajustes</strong> de tu celular.</li>
          <li>Toca <strong>Aplicaciones</strong> (o «Apps») y busca <strong>Yape</strong>.</li>
          <li>Entra a <strong>Notificaciones</strong> y comprueba que el interruptor principal esté encendido.</li>
          <li>Dentro verás categorías. Asegúrate de que la de <strong>pagos o transferencias recibidas</strong> esté activa y con sonido.</li>
        </ol>
        <p>Haz lo mismo con la app de tu banco si cobras por Plin.</p>

        <h2>Paso 2: saca la app del ahorro de batería</h2>
        <p>
          Este es el que resuelve la mayoría de casos y el que casi nadie hace. Android
          «duerme» las apps que no usas por horas para ahorrar batería, y una app dormida
          no te avisa nada. En el celular de la caja, que pasa el día quieto sobre el
          mostrador, pasa todo el tiempo.
        </p>
        <ol>
          <li>Ajustes → <strong>Aplicaciones</strong> → <strong>Yape</strong>.</li>
          <li>Entra a <strong>Batería</strong> (o «Uso de batería»).</li>
          <li>Elige <strong>Sin restricciones</strong> / <strong>Permitir actividad en segundo plano</strong>.</li>
        </ol>

        <h3>Xiaomi, Redmi y POCO (MIUI / HyperOS)</h3>
        <p>
          Son los más agresivos cerrando apps. Además de lo anterior:
        </p>
        <ul>
          <li>Ajustes → Aplicaciones → Yape → <strong>Ahorro de batería</strong> → <strong>Sin restricciones</strong>.</li>
          <li>En la misma ficha, activa <strong>Inicio automático</strong>.</li>
          <li>Abre la pantalla de apps recientes, desliza hacia abajo sobre la tarjeta de Yape y toca el <strong>candado</strong> para que el sistema no la cierre.</li>
        </ul>

        <h3>Samsung</h3>
        <ul>
          <li>Ajustes → <strong>Mantenimiento del dispositivo</strong> → <strong>Batería</strong> → <strong>Límites de uso en segundo plano</strong>.</li>
          <li>Comprueba que Yape <strong>no</strong> esté en «Aplicaciones en suspensión» ni en «Aplicaciones en suspensión profunda».</li>
        </ul>

        <h3>Huawei y Honor</h3>
        <ul>
          <li>Ajustes → <strong>Batería</strong> → <strong>Inicio de aplicaciones</strong>.</li>
          <li>Pon Yape en <strong>gestión manual</strong> y activa las tres opciones: inicio automático, inicio secundario y ejecución en segundo plano.</li>
        </ul>

        <h3>Oppo, realme y vivo</h3>
        <ul>
          <li>Ajustes → <strong>Batería</strong> → <strong>Consumo de energía en segundo plano</strong> → permite Yape.</li>
          <li>Busca también <strong>Inicio automático</strong> y actívalo para la app.</li>
        </ul>

        <h2>Paso 3: descarta No molestar y el silencio</h2>
        <ul>
          <li>Baja la cortina de notificaciones y comprueba que <strong>No molestar</strong> esté apagado.</li>
          <li>Revisa que el celular no esté en <strong>silencio</strong> o solo vibración.</li>
          <li>Si usas un modo «Enfoque» o una rutina programada, comprueba que no esté silenciando la app.</li>
        </ul>

        <h2>Paso 4: conexión, espacio y versión</h2>
        <ul>
          <li>Sin datos ni WiFi no llega ninguna notificación. Si el celular vive en el mostrador, revisa que la señal llegue bien ahí.</li>
          <li>Con la memoria del celular casi llena, Android empieza a matar procesos. Libera espacio.</li>
          <li>Actualiza la app desde Google Play: las versiones muy viejas dan problemas de notificaciones.</li>
          <li>Como último recurso, cierra sesión y vuelve a entrar en la app de tu billetera.</li>
        </ul>

        <h2>Paso 5: haz la prueba</h2>
        <p>
          No des nada por arreglado sin probar. Pídele a alguien de confianza que te
          yapee <strong>S/ 0.10</strong> y comprueba que la notificación entra con el
          celular bloqueado. Si entra, quedó.
        </p>

        <h2>El problema que esto no arregla</h2>
        <p>
          Puede que las notificaciones ya te lleguen perfecto y de todas formas se te
          pasen pagos. Es normal: en hora punta, con el ruido de la calle y el celular
          boca abajo sobre el mostrador, un tono corto no lo escucha nadie. Y si el dueño
          no está en la tienda, sus trabajadores nunca ven esa notificación.
        </p>
        <p>
          Ahí es donde entra <a href="/">PagoYa</a>: lee esa misma notificación —la que
          acabas de arreglar— y la <strong>dice en voz alta</strong> apenas llega:{' '}
          <em>«¡PagoYaaa! Te yapearon 25 soles»</em>. Con la pantalla apagada, sin abrir
          nada, y también en los celulares del dueño y de sus trabajadores aunque estén
          lejos del local.
        </p>
        <p className="destacado">
          Una notificación te avisa si la miras. Un anuncio en voz alta te avisa aunque
          estés atendiendo.
        </p>
        <p>
          Y de paso te ahorra el paso 2 para siempre: al instalarla, PagoYa te guía según
          la marca de tu celular para dejar bien configurados los permisos y el ahorro de
          batería, tanto de la app como de tu billetera.
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
          donde="guia-notificaciones"
          titulo="¿Sigue sin avisarte?"
          texto="Escríbenos por WhatsApp con la marca de tu celular y te ayudamos a dejarlo configurado. Sin costo, seas cliente o no."
          mensaje="Hola PagoYa, no me llegan las notificaciones de Yape. Mi celular es: "
        />

        <Relacionadas actual={GUIA.ruta} />
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
