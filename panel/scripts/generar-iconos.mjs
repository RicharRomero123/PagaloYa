/**
 * Generador de íconos PWA de PagoYa, sin dependencias (Node + zlib).
 *
 * No hay ImageMagick/sharp/canvas en el entorno, así que rasterizamos a mano:
 * cada píxel se decide por geometría (SDF) con supersampling para antialias, y
 * se codifica un PNG RGBA con zlib. Mark on-brand: lienzo azul #1A2B4A con
 * degradado + "ondas de sonido" naranjas (BRAND: si suena, te pagaron).
 *
 * Reproducible: `node scripts/generar-iconos.mjs` regenera public/*.png.
 */
import zlib from "node:zlib";
import { writeFileSync, mkdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const RAIZ = join(dirname(fileURLToPath(import.meta.url)), "..");
const OUT = join(RAIZ, "public");
mkdirSync(OUT, { recursive: true });

const hex = (h) => [
  parseInt(h.slice(1, 3), 16),
  parseInt(h.slice(3, 5), 16),
  parseInt(h.slice(5, 7), 16),
];
const mix = (a, b, t) => a.map((v, i) => v + (b[i] - v) * t);

// Paleta BRAND
const AZUL_TOP = hex("#223a63"); // azul con relieve arriba
const AZUL_BOT = hex("#14223c");
const AZUL_GLOW = hex("#2c4570");
const NARANJA = hex("#FF6B1A");
const NARANJA_MED = hex("#FF8A3D");
const NARANJA_CLARO = hex("#FFB070");

const DEG = Math.PI / 180;

// ── Geometría del mark, en coords normalizadas [0,1] ────────────────────────
const DOT = { x: 0.365, y: 0.5, r: 0.058 };
const ARCOS = [
  { r: 0.155, col: NARANJA },
  { r: 0.255, col: NARANJA_MED },
  { r: 0.355, col: NARANJA_CLARO },
];
const GROSOR = 0.05;
const APERTURA = 52 * DEG; // las ondas abren hacia la derecha

/** SDF caja redondeada centrada en (0.5,0.5), lado 1, radio de esquina rc. */
function dentroRoundRect(nx, ny, rc) {
  const qx = Math.abs(nx - 0.5) - (0.5 - rc);
  const qy = Math.abs(ny - 0.5) - (0.5 - rc);
  const fuera = Math.hypot(Math.max(qx, 0), Math.max(qy, 0));
  const dentro = Math.min(Math.max(qx, qy), 0);
  return fuera + dentro - rc <= 0;
}

/** Color del mark en (mx,my) o null si el píxel no pertenece al mark. */
function colorMark(mx, my) {
  const dx = mx - DOT.x;
  const dy = my - DOT.y;
  const dist = Math.hypot(dx, dy);
  if (dist <= DOT.r) return NARANJA;
  const ang = Math.atan2(dy, dx); // 0 = hacia la derecha
  if (Math.abs(ang) <= APERTURA) {
    for (const a of ARCOS) {
      if (dist >= a.r - GROSOR / 2 && dist <= a.r + GROSOR / 2) return a.col;
    }
  }
  return null;
}

/** Fondo azul con degradado vertical + brillo suave arriba-izquierda. */
function colorFondo(nx, ny) {
  let c = mix(AZUL_TOP, AZUL_BOT, Math.min(1, ny * 1.05));
  const dg = Math.hypot(nx - 0.32, ny - 0.24);
  const glow = Math.max(0, 1 - dg / 0.6);
  c = mix(c, AZUL_GLOW, glow * 0.35);
  return c;
}

/**
 * Renderiza un ícono size×size.
 *  - rc: radio de esquina normalizado (0 = cuadrado a sangre, para maskable/apple)
 *  - scale: escala del mark alrededor del centro (maskable/apple reducen a 0.8)
 *  - opaco: si true, fuera del lienzo se rellena azul (apple no admite alfa)
 */
function render(size, { rc, scale, opaco }) {
  const ss = size >= 400 ? 3 : 4; // supersampling
  const px = Buffer.alloc(size * size * 4);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      let r = 0, g = 0, b = 0, a = 0;
      for (let sy = 0; sy < ss; sy++) {
        for (let sx = 0; sx < ss; sx++) {
          const nx = (x + (sx + 0.5) / ss) / size;
          const ny = (y + (sy + 0.5) / ss) / size;
          let sr, sg, sb, sa;
          const enLienzo = rc <= 0 ? true : dentroRoundRect(nx, ny, rc);
          if (!enLienzo) {
            if (opaco) {
              [sr, sg, sb] = colorFondo(nx, ny);
              sa = 255;
            } else {
              sr = sg = sb = sa = 0;
            }
          } else {
            const mx = 0.5 + (nx - 0.5) / scale;
            const my = 0.5 + (ny - 0.5) / scale;
            const cm = colorMark(mx, my);
            if (cm) {
              [sr, sg, sb] = cm;
              sa = 255;
            } else {
              [sr, sg, sb] = colorFondo(nx, ny);
              sa = 255;
            }
          }
          r += sr; g += sg; b += sb; a += sa;
        }
      }
      const n = ss * ss;
      const i = (y * size + x) * 4;
      px[i] = Math.round(r / n);
      px[i + 1] = Math.round(g / n);
      px[i + 2] = Math.round(b / n);
      px[i + 3] = Math.round(a / n);
    }
  }
  return px;
}

// ── Codificador PNG (RGBA, filtro none) ─────────────────────────────────────
const CRC = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();
function crc32(buf) {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function chunk(tipo, datos) {
  const t = Buffer.from(tipo, "ascii");
  const len = Buffer.alloc(4);
  len.writeUInt32BE(datos.length, 0);
  const cuerpo = Buffer.concat([t, datos]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(cuerpo), 0);
  return Buffer.concat([len, cuerpo, crc]);
}
function png(size, px) {
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(size, 0);
  ihdr.writeUInt32BE(size, 4);
  ihdr[8] = 8; // bit depth
  ihdr[9] = 6; // color type RGBA
  const raw = Buffer.alloc((size * 4 + 1) * size);
  for (let y = 0; y < size; y++) {
    raw[y * (size * 4 + 1)] = 0;
    px.copy(raw, y * (size * 4 + 1) + 1, y * size * 4, (y + 1) * size * 4);
  }
  const idat = zlib.deflateSync(raw, { level: 9 });
  return Buffer.concat([
    sig,
    chunk("IHDR", ihdr),
    chunk("IDAT", idat),
    chunk("IEND", Buffer.alloc(0)),
  ]);
}

const salidas = [
  ["icon-192.png", 192, { rc: 0.22, scale: 1, opaco: false }],
  ["icon-512.png", 512, { rc: 0.22, scale: 1, opaco: false }],
  ["icon-maskable-512.png", 512, { rc: 0, scale: 0.78, opaco: true }],
  ["apple-touch-icon.png", 180, { rc: 0, scale: 0.8, opaco: true }],
  ["favicon-32.png", 32, { rc: 0.22, scale: 1, opaco: false }],
];
for (const [nombre, size, opts] of salidas) {
  writeFileSync(join(OUT, nombre), png(size, render(size, opts)));
  console.log(`✓ ${nombre} (${size}px)`);
}
console.log("Íconos generados en public/");
