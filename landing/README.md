# Landing de PagoYa — pagoya.pe

Sitio en **Next.js 15 + Tailwind CSS v4**, con **exportación 100% estática**
(`output: 'export'`): el build genera HTML/CSS/JS puros en `out/` y se sirve
desde cualquier hosting estático (Firebase Hosting incluido). No hay servidor.

Diseño: "El mercado que habla" — vernáculo de bodega/mercado peruano (toldo
rayado, letrero corredizo, pizarra de precios, tickets con muescas, anotaciones
manuscritas). Tipografías Baloo 2 + Caveat autoalojadas vía `next/font`.

## Estructura

```
landing/
├── app/
│   ├── layout.jsx               ← metadata global, JSON-LD del sitio, fuentes
│   ├── globals.css              ← Tailwind v4 + tokens de marca + CSS artesanal
│   ├── page.jsx                 ← página principal
│   ├── guias/page.jsx           ← índice de guías (hub de enlazado interno)
│   ├── yape-falso/page.jsx                      ← guía SEO
│   ├── parlante-para-yape/page.jsx              ← guía SEO
│   ├── no-me-llegan-notificaciones-yape/page.jsx ← guía SEO
│   ├── para-delivery/page.jsx   ← página de segmento (ver MERCADO.md)
│   ├── privacidad/page.jsx      ← política de privacidad (URL para Play Console)
│   └── eliminar-datos/page.jsx  ← solicitud de eliminación de cuenta/datos
├── components/
│   ├── Cabecera.jsx / Pie.jsx / WspFlotante.jsx / Iconos.jsx
│   ├── Guia.jsx                 ← migas, caja de CTA y relacionadas de las guías
│   ├── DemoVoz.jsx              ← botón "Escúchalo" (TTS del navegador)
│   └── Efectos.jsx              ← reveal al scroll + evento WhatsApp (Plausible)
├── lib/
│   ├── enlaces.js               ← número de WhatsApp centralizado (ÚNICO lugar)
│   ├── seo.js                   ← dominio, metadata, JSON-LD (ÚNICO lugar)
│   ├── guias.js                 ← catálogo de guías (pie, home, hub y relacionadas)
│   └── segmentos.js             ← catálogo de páginas por segmento (pie y #para-quien)
├── public/
│   ├── assets/                  ← marca oficial: favicons (favicon-32, icon-192/512,
│   │                              apple-touch-icon), icono-96, wordmark-pagoya,
│   │                              mascota-pagoya y og.png (generado con esos assets)
│   ├── robots.txt / sitemap.xml / site.webmanifest
├── next.config.mjs              ← output: 'export', trailingSlash
├── postcss.config.mjs           ← Tailwind v4
├── firebase.json                ← hosting apunta a out/
├── SEO.md                       ← estado del SEO y plan de posicionamiento
└── package.json
```

## Agregar una guía nueva

1. Añade la entrada en `lib/guias.js` (ruta con `/` final).
2. Crea `app/<ruta>/page.jsx` copiando la estructura de una guía existente.
3. Agrega la URL en `public/sitemap.xml`.
4. Reenvía el sitemap en Google Search Console.

El enlazado interno (pie, bloque de la home, relacionadas) se actualiza solo
desde `lib/guias.js`.

## Agregar una página de segmento

Igual que una guía, pero el catálogo es `lib/segmentos.js` y cada entrada lleva
un campo `tarjeta` (`delivery`, `taxi`, `combi`…) que la enlaza con su tarjeta de
la sección `#para-quien` de la home. Mientras un segmento no tenga página, su
tarjeta simplemente no muestra enlace: no hay que tocar `app/page.jsx`.

## Comandos

```bash
cd landing
npm install        # una sola vez
npm run dev        # desarrollo en http://localhost:3000
npm run build      # exporta el sitio estático a out/
npm run servir     # sirve out/ para probar el build
```

## Desplegar (Firebase Hosting)

```bash
cd landing
npm run build
firebase login
firebase use <id-del-proyecto>   # una sola vez
firebase deploy --only hosting   # firebase.json ya apunta a out/
```

En cualquier otro hosting estático (Netlify, Cloudflare Pages) basta con
publicar la carpeta `out/`.

## Tokens Tailwind disponibles

Definidos en `app/globals.css` con `@theme`: `bg-naranja`, `bg-azul`,
`bg-azul-noche`, `bg-crema`, `bg-amarillo`, `text-naranja-osc`,
`font-display` (Baloo 2), `font-mano` (Caveat), etc. Úsalos al crear
componentes nuevos para respetar la marca.

## TODOs / placeholders pendientes

| Pendiente | Dónde | Cómo |
|---|---|---|
| **Número de WhatsApp real** 🔴 | `lib/enlaces.js` | Cambiar `NUMERO_WSP = '51999999999'` por el real (formato `51XXXXXXXXX`). Es el único lugar. **Hasta que se haga, ningún botón del sitio funciona.** |
| **Verificación de Search Console** 🔴 | `app/layout.jsx` (`verification`) | Descomentar con el código de Google. Sin esto no se puede pedir indexación manual. Ver `SEO.md`. |
| **Analytics** | `app/layout.jsx` (head) | Descomentar el snippet de Plausible (o cambiar por GA4). Los eventos `WhatsApp` (prop `donde`) y `DemoVoz` ya se disparan solos. |
| **Dominio** 🟡 | metadata/sitemap/robots usan `https://pagalo-ya.vercel.app` (provisional, en Vercel) | Cuando `pagoya.pe` esté comprado y apuntando, reemplazar en `lib/seo.js` (constante `SITIO`), `public/sitemap.xml` y `public/robots.txt`. Está marcado con `TODO(dominio)`. |
| **Redes sociales** | `components/Pie.jsx` | Enlaces a `@pagoya.pe` en TikTok/Facebook/Instagram; verificar URLs finales. |
| ~~Favicon/OG definitivos~~ | `public/assets/` | ✅ Listo: assets oficiales (ícono, wordmark y mascota) integrados; fuentes en la raíz del repo (`icono-pagoya.png`, `Untitled design (6).png`, `mascota-pagalo-ya.svg`). |
| **Enlace a Google Play** | `app/page.jsx` (paso 1 y CTA) | Agregar el badge cuando la app esté publicada. |

## Notas para Google Play Console

- **URL de política de privacidad:** `https://pagalo-ya.vercel.app/privacidad/` (provisional; será `https://pagoya.pe/privacidad/` cuando el dominio apunte)
- **URL de eliminación de datos:** `https://pagalo-ya.vercel.app/eliminar-datos/` (provisional; será `https://pagoya.pe/eliminar-datos/`)
- La política cubre: acceso a notificaciones (`BIND_NOTIFICATION_LISTENER_SERVICE`),
  Google Sign-In, Firebase/Google Cloud, no compartición con terceros, retención
  y eliminación, derechos ARCO (Ley 29733), menores, cambios y contacto
  (pimentel@inklop.com). Paquete: `pe.pagoya.app`. Vigencia: 30/07/2026.

## Reglas de marca aplicadas

- Naranja `#FF6B1A` + azul noche `#1A2B4A` + amarillo acento; cero morado,
  cero logo de Yape. "Compatible con Yape" solo como uso descriptivo;
  disclaimer de independencia en el footer.
- Sin promesas absolutas ("elimina el fraude 100%"): el mensaje es
  "si no suena, no te pagaron".
