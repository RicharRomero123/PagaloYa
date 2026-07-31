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
│   ├── layout.jsx               ← metadata global, fuentes, efectos
│   ├── globals.css              ← Tailwind v4 + tokens de marca + CSS artesanal
│   ├── page.jsx                 ← página principal
│   ├── privacidad/page.jsx      ← política de privacidad (URL para Play Console)
│   └── eliminar-datos/page.jsx  ← solicitud de eliminación de cuenta/datos
├── components/
│   ├── Cabecera.jsx / Pie.jsx / WspFlotante.jsx / Iconos.jsx
│   ├── DemoVoz.jsx              ← botón "Escúchalo" (TTS del navegador)
│   └── Efectos.jsx              ← reveal al scroll + evento WhatsApp (Plausible)
├── lib/enlaces.js               ← número de WhatsApp centralizado (ÚNICO lugar)
├── public/
│   ├── assets/ (favicon.svg, og.png)
│   ├── robots.txt / sitemap.xml
├── next.config.mjs              ← output: 'export', trailingSlash
├── postcss.config.mjs           ← Tailwind v4
├── firebase.json                ← hosting apunta a out/
└── package.json
```

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
| **Número de WhatsApp real** | `lib/enlaces.js` | Cambiar `NUMERO_WSP = '51999999999'` por el real (formato `51XXXXXXXXX`). Es el único lugar. |
| **Analytics** | `app/layout.jsx` (head) | Descomentar el snippet de Plausible (o cambiar por GA4). Los eventos `WhatsApp` (prop `donde`) y `DemoVoz` ya se disparan solos. |
| **Dominio** | metadata/sitemap/robots asumen `https://pagoya.pe` | Si cambia, reemplazar en `app/layout.jsx`, `public/sitemap.xml` y `public/robots.txt`. |
| **Redes sociales** | `components/Pie.jsx` | Enlaces a `@pagoya.pe` en TikTok/Facebook/Instagram; verificar URLs finales. |
| **Favicon/OG definitivos** | `public/assets/` | Placeholders con la marca; reemplazar cuando exista el logo oficial. |
| **Enlace a Google Play** | `app/page.jsx` (paso 1 y CTA) | Agregar el badge cuando la app esté publicada. |

## Notas para Google Play Console

- **URL de política de privacidad:** `https://pagoya.pe/privacidad/`
- **URL de eliminación de datos:** `https://pagoya.pe/eliminar-datos/`
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
