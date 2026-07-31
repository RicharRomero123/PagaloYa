import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';

export const metadata = {
  title: 'Política de privacidad — PagoYa',
  description:
    'Política de privacidad de PagoYa (pe.pagoya.app): qué datos trata la app, cómo usa el acceso a notificaciones de Android, dónde se almacenan, con quién se comparten y cómo ejercer tus derechos bajo la Ley 29733.',
  alternates: { canonical: '/privacidad/' },
};

export default function Privacidad() {
  return (
    <>
      <Cabecera />
      <main className="legal">
        <p className="miga"><a href="/">← Volver a pagoya.pe</a></p>
        <h1>Política de privacidad de PagoYa</h1>
        <p className="vigencia">Fecha de vigencia: 30 de julio de 2026</p>

        <div className="caja-resumen">
          <p><strong>Resumen en simple:</strong></p>
          <ul>
            <li>PagoYa lee únicamente las notificaciones de pago de tus billeteras (Yape, Plin y apps bancarias) para anunciarlas por voz. Las demás notificaciones de tu celular se ignoran y no se guardan.</li>
            <li>Nunca accedemos a tu cuenta bancaria, no vemos tu saldo y no podemos mover tu dinero.</li>
            <li>No vendemos ni compartimos tus datos con terceros. Solo los miembros de tu propio comercio ven sus pagos.</li>
            <li>Puedes pedir la eliminación de tu cuenta y tus datos cuando quieras: <a href="/eliminar-datos/">cómo hacerlo</a>.</li>
          </ul>
        </div>

        <p>
          Esta Política de privacidad describe cómo <strong>PagoYa</strong> (&quot;nosotros&quot;)
          trata los datos personales de los usuarios de la aplicación móvil{' '}
          <strong>PagoYa</strong> (paquete Android <code>pe.pagoya.app</code>, en adelante
          la &quot;App&quot;) y del sitio web <strong>pagoya.pe</strong>. Al crear una cuenta y usar
          la App aceptas las prácticas descritas aquí.
        </p>
        <p>
          Responsable del tratamiento: el desarrollador de PagoYa, con contacto en{' '}
          <a href="mailto:pimentel@inklop.com">pimentel@inklop.com</a>. El tratamiento se
          realiza conforme a la <strong>Ley N.º 29733, Ley de Protección de Datos
          Personales del Perú</strong>, y su reglamento.
        </p>

        <h2>1. Qué hace la App</h2>
        <p>
          PagoYa es un anunciador de pagos para comercios: detecta las notificaciones
          reales de pago que las billeteras digitales (Yape, Plin y aplicaciones
          bancarias compatibles) muestran en el teléfono Android del comercio, las
          anuncia por voz en el local y las sincroniza con los teléfonos autorizados
          del mismo comercio (por ejemplo, el del dueño cuando no está en el local o
          los de sus trabajadores).
        </p>

        <h2>2. Qué datos tratamos</h2>

        <h3>2.1. Datos de tu cuenta</h3>
        <ul>
          <li><strong>Inicio de sesión con Google (Google Sign-In):</strong> tu nombre, tu dirección de correo electrónico y, si existe, la foto de perfil asociada a tu cuenta de Google.</li>
          <li><strong>Datos del comercio:</strong> el nombre del negocio y los datos que registres al configurarlo (por ejemplo, el rubro o la ubicación, si decides darla).</li>
          <li><strong>Miembros del comercio:</strong> la relación entre la cuenta del dueño y las cuentas de los trabajadores que el dueño invite.</li>
        </ul>

        <h3>2.2. Datos de las notificaciones de pago</h3>
        <p>
          Cuando concedes el acceso a notificaciones (ver sección 3), la App procesa las
          notificaciones emitidas por las aplicaciones de billeteras y bancos
          compatibles, y extrae de ellas únicamente:
        </p>
        <ul>
          <li>El <strong>monto</strong> del pago.</li>
          <li>El <strong>nombre del pagador</strong>, tal como aparece en la propia notificación de la billetera.</li>
          <li>La <strong>billetera de origen</strong> (por ejemplo, Yape o Plin) y la <strong>fecha y hora</strong> del pago.</li>
        </ul>
        <p>
          El nombre del pagador es un dato personal de un tercero. Lo tratamos con
          minimización: <strong>por defecto la App no lo anuncia por voz</strong> (solo se
          muestra en pantalla a los miembros del comercio), y el comerciante puede
          activar o desactivar su anuncio.
        </p>

        <h3>2.3. Datos técnicos</h3>
        <ul>
          <li>Modelo y versión del dispositivo, versión de la App e idioma del sistema.</li>
          <li>Identificadores técnicos necesarios para las notificaciones push (token de Firebase Cloud Messaging).</li>
          <li>Registros de errores y diagnóstico para mantener la App funcionando.</li>
        </ul>

        <h3>2.4. Qué datos NO tratamos</h3>
        <ul>
          <li>No accedemos a tu cuenta de Yape, Plin ni de ningún banco.</li>
          <li>No conocemos tus claves, tu saldo ni tus movimientos completos: solo los pagos entrantes que la billetera anuncia por notificación.</li>
          <li>No podemos iniciar, modificar ni revertir transacciones.</li>
          <li>No leemos ni almacenamos notificaciones ajenas a las billeteras compatibles (mensajes, correos, redes sociales, etc.): se descartan sin procesarse.</li>
        </ul>

        <h2>3. Acceso a notificaciones de Android</h2>
        <p>
          La App solicita el permiso especial de <strong>acceso a notificaciones</strong>{' '}
          de Android (<code>BIND_NOTIFICATION_LISTENER_SERVICE</code>). Este permiso se usa{' '}
          <strong>exclusivamente</strong> para:
        </p>
        <ul>
          <li>Detectar en tiempo real las notificaciones de pago de las billeteras y bancos compatibles.</li>
          <li>Extraer de esas notificaciones los datos descritos en la sección 2.2 para anunciarlos por voz y sincronizarlos con los dispositivos autorizados del mismo comercio.</li>
        </ul>
        <p>
          Aunque el permiso de Android da técnicamente visibilidad sobre todas las
          notificaciones del dispositivo, la App filtra por el paquete de la aplicación
          emisora y <strong>solo procesa las de billeteras y bancos compatibles</strong>;
          el resto se ignora de inmediato y nunca se almacena ni se transmite. Puedes
          revocar este permiso en cualquier momento desde los ajustes de Android
          (Ajustes → Notificaciones → Acceso a notificaciones); la App dejará de
          detectar pagos, pero podrás seguir usando el resto de funciones.
        </p>

        <h2>4. Para qué usamos los datos</h2>
        <ul>
          <li>Anunciar por voz los pagos recibidos en el local.</li>
          <li>Reenviar cada pago a los dispositivos autorizados del mismo comercio (modo &quot;dueño remoto&quot; y teléfonos de trabajadores).</li>
          <li>Mostrar el historial de pagos, totales del día y cierre de caja del comercio.</li>
          <li>Mantener tu sesión, administrar los miembros del comercio y tu plan contratado.</li>
          <li>Dar soporte técnico y corregir errores.</li>
          <li>Mejorar la detección de billeteras. Si activas el <strong>modo aprendizaje</strong> (opcional y con tu consentimiento expreso), las notificaciones de aplicaciones financieras que no coincidan con ningún formato conocido se reportan a nuestro servidor para reconocer formatos nuevos.</li>
        </ul>
        <p>No usamos tus datos para publicidad ni elaboramos perfiles con fines comerciales de terceros.</p>

        <h2>5. Dónde se almacenan los datos</h2>
        <p>
          Los datos se almacenan en <strong>Firebase</strong>, la plataforma de{' '}
          <strong>Google Cloud</strong> (Google LLC), que actúa como encargado del
          tratamiento por cuenta nuestra. Los servidores de Google pueden estar ubicados
          fuera del Perú (por ejemplo, en Estados Unidos), lo que constituye un flujo
          transfronterizo de datos conforme a la Ley 29733; Google mantiene
          certificaciones y medidas de seguridad reconocidas internacionalmente
          (más información en{' '}
          <a href="https://firebase.google.com/support/privacy" target="_blank" rel="noopener">
            firebase.google.com/support/privacy
          </a>).
        </p>
        <p>
          La información viaja cifrada (TLS) y se guarda con controles de acceso: las
          reglas de seguridad de la base de datos garantizan que cada comercio solo
          puede leer sus propios datos.
        </p>

        <h2>6. Con quién compartimos los datos</h2>
        <p><strong>Con nadie.</strong> En concreto:</p>
        <ul>
          <li>No vendemos, alquilamos ni cedemos tus datos a terceros.</li>
          <li>Los pagos de un comercio solo son visibles para los miembros de ese mismo comercio (el dueño y los trabajadores que él autorice).</li>
          <li>Nuestros proveedores tecnológicos (Google/Firebase) procesan los datos únicamente por encargo nuestro y para prestar el servicio.</li>
          <li>Solo entregaríamos información a autoridades competentes ante una obligación legal (por ejemplo, un mandato judicial).</li>
        </ul>

        <h2>7. Cuánto tiempo conservamos los datos</h2>
        <ul>
          <li><strong>Datos de cuenta y comercio:</strong> mientras tu cuenta esté activa.</li>
          <li><strong>Historial de pagos:</strong> mientras tu cuenta esté activa, según el alcance de tu plan.</li>
          <li><strong>Al eliminar tu cuenta:</strong> borramos tus datos de los sistemas activos en un plazo máximo de <strong>30 días</strong>, y de las copias de seguridad en un plazo máximo de <strong>90 días</strong>. Podemos conservar únicamente lo estrictamente necesario para cumplir obligaciones legales, contables o de facturación, por el plazo que la ley exija.</li>
          <li><strong>Registros técnicos y de errores:</strong> hasta 12 meses.</li>
        </ul>

        <h2>8. Tus derechos (Ley 29733)</h2>
        <p>
          Como titular de datos personales, la ley peruana te reconoce los derechos de{' '}
          <strong>acceso, rectificación, cancelación y oposición</strong> (derechos ARCO),
          además de la revocación del consentimiento y el derecho a la portabilidad
          cuando corresponda:
        </p>
        <ul>
          <li><strong>Acceso:</strong> saber qué datos tuyos tenemos y cómo los tratamos.</li>
          <li><strong>Rectificación:</strong> corregir datos inexactos o incompletos.</li>
          <li><strong>Cancelación:</strong> pedir la eliminación de tus datos (ver <a href="/eliminar-datos/">Eliminar mi cuenta y mis datos</a>).</li>
          <li><strong>Oposición:</strong> oponerte a un tratamiento concreto por motivos legítimos.</li>
        </ul>
        <p>
          Para ejercerlos, escribe a <a href="mailto:pimentel@inklop.com">pimentel@inklop.com</a>{' '}
          desde el correo asociado a tu cuenta, indicando qué derecho quieres ejercer.
          Responderemos dentro de los plazos que fija la normativa. Si consideras que tu
          solicitud no fue atendida, puedes presentar una reclamación ante la{' '}
          <strong>Autoridad Nacional de Protección de Datos Personales</strong>{' '}
          (Ministerio de Justicia y Derechos Humanos del Perú).
        </p>
        <p>
          <strong>Nota sobre los pagadores:</strong> si eres cliente de un comercio que usa
          PagoYa y tu nombre aparece en su registro de pagos (porque tu billetera lo
          incluyó en la notificación de pago), también puedes ejercer estos derechos
          escribiéndonos al mismo correo.
        </p>

        <h2>9. Menores de edad</h2>
        <p>
          PagoYa es una herramienta para comercios y no está dirigida a menores de
          edad. No recolectamos conscientemente datos de menores. Si detectamos una
          cuenta creada por un menor sin autorización de sus padres o tutores, la
          eliminaremos junto con sus datos.
        </p>

        <h2>10. Seguridad</h2>
        <ul>
          <li>Cifrado de datos en tránsito (TLS) y en reposo en la infraestructura de Google Cloud.</li>
          <li>Reglas de acceso por comercio: nadie fuera de tu negocio puede ver tus pagos.</li>
          <li>Minimización: solo extraemos de cada notificación los datos indispensables, y el nombre del pagador no se anuncia por voz por defecto.</li>
          <li>Acceso interno restringido: solo el personal indispensable de PagoYa puede acceder a datos, y únicamente para soporte o mantenimiento.</li>
        </ul>

        <h2>11. Cambios a esta política</h2>
        <p>
          Si modificamos esta política, publicaremos la nueva versión en{' '}
          <a href="https://pagoya.pe/privacidad/">pagoya.pe/privacidad</a> con su nueva
          fecha de vigencia. Si el cambio es significativo (por ejemplo, un nuevo uso de
          los datos), te lo avisaremos además dentro de la App antes de que entre en
          vigor.
        </p>

        <h2>12. Contacto</h2>
        <p>
          Para cualquier consulta sobre privacidad o protección de datos:
          <br />
          Correo: <a href="mailto:pimentel@inklop.com">pimentel@inklop.com</a>
          <br />
          Web: <a href="https://pagoya.pe/">pagoya.pe</a>
          <br />
          Aplicación: PagoYa (<code>pe.pagoya.app</code>) en Google Play.
        </p>
      </main>
      <Pie compacto />
    </>
  );
}
