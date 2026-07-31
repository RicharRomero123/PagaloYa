# SEO de pagoya.pe — estado y plan

> Última revisión: 31 de julio de 2026.

## Expectativa realista (léelo antes que nada)

El SEO orgánico **no** pone un dominio nuevo en el primer puesto de términos
competidos en un mes. Google necesita ver el sitio, indexarlo, y luego meses de
señales (enlaces, clics, permanencia) antes de darle autoridad. Lo normal para un
dominio recién nacido es **3 a 6 meses** para consultas con competencia real.

Lo que **sí** se consigue en 30 días, y es lo que persigue este plan:

| Plazo | Meta alcanzable |
|---|---|
| Días 1–3 | Sitio indexado; `site:pagoya.pe` devuelve las 7 páginas |
| Días 3–7 | Puesto 1 por la marca: «PagoYa», «PagoYa app», «pagoya.pe» |
| Días 7–30 | Top 10 en *long-tail* de baja competencia (las 3 guías) |
| Día 1 (pagando) | Arriba de todo vía Google Ads en los términos comerciales |
| Mes 3–6 | Pelear el top 3 de «yape falso», «parlante yape» |

**Para estar arriba ya, la única palanca es pagada.** El SEO de este repo es la
inversión que hace que en el mes 4 dejes de pagar por cada clic.

---

## Mapa de palabras clave

Una intención por página. Nunca dos páginas peleando la misma consulta
(canibalización): es el error que más frena a los sitios nuevos.

| Página | Consulta principal | Intención | Competencia |
|---|---|---|---|
| `/` | anunciador de pagos yape, app que anuncia yape | comercial | media |
| `/yape-falso/` | yape falso, cómo saber si un yape es falso, app de yape falso | informativa | media-alta, mucho volumen |
| `/parlante-para-yape/` | parlante para yape, qr parlante precio, altavoz yape | comercial | media |
| `/no-me-llegan-notificaciones-yape/` | no me llegan las notificaciones de yape, yape no me avisa | soporte | **baja — la victoria más rápida** |
| `/guias/` | — (hub de enlazado interno) | navegación | — |

La tercera guía es la apuesta de corto plazo: mucha gente la busca, casi nadie la
responde bien, y quien la busca es exactamente el usuario de PagoYa.

---

## Lo que ya está resuelto en el código

- Títulos dentro del límite de ~60 caracteres, con `template` heredado.
- `description` única por página, canónica en todas, `metadataBase` centralizado.
- Open Graph completo (con dimensiones de imagen) y Twitter Card.
- Datos estructurados: `Organization` + `WebSite` (global), `SoftwareApplication`
  con los tres planes, `FAQPage` en la home y en cada guía, `Article` y
  `BreadcrumbList` en las guías.
- Directivas `robots` explícitas con `max-image-preview: large`.
- `sitemap.xml` con las 7 URLs y `robots.txt` apuntando a él.
- `site.webmanifest` y `theme-color`.
- Enlazado interno: cabecera, pie y bloque en la home apuntan a las guías; las
  guías se enlazan entre sí y de vuelta a la home.
- H1 de la home con la palabra clave («Cuando te yapean…»).
- Exportación estática: sin servidor, tiempos de carga mínimos en 3G.

## Lo que falta y solo puedes hacer tú

Ordenado por impacto. Los tres primeros son **bloqueantes**.

### 1. Número de WhatsApp real 🔴 BLOQUEANTE
`lib/enlaces.js` sigue con `51999999999`. **Cada botón de la página lleva a un
número inexistente.** Ahora mismo el sitio no puede convertir ni una visita.

### 2. Google Search Console 🔴 BLOQUEANTE para indexar rápido
1. Da de alta `pagoya.pe` en [search.google.com/search-console](https://search.google.com/search-console).
2. Verifica por DNS (recomendado) o por etiqueta HTML: si eliges la etiqueta,
   descomenta `verification.google` en `app/layout.jsx` y despliega.
3. **Sitemaps → Enviar** → `sitemap.xml`.
4. **Inspección de URLs** → pega cada una de las 7 URLs → *Solicitar indexación*.
   Esto es lo que baja el tiempo de indexación de semanas a días.

### 3. Analytics
Descomenta el script de Plausible en `app/layout.jsx` (o cámbialo por GA4). Los
eventos `WhatsApp` (con la prop `donde`) y `DemoVoz` ya se disparan solos. Sin
medición no sabes qué guía trae clientes y estarías escribiendo a ciegas.

### 4. Ficha de Google Business Profile
Gratis y es lo que más rápido te pone visible en Perú. Crea la ficha del negocio,
categoría «Empresa de software» o «Servicio de asistencia informática», con
enlace a pagoya.pe. Aparece en el mapa y en las búsquedas locales en días, no en
meses.

### 5. Google Ads (la vía rápida de verdad)
Presupuesto de arranque S/ 15–25/día, segmentación Perú, idioma español.
- Grupo 1 → `/parlante-para-yape/`: «parlante para yape», «qr parlante precio».
- Grupo 2 → `/yape-falso/`: «yape falso», «como saber si un yape es falso».
- Grupo 3 → `/`: «app anunciar pagos yape», «anunciador de pagos».
Negativas obligatorias: «gratis apk», «hackear», «generador».
Las landings ya existen y son relevantes: eso baja el costo por clic.

### 6. Enlaces entrantes (lo que decide el mes 3 en adelante)
Sin enlaces no hay autoridad, y sin autoridad no hay top 3. Las vías realistas:
- Perfiles con enlace: TikTok, Facebook, Instagram (ya en el pie), LinkedIn.
- Ficha en Google Play cuando la app esté publicada → enlaza a pagoya.pe.
- Directorios de emprendimiento peruano y de apps fintech.
- Notas de prensa locales sobre el yape falso: es tema con interés periodístico.
- Grupos de bodegueros en Facebook y WhatsApp: compartir la guía de
  notificaciones (útil de verdad, no publicidad) genera enlaces y visitas.

### 7. Assets pendientes
- `public/assets/og.png` debe medir **1200 × 630 px**. Es la imagen que se ve al
  compartir en WhatsApp, y en Perú el sitio se comparte por WhatsApp.
- Favicon definitivo cuando exista el logo oficial.
- Badge de Google Play en la home cuando la app esté publicada.

---

## Ritmo de contenido sugerido

Una guía nueva cada 10–14 días. Cada una: entrada en `lib/guias.js`, carpeta en
`app/`, URL en `public/sitemap.xml`, y reenvío del sitemap en Search Console.
El enlazado interno (pie, home, relacionadas) se actualiza solo.

Próximas candidatas, por orden de facilidad para rankear:

1. «Cómo cobrar con Yape en mi negocio: guía para bodegas»
2. «Yape vs Plin para negocios: cuál conviene»
3. «Cómo cuadrar la caja de tu bodega al final del día»
4. «¿Yape cobra comisión a los negocios?»
5. «Trabajadores y caja: cómo controlar los cobros sin estar en la tienda»

## Cómo revisar que todo sigue bien

- **Datos estructurados:** [validator.schema.org](https://validator.schema.org/) y la
  Prueba de resultados enriquecidos de Google, una URL por vez.
- **Velocidad:** PageSpeed Insights en móvil. El objetivo es LCP < 2.5 s en 3G;
  el sitio es estático, así que si baja de ahí es por una imagen pesada.
- **Indexación:** busca `site:pagoya.pe` en Google. Deben salir las 7 páginas.
- **Enlaces rotos:** tras agregar una guía, comprueba que su ruta en
  `lib/guias.js` coincide exactamente con la carpeta en `app/` (con `/` final).
