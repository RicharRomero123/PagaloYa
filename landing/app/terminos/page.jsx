import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import { SITIO } from '../../lib/seo';

export const metadata = {
  title: 'Términos y condiciones',
  description:
    'Términos y condiciones de uso de PagoYa (pe.pagoya.app): qué es el servicio, cómo se usa, los planes, la relación con las billeteras, responsabilidades y contacto.',
  alternates: { canonical: '/terminos/' },
};

export default function Terminos() {
  return (
    <>
      <Cabecera />
      <main className="legal">
        <p className="miga"><a href="/">← Volver a pagoya.pe</a></p>
        <h1>Términos y condiciones de PagoYa</h1>
        <p className="vigencia">Fecha de vigencia: 1 de agosto de 2026</p>

        <div className="caja-resumen">
          <p><strong>En simple:</strong></p>
          <ul>
            <li>PagoYa te anuncia por voz los pagos que tus billeteras te avisan por notificación. Es una ayuda, no un reemplazo de tu app de Yape, Plin o tu banco.</li>
            <li>La verificación final de un pago siempre es tu propia app de la billetera. Confía en tu historial, no solo en el anuncio.</li>
            <li>Empiezas gratis. Los planes de pago son opcionales y los puedes dejar cuando quieras.</li>
            <li>PagoYa es independiente: no pertenece a Yape, al BCP, a Plin ni a ningún banco.</li>
          </ul>
        </div>

        <p>
          Estos Términos y Condiciones (los &quot;Términos&quot;) regulan el uso de la
          aplicación móvil <strong>PagoYa</strong> (paquete Android{' '}
          <code>pe.pagoya.app</code>, la &quot;App&quot;) y del sitio web{' '}
          <strong>pagoya.pe</strong> (el &quot;Sitio&quot;), en adelante el
          &quot;Servicio&quot;. Al crear una cuenta y usar el Servicio, aceptas estos
          Términos. Si no estás de acuerdo, no uses el Servicio.
        </p>

        <h2>1. Qué es el Servicio</h2>
        <p>
          PagoYa es un anunciador de pagos para comercios: detecta las notificaciones
          reales de pago que las billeteras digitales (Yape, Plin y aplicaciones
          bancarias compatibles) muestran en el teléfono Android del comercio, las
          anuncia por voz en el local y las sincroniza con los teléfonos autorizados
          del mismo comercio (por ejemplo, el del dueño cuando no está o los de sus
          trabajadores).
        </p>
        <p>
          <strong>PagoYa no es una billetera, un banco ni un procesador de pagos.</strong>{' '}
          No mueve dinero, no cobra por ti y no interviene en la transacción entre tú y
          tu cliente. Solo escucha y anuncia notificaciones que ya llegaron a tu
          celular.
        </p>

        <h2>2. Requisitos para usar el Servicio</h2>
        <ul>
          <li>Debes ser mayor de edad y usar el Servicio para tu actividad comercial o negocio.</li>
          <li>El celular que recibe los pagos debe ser Android y tener instaladas las apps de tus billeteras con sus notificaciones activadas.</li>
          <li>Debes conceder el permiso de acceso a notificaciones de Android, que la App usa solo para las billeteras compatibles (ver la <a href="/privacidad/">Política de privacidad</a>).</li>
          <li>Eres responsable de la veracidad de los datos que registres y de mantener la seguridad de tu cuenta de acceso.</li>
        </ul>

        <h2>3. Tu cuenta y tu comercio</h2>
        <p>
          Para usar el Servicio creas una cuenta (por ejemplo, con Google Sign-In) y
          registras tu comercio. El dueño puede invitar a trabajadores a su comercio
          mediante un código; quien tenga acceso al comercio verá los pagos de ese
          comercio. Eres responsable de a quién invitas y de retirar el acceso cuando
          corresponda.
        </p>

        <h2>4. Planes y pagos</h2>
        <ul>
          <li><strong>Plan Gratis:</strong> anuncio de voz de cada pago en 1 celular, con protección anti yape falso. Sin costo.</li>
          <li><strong>Plan Caserito (S/ 12.90 al mes):</strong> de 2 a 3 celulares conectados, dueño escuchando a distancia, historial y cierre de caja.</li>
          <li><strong>Plan Patrón (S/ 24.90 al mes, próximamente):</strong> de 4 celulares a más, todo lo del plan Caserito y un parlante PagoYa de mostrador en préstamo (comodato) mientras tu plan esté activo.</li>
        </ul>
        <p>
          El número de celulares de cada plan <strong>incluye el celular del negocio</strong>,
          es decir, aquel donde se reciben las notificaciones de pago, y no solo los
          equipos adicionales. Si necesitas conectar más equipos de los que permite tu
          plan, escríbenos y lo acordamos contigo.
        </p>
        <p>
          Los precios están expresados en soles peruanos (PEN) e incluyen los
          impuestos aplicables. Podemos actualizar los precios avisándote con
          antelación. Si dejas de pagar un plan, tu cuenta y tu historial no se pierden:
          tu comercio pasa al plan Gratis y puedes reactivar tu plan cuando quieras. En
          el plan Patrón, el parlante es en préstamo (no lo compras) y debe devolverse
          si terminas el servicio.
        </p>

        <h2>5. Uso correcto del Servicio</h2>
        <p>Al usar PagoYa te comprometes a:</p>
        <ul>
          <li>Usar el Servicio solo para fines legítimos de tu negocio.</li>
          <li>No intentar vulnerar, copiar, descompilar ni interferir con el funcionamiento de la App o del Sitio.</li>
          <li>No usar el Servicio para engañar a terceros ni para fines contrarios a la ley.</li>
          <li>Respetar los datos personales de tus clientes (por ejemplo, los nombres de pagadores que aparezcan), tratándolos conforme a la ley peruana.</li>
        </ul>

        <h2>6. Relación con las billeteras y los bancos</h2>
        <p>
          PagoYa es un producto independiente. No pertenece a Yape, al BCP, a Plin, a
          Izipay ni a ningún banco o procesador, y no está afiliado ni respaldado por
          ellos. Las marcas mencionadas pertenecen a sus respectivos titulares y se
          citan solo para indicar compatibilidad. El funcionamiento de PagoYa depende de
          que esas apps sigan mostrando notificaciones de pago; si una billetera cambia
          la forma de sus notificaciones, actualizamos la App para seguir
          reconociéndolas, pero no controlamos esas apps ni sus servicios.
        </p>

        <h2>7. Anti yape falso: alcance y límites</h2>
        <p>
          PagoYa te ayuda a confirmar cobros anunciando por voz las notificaciones
          reales de pago: <strong>si no suena, no te pagaron</strong>. Esto reduce
          mucho el riesgo del yape falso, porque nadie puede hacer sonar una
          notificación en tu equipo desde el suyo.
        </p>
        <p>
          Sin embargo, PagoYa <strong>no garantiza la eliminación total del fraude</strong>{' '}
          ni sustituye tu criterio. La verificación definitiva de un pago siempre es tu
          propia app de la billetera o de tu banco: ante cualquier duda, revisa tu
          historial ahí. PagoYa es una herramienta de apoyo, no una garantía de pago.
        </p>

        <h2>8. Disponibilidad del Servicio</h2>
        <p>
          Hacemos lo posible por mantener el Servicio funcionando, pero puede tener
          interrupciones por mantenimiento, fallas de terceros (por ejemplo, la
          plataforma en la nube o la red del teléfono), o factores fuera de nuestro
          control. El anuncio de voz en el propio celular funciona sin internet; la
          sincronización con otros equipos y el historial sí requieren conexión.
        </p>

        <h2>9. Limitación de responsabilidad</h2>
        <p>
          El Servicio se ofrece &quot;tal cual&quot;. En la medida que permita la ley,
          PagoYa no será responsable por pérdidas derivadas de: pagos que no se
          anunciaron por falta de notificación, de permisos, de batería o de conexión;
          decisiones que tomes confiando únicamente en el anuncio sin verificar en tu
          billetera; o del mal uso del Servicio. Nada en estos Términos limita
          responsabilidades que la ley no permita excluir.
        </p>

        <h2>10. Propiedad intelectual</h2>
        <p>
          La marca PagoYa, el nombre, el logotipo, la mascota, los textos y el software
          son de su titular y están protegidos. No puedes usarlos sin autorización.
          Conservas la titularidad de los datos de tu comercio; nos autorizas a tratarlos
          para prestar el Servicio conforme a la <a href="/privacidad/">Política de privacidad</a>.
        </p>

        <h2>11. Suspensión y terminación</h2>
        <p>
          Puedes dejar de usar el Servicio y{' '}
          <a href="/eliminar-datos/">eliminar tu cuenta y tus datos</a> cuando quieras.
          Podemos suspender o terminar el acceso de una cuenta que incumpla estos
          Términos o use el Servicio de forma fraudulenta o ilegal.
        </p>

        <h2>12. Cambios a estos Términos</h2>
        <p>
          Podemos actualizar estos Términos. Publicaremos la nueva versión en{' '}
          <a href={`${SITIO}/terminos/`}>los Términos publicados en el sitio</a> con su fecha de
          vigencia. Si el cambio es importante, te avisaremos dentro de la App. Seguir
          usando el Servicio después de un cambio significa que lo aceptas.
        </p>

        <h2>13. Ley aplicable</h2>
        <p>
          Estos Términos se rigen por las leyes de la República del Perú. Cualquier
          controversia se someterá a los tribunales competentes del Perú, sin perjuicio
          de los derechos que te reconozca la normativa de protección al consumidor.
        </p>

        <h2>14. Contacto</h2>
        <p>
          Para cualquier consulta sobre estos Términos:
          <br />
          Correo: <a href="mailto:pimentel@inklop.com">pimentel@inklop.com</a>
          <br />
          Web: <a href={`${SITIO}/`}>{SITIO.replace('https://', '')}</a>
          <br />
          Aplicación: PagoYa (<code>pe.pagoya.app</code>) en Google Play.
        </p>
      </main>
      <Pie compacto />
    </>
  );
}
