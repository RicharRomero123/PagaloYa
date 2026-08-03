import { IconoLogo } from './Iconos';
import { wsp } from '../lib/enlaces';
import { GUIAS } from '../lib/guias';
import { SEGMENTOS } from '../lib/segmentos';

/** Footer completo (home) o compacto (páginas legales). */
export default function Pie({ compacto = false }) {
  if (compacto) {
    return (
      <footer className="pie">
        <div className="wrap pie-legal pie-legal--sola">
          <p>
            © 2026 PagoYa · Hecho en Perú · <a href="/">Inicio</a> ·{' '}
            <a href="/ayuda/">Ayuda</a> ·{' '}
            <a href="/preguntas-frecuentes/">Preguntas frecuentes</a> ·{' '}
            <a href="/privacidad/">Política de privacidad</a> ·{' '}
            <a href="/terminos/">Términos</a> ·{' '}
            <a href="/eliminar-datos/">Eliminar mis datos</a>
          </p>
        </div>
      </footer>
    );
  }

  return (
    <footer className="pie">
      <div className="wrap pie-grid">
        <div>
          <a className="logo" href="/" aria-label="PagoYa, inicio">
            <IconoLogo ancho={30} />
            Pago<b>Ya</b>
          </a>
          <p>
            Tu caja habla. Tus pagos suenan.
            <br />
            Si no suena, no te pagaron.
          </p>
        </div>
        {/* Enlazar las guías desde todas las páginas es lo que hace que Google
            las descubra y las considere parte del sitio, no páginas sueltas. */}
        <div>
          <h3>Guías y casos</h3>
          <ul>
            {GUIAS.map((g) => (
              <li key={g.ruta}><a href={g.ruta}>{g.corto}</a></li>
            ))}
            {SEGMENTOS.map((s) => (
              <li key={s.ruta}><a href={s.ruta}>{s.corto}</a></li>
            ))}
            <li><a href="/guias/">Todas las guías</a></li>
          </ul>
        </div>
        <div>
          <h3>Ayuda</h3>
          <ul>
            <li><a href="/ayuda/">Centro de ayuda</a></li>
            <li><a href="/preguntas-frecuentes/">Preguntas frecuentes</a></li>
            <li><a href="/consultas/">Consultas</a></li>
          </ul>
        </div>
        <div>
          <h3>Legal</h3>
          <ul>
            <li><a href="/privacidad/">Política de privacidad</a></li>
            <li><a href="/terminos/">Términos y condiciones</a></li>
            <li><a href="/eliminar-datos/">Eliminar mi cuenta y mis datos</a></li>
          </ul>
        </div>
        <div>
          <h3>Contacto</h3>
          <ul>
            <li>
              <a className="js-wsp" data-donde="pie" href={wsp('Hola PagoYa')} target="_blank" rel="noopener">
                WhatsApp
              </a>
            </li>
            <li><a href="mailto:pimentel@inklop.com">pimentel@inklop.com</a></li>
            <li><a href="https://www.tiktok.com/@pagoya.pe" target="_blank" rel="noopener">TikTok @pagoya.pe</a></li>
            <li><a href="https://www.facebook.com/pagoya.pe" target="_blank" rel="noopener">Facebook</a></li>
            <li><a href="https://www.instagram.com/pagoya.pe" target="_blank" rel="noopener">Instagram</a></li>
          </ul>
        </div>
      </div>
      <div className="wrap pie-legal">
        <p>
          PagoYa es un producto independiente. No pertenece a Yape, al BCP, a Plin, a
          Izipay ni a ningún banco o procesador, y no está afiliado ni respaldado por
          ellos. Las marcas mencionadas pertenecen a sus respectivos titulares y se
          citan solo para indicar compatibilidad o comparación. PagoYa anuncia las
          notificaciones reales que llegan a tu celular; siempre verifica tus
          movimientos en tu propia app.
        </p>
        <p>© 2026 PagoYa · Hecho en Perú</p>
      </div>
    </footer>
  );
}
