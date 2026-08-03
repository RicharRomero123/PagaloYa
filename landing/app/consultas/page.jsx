import Cabecera from '../../components/Cabecera';
import Pie from '../../components/Pie';
import WspFlotante from '../../components/WspFlotante';
import FormularioConsulta from './FormularioConsulta';
import { meta, migas, ld } from '../../lib/seo';
import { wsp } from '../../lib/enlaces';

export const metadata = meta({
  titulo: 'Consultas y contacto de PagoYa',
  descripcion:
    'Mándanos tu consulta sobre PagoYa. Escríbenos por WhatsApp o por correo y te respondemos rápido, en cristiano, para que tu negocio escuche cada Yape y Plin.',
  ruta: '/consultas/',
  tipo: 'website',
});

export default function Consultas() {
  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{
          __html: ld(
            migas([
              { nombre: 'Inicio', ruta: '/' },
              { nombre: 'Consultas', ruta: '/consultas/' },
            ]),
          ),
        }}
      />

      <Cabecera />
      <main className="legal articulo">
        <p className="miga"><a href="/">← Volver a pagoya.pe</a></p>

        <h1>Mándanos tu consulta</h1>
        <p className="vigencia">Te respondemos una persona de verdad, no un robot</p>

        <p>
          ¿Tienes una duda sobre PagoYa, quieres probarlo en tu negocio o algo no te
          está sonando? Escríbenos. Llena el formulario y con un toque se abre tu
          WhatsApp (o tu correo) con el mensaje ya redactado; solo le das enviar.
        </p>

        <div className="caja-resumen">
          <p><strong>Ojo, casero:</strong> este es un formulario simple. No manda tus
          datos a ningún servidor ni los guarda en internet. Lo único que hace es
          preparar el mensaje y abrirlo en el canal que elijas —WhatsApp o correo—
          para que lo envíes tú. Directo y sin vueltas.</p>
        </div>

        <FormularioConsulta />

        <h2>¿Prefieres escribir directo?</h2>
        <p>También puedes contactarnos sin llenar el formulario:</p>
        <ul>
          <li>
            {/* TODO(whatsapp): reemplazar por el número real. El enlace usa el número
                central de lib/enlaces.js; este texto es solo para mostrarlo. */}
            <strong>WhatsApp:</strong>{' '}
            <a className="js-wsp" data-donde="consultas-directo" href={wsp('Hola PagoYa, tengo una consulta: ')} target="_blank" rel="noopener">
              +51 9XX XXX XXX
            </a>
          </li>
          <li>
            <strong>Correo:</strong>{' '}
            <a href="mailto:pimentel@inklop.com?subject=Consulta%20PagoYa">pimentel@inklop.com</a>
          </li>
        </ul>

        <p>
          Antes de escribir, quizás tu duda ya esté resuelta en las{' '}
          <a href="/preguntas-frecuentes/">preguntas frecuentes</a> o en el{' '}
          <a href="/ayuda/">centro de ayuda</a>.
        </p>
      </main>
      <Pie />
      <WspFlotante />
    </>
  );
}
