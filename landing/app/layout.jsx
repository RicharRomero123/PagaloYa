import { Baloo_2, Caveat } from 'next/font/google';
import './globals.css';
import Efectos from '../components/Efectos';

const baloo = Baloo_2({
  subsets: ['latin'],
  weight: ['700', '800'],
  variable: '--font-baloo',
  display: 'swap',
});

const caveat = Caveat({
  subsets: ['latin'],
  weight: ['700'],
  variable: '--font-caveat',
  display: 'swap',
});

export const metadata = {
  metadataBase: new URL('https://pagoya.pe'),
  title: 'PagoYa — Anunciador de voz de pagos Yape y Plin | Si no suena, no te pagaron',
  description:
    'PagoYa dice en voz alta cada Yape o Plin que te cae: suena en tu tienda y en tu celular aunque no estés. Adiós al yape falso: si no suena, no te pagaron. Funciona con tu Yape de siempre, sin cambiar tu forma de cobrar ni comprar un QR parlante.',
  icons: { icon: '/assets/favicon.svg' },
  openGraph: {
    type: 'website',
    url: 'https://pagoya.pe/',
    title: 'PagoYa — Tu caja habla. Tus pagos suenan.',
    description: 'Anunciador de voz de pagos Yape y Plin para tu negocio. Si no suena, no te pagaron.',
    images: ['/assets/og.png'],
    locale: 'es_PE',
  },
  twitter: { card: 'summary_large_image' },
};

export default function RootLayout({ children }) {
  return (
    // suppressHydrationWarning: el script de abajo agrega la clase 'js' a <html>
    // antes de que React hidrate (adrede, para que las animaciones .reveal no
    // parpadeen). Es el mismo patrón que los scripts de tema claro/oscuro.
    <html lang="es-PE" suppressHydrationWarning className={`${baloo.variable} ${caveat.variable}`}>
      <head>
        {/* Marca 'js' antes de pintar para que las animaciones .reveal
            solo se activen cuando hay JavaScript (sin flash). */}
        <script
          dangerouslySetInnerHTML={{ __html: "document.documentElement.classList.add('js');" }}
        />
        {/* TODO(analytics): descomentar cuando exista la cuenta de Plausible (o cambiar por GA4).
            Los eventos "WhatsApp" y "DemoVoz" ya se disparan solos desde los componentes. */}
        {/* <script defer data-domain="pagoya.pe" src="https://plausible.io/js/script.js"></script> */}
      </head>
      <body>
        {children}
        <Efectos />
      </body>
    </html>
  );
}
