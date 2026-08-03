import { Baloo_2, Caveat } from 'next/font/google';
import './globals.css';
import Efectos from '../components/Efectos';
import { SITIO, NOMBRE, ORGANIZACION, SITIO_WEB, ld } from '../lib/seo';

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
  metadataBase: new URL(SITIO),
  // Google corta el título alrededor de los 60 caracteres: el de la home cabe
  // entero y las páginas hijas heredan el sufijo por `template`.
  title: {
    default: 'PagoYa: anuncia tus pagos Yape y Plin en voz alta',
    template: `%s · ${NOMBRE}`,
  },
  description:
    'App peruana que dice en voz alta cada Yape o Plin que te cae, en tu tienda y en tu celular aunque no estés. Contra el yape falso: si no suena, no te pagaron. Funciona con tu Yape de siempre, sin comprar un QR parlante. Gratis para empezar.',
  applicationName: NOMBRE,
  category: 'finance',
  authors: [{ name: NOMBRE, url: SITIO }],
  creator: NOMBRE,
  publisher: NOMBRE,
  alternates: { canonical: '/' },
  manifest: '/site.webmanifest',
  icons: {
    icon: [
      { url: '/assets/favicon-32.png', sizes: '32x32', type: 'image/png' },
      { url: '/assets/icon-192.png', sizes: '192x192', type: 'image/png' },
    ],
    apple: '/assets/apple-touch-icon.png',
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-snippet': -1,
      'max-image-preview': 'large',
      'max-video-preview': -1,
    },
  },
  openGraph: {
    type: 'website',
    url: `${SITIO}/`,
    siteName: NOMBRE,
    title: 'PagoYa — Tu caja habla. Tus pagos suenan.',
    description:
      'Anunciador de voz de pagos Yape y Plin para tu negocio. Si no suena, no te pagaron.',
    images: [
      {
        url: '/assets/og.png',
        width: 1200,
        height: 630,
        alt: 'PagoYa — Tu caja habla. Tus pagos suenan.',
      },
    ],
    locale: 'es_PE',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'PagoYa — Tu caja habla. Tus pagos suenan.',
    description: 'Anunciador de voz de pagos Yape y Plin para tu negocio. Si no suena, no te pagaron.',
    images: ['/assets/og.png'],
  },
  // TODO(seo): pegar aquí el código de verificación de Google Search Console
  // (Configuración → Propiedad → Etiqueta HTML) y volver a desplegar. Sin esto
  // no se puede pedir indexación manual ni ver por qué consultas apareces.
  // verification: { google: 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX' },
};

export const viewport = {
  themeColor: '#FF6B1A',
  colorScheme: 'light',
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
        {/* Identidad del sitio para Google: se declara una sola vez, aquí, y
            las páginas solo añaden su Article/FAQ/migas. */}
        <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(ORGANIZACION) }} />
        <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: ld(SITIO_WEB) }} />
        {/* TODO(analytics): descomentar cuando exista la cuenta de Plausible (o cambiar por GA4).
            Los eventos "WhatsApp" y "DemoVoz" ya se disparan solos desde los componentes.
            OJO: `data-domain` debe ser el dominio donde corre el sitio de verdad. Hoy es
            pagalo-ya.vercel.app; si se deja pagoya.pe antes de migrar, no registra nada. */}
        {/* <script defer data-domain="pagalo-ya.vercel.app" src="https://plausible.io/js/script.js"></script> */}
      </head>
      <body>
        {children}
        <Efectos />
      </body>
    </html>
  );
}
