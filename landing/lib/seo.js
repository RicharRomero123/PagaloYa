// Constantes y helpers de SEO. ÚNICO lugar donde vive el dominio: si algún día
// cambia pagoya.pe, se cambia aquí y en public/robots.txt + public/sitemap.xml.

export const SITIO = 'https://pagoya.pe';
export const NOMBRE = 'PagoYa';

/** URL absoluta a partir de una ruta ('/yape-falso/'). */
export function url(ruta = '/') {
  return `${SITIO}${ruta}`;
}

/**
 * Metadata base de una página de contenido. Centraliza canonical + Open Graph
 * para que ninguna página nueva se olvide de ellos (es el error clásico que
 * genera contenido duplicado y frena la indexación).
 */
export function meta({ titulo, descripcion, ruta, tipo = 'article' }) {
  return {
    title: titulo,
    description: descripcion,
    alternates: { canonical: ruta },
    openGraph: {
      type: tipo,
      url: url(ruta),
      title: titulo,
      description: descripcion,
      siteName: NOMBRE,
      locale: 'es_PE',
      images: [{ url: '/assets/og.png', width: 1200, height: 630, alt: 'PagoYa — Tu caja habla. Tus pagos suenan.' }],
    },
  };
}

/** Migas de pan en JSON-LD. Google las muestra en el resultado de búsqueda. */
export function migas(items) {
  return {
    '@context': 'https://schema.org',
    '@type': 'BreadcrumbList',
    itemListElement: items.map((it, i) => ({
      '@type': 'ListItem',
      position: i + 1,
      name: it.nombre,
      item: url(it.ruta),
    })),
  };
}

/** Artículo de guía en JSON-LD. */
export function articulo({ titulo, descripcion, ruta, publicado, modificado }) {
  return {
    '@context': 'https://schema.org',
    '@type': 'Article',
    headline: titulo,
    description: descripcion,
    inLanguage: 'es-PE',
    mainEntityOfPage: { '@type': 'WebPage', '@id': url(ruta) },
    image: url('/assets/og.png'),
    datePublished: publicado,
    dateModified: modificado || publicado,
    author: { '@type': 'Organization', name: NOMBRE, url: SITIO },
    publisher: { '@type': 'Organization', name: NOMBRE, url: SITIO },
  };
}

/** Bloque de preguntas frecuentes en JSON-LD, a partir de [{p, r}]. */
export function faq(preguntas) {
  return {
    '@context': 'https://schema.org',
    '@type': 'FAQPage',
    mainEntity: preguntas.map(({ p, r }) => ({
      '@type': 'Question',
      name: p,
      acceptedAnswer: { '@type': 'Answer', text: r },
    })),
  };
}

/** Organización + sitio: se inyecta una sola vez, en el layout raíz. */
export const ORGANIZACION = {
  '@context': 'https://schema.org',
  '@type': 'Organization',
  '@id': `${SITIO}/#organizacion`,
  name: NOMBRE,
  url: SITIO,
  logo: url('/assets/favicon.svg'),
  email: 'pimentel@inklop.com',
  areaServed: { '@type': 'Country', name: 'Perú' },
  slogan: 'Tu caja habla. Tus pagos suenan.',
  sameAs: [
    'https://www.tiktok.com/@pagoya.pe',
    'https://www.facebook.com/pagoya.pe',
    'https://www.instagram.com/pagoya.pe',
  ],
};

export const SITIO_WEB = {
  '@context': 'https://schema.org',
  '@type': 'WebSite',
  '@id': `${SITIO}/#sitio`,
  url: SITIO,
  name: NOMBRE,
  inLanguage: 'es-PE',
  publisher: { '@id': `${SITIO}/#organizacion` },
};

/** Serializa JSON-LD de forma segura dentro de <script>. */
export function ld(objeto) {
  return JSON.stringify(objeto).replace(/</g, '\\u003c');
}
