const LOCAL = "es-PE";

export function soles(monto: number): string {
  return new Intl.NumberFormat(LOCAL, {
    style: "currency",
    currency: "PEN",
    minimumFractionDigits: 2,
  }).format(monto);
}

export function fechaCorta(ms: number): string {
  if (!ms) return "—";
  return new Intl.DateTimeFormat(LOCAL, {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date(ms));
}

/** "en 12 días" / "hace 3 días" / "hoy" — se lee más rápido que una fecha. */
export function haceOEn(ms: number, ahora = Date.now()): string {
  if (!ms) return "—";
  const dias = Math.round((ms - ahora) / 86_400_000);
  if (dias === 0) return "hoy";
  if (dias === 1) return "mañana";
  if (dias === -1) return "ayer";
  return dias > 0 ? `en ${dias} días` : `hace ${Math.abs(dias)} días`;
}
