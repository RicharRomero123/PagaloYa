// Catálogo único de guías. De aquí comen el hub /guias/, el pie de página,
// las "guías relacionadas" y el sitemap: agregar una guía nueva es agregar una
// entrada aquí + su carpeta en app/. Nunca se enlaza una guía a mano.

export const GUIAS = [
  {
    ruta: '/yape-falso/',
    titulo: 'Yape falso: cómo saber si de verdad te pagaron',
    corto: 'Cómo detectar un Yape falso',
    descripcion:
      'Cómo funciona la estafa del yape falso en bodegas y mercados del Perú, las 7 señales que delatan una captura editada y la única forma segura de confirmar un pago: escucharlo.',
    gancho: 'Las 7 señales de una captura editada y cómo confirmar un pago en 2 segundos.',
    publicado: '2026-07-31',
  },
  {
    ruta: '/parlante-para-yape/',
    titulo: 'Parlante para Yape: opciones y precios en Perú',
    corto: 'Parlante para Yape: qué opciones hay',
    descripcion:
      'QR parlante, altavoz de mostrador o tu propio celular: cuánto cuesta cada opción para escuchar tus pagos Yape en Perú, qué te amarra cada una y cuál te conviene según tu negocio.',
    gancho: 'Cuánto cuesta cada opción y cuál te amarra a un procesador de pagos.',
    publicado: '2026-07-31',
  },
  {
    ruta: '/yape-comision-negocios/',
    titulo: '¿Yape cobra comisión a los negocios?',
    corto: '¿Yape cobra comisión a los negocios?',
    descripcion:
      'Qué cobra Yape a un negocio y qué no: cuándo se paga la comisión de Yape Empresa, desde qué monto te obliga a pasar al perfil de negocio, si necesitas RUC y qué opciones tienes si todavía no estás formalizado.',
    gancho: 'Cuándo se paga, desde qué monto y qué pasa si no tienes RUC.',
    publicado: '2026-08-03',
  },
  {
    ruta: '/no-me-llegan-notificaciones-yape/',
    titulo: 'No me llegan las notificaciones de Yape: cómo arreglarlo',
    corto: 'No me llegan las notificaciones de Yape',
    descripcion:
      'Guía paso a paso para que las notificaciones de Yape vuelvan a llegarte al celular: permisos, ahorro de batería en Xiaomi, Samsung, Huawei y Oppo, y modo No molestar.',
    gancho: 'Paso a paso por marca de celular: Xiaomi, Samsung, Huawei y Oppo.',
    publicado: '2026-07-31',
  },
];

/** Devuelve las demás guías, para el bloque de relacionadas. */
export function otrasGuias(rutaActual) {
  return GUIAS.filter((g) => g.ruta !== rutaActual);
}
