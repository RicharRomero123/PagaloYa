import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import { wsp } from '../../lib/enlaces';

export const metadata = {
  title: 'Eliminar mi cuenta y mis datos — PagoYa',
  description:
    'Cómo solicitar la eliminación de tu cuenta de PagoYa (pe.pagoya.app) y de todos tus datos: desde la app o por correo. Qué se elimina, qué se conserva y en qué plazos.',
  alternates: { canonical: '/eliminar-datos/' },
};

export default function EliminarDatos() {
  return (
    <>
      <Cabecera />
      <main className="legal">
        <p className="miga"><a href="/">← Volver a pagoya.pe</a></p>
        <h1>Eliminar mi cuenta y mis datos</h1>
        <p className="vigencia">App: PagoYa (<code>pe.pagoya.app</code>)</p>

        <p>
          Puedes pedir la eliminación de tu cuenta de PagoYa y de todos los datos
          asociados en cualquier momento y sin dar explicaciones. Hay dos formas:
        </p>

        <h2>Opción 1: desde la App</h2>
        <ol>
          <li>Abre PagoYa en tu celular.</li>
          <li>Entra a <strong>Ajustes → Cuenta</strong>.</li>
          <li>Toca <strong>&quot;Eliminar mi cuenta&quot;</strong> y confirma.</li>
        </ol>
        <p>
          Si tu versión de la App todavía no muestra esta opción, usa la opción 2:
          el resultado es el mismo.
        </p>

        <h2>Opción 2: por correo o WhatsApp</h2>
        <ul>
          <li>
            <strong>Correo:</strong> escribe a{' '}
            <a href="mailto:pimentel@inklop.com?subject=Eliminar%20mi%20cuenta%20PagoYa">pimentel@inklop.com</a>{' '}
            con el asunto <strong>&quot;Eliminar mi cuenta PagoYa&quot;</strong>, desde el mismo
            correo de Google con el que entras a la App (así verificamos que la cuenta es tuya).
          </li>
          <li>
            <strong>WhatsApp:</strong> escríbenos a nuestro{' '}
            <a href={wsp('Hola PagoYa, quiero eliminar mi cuenta y mis datos')} target="_blank" rel="noopener">
              WhatsApp
            </a>{' '}
            indicando el correo con el que usas la App.
          </li>
        </ul>
        <p>Confirmaremos la recepción de tu solicitud y te avisaremos cuando esté completada.</p>

        <h2>Qué se elimina</h2>
        <ul>
          <li>Tu cuenta (nombre, correo y foto de perfil de Google Sign-In).</li>
          <li>Los datos de tu comercio y la relación con sus miembros.</li>
          <li>Todo el historial de pagos registrado por tu comercio, incluidos los nombres de pagadores.</li>
          <li>Los identificadores técnicos de tus dispositivos (tokens de notificaciones push).</li>
        </ul>

        <h2>Qué puede conservarse temporalmente</h2>
        <ul>
          <li>Datos estrictamente necesarios para obligaciones legales, contables o de facturación (por ejemplo, comprobantes de pago de tu plan), solo por el plazo que la ley exija.</li>
          <li>Registros técnicos anónimos que ya no permiten identificarte.</li>
        </ul>

        <h2>Plazos</h2>
        <ul>
          <li>Eliminación de los sistemas activos: máximo <strong>30 días</strong> desde tu solicitud.</li>
          <li>Eliminación de copias de seguridad: máximo <strong>90 días</strong>.</li>
        </ul>

        <h2>¿Solo quieres borrar el historial, sin cerrar tu cuenta?</h2>
        <p>
          También puedes pedirlo por los mismos canales: eliminamos el historial de
          pagos de tu comercio y tu cuenta sigue activa.
        </p>

        <p>
          Más detalles sobre cómo tratamos tus datos en nuestra{' '}
          <a href="/privacidad/">Política de privacidad</a>.
        </p>
      </main>
      <Pie compacto />
    </>
  );
}
