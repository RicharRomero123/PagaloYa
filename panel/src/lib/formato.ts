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

/** "hace 5 min" / "hace 2 h" / "hace 3 días" — para latidos y cosas frescas. */
export function haceRato(ms: number, ahora = Date.now()): string {
  if (!ms) return "nunca";
  const min = Math.round((ahora - ms) / 60_000);
  if (min < 1) return "ahorita";
  if (min < 60) return `hace ${min} min`;
  const horas = Math.round(min / 60);
  if (horas < 24) return `hace ${horas} h`;
  return `hace ${Math.round(horas / 24)} días`;
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
