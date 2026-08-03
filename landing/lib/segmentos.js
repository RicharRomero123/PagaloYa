// Catálogo de páginas por segmento (ver MERCADO.md). Mismo patrón que
// lib/guias.js: de aquí comen el pie, las tarjetas de "¿Para quién es?" de la
// home y el sitemap. Agregar un segmento nuevo es agregar una entrada aquí
// + su carpeta en app/.
//
// `tarjeta` enlaza la entrada con la tarjeta correspondiente de #para-quien en
// la home: si un segmento aún no tiene página, su tarjeta simplemente no lleva
// enlace. Así se pueden ir publicando de a uno sin tocar la home.

export const SEGMENTOS = [
  {
    ruta: '/para-delivery/',
    tarjeta: 'delivery',
    titulo: 'Delivery: confirma el pago sin tener la cuenta',
    corto: 'Para delivery y motorizados',
    descripcion:
      'Cómo hace hoy un motorizado para confirmar un pago con Yape (captura y WhatsApp al jefe), por qué falla y cómo lograr que escuche el pago del cliente al instante sin tener la cuenta del negocio.',
    gancho: 'Se acabó la captura por WhatsApp: el motorizado escucha el pago al toque.',
    publicado: '2026-08-03',
  },
];

/** Segmento que corresponde a una tarjeta de #para-quien, si ya tiene página. */
export function segmentoDeTarjeta(tarjeta) {
  return SEGMENTOS.find((s) => s.tarjeta === tarjeta) || null;
}
