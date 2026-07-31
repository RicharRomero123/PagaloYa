// TODO(whatsapp): reemplazar por el número real (formato 51XXXXXXXXX).
// Este es el ÚNICO lugar donde se define el número.
export const NUMERO_WSP = '51999999999';

/** Arma un enlace wa.me con mensaje precargado. */
export function wsp(mensaje) {
  return `https://wa.me/${NUMERO_WSP}?text=${encodeURIComponent(mensaje)}`;
}

export const MENSAJE_GENERAL = 'Hola PagoYa, quiero probar la app en mi negocio';
