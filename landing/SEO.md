# SEO de PagoYa — estado y plan

> Última revisión: 3 de agosto de 2026.
> Dominio en producción: **`pagalo-ya.vercel.app`** (provisional).
> Dominio objetivo: **`pagoya.pe`** (pendiente de compra).

## 🔴 Decisión pendiente que condiciona todo lo demás

Hoy el sitio vive en un subdominio de Vercel. **Si pides indexación ahora y
después migras a `pagoya.pe`, haces el trabajo dos veces:** el histórico, los
enlaces conseguidos y la antigüedad quedan en el dominio viejo, y recuperarlos
exige redirecciones 301 y semanas de reprocesamiento de Google.

Además, un subdominio gratuito de una plataforma de hosting arranca con menos
confianza que un `.pe` propio, y para un producto financiero eso pesa.

**Recomendación: compra `pagoya.pe` antes de dar de alta Search Console.** Un
`.pe` cuesta del orden de S/ 100/año; el retraso de un mes en el arranque de SEO
cuesta más. Mientras tanto el sitio en Vercel sirve perfecto para las URLs que
pide Play Console y para enseñar el producto.

### Si igual se indexa primero en Vercel, la migración debe hacerse así

1. Comprar `pagoya.pe` y apuntarlo a Vercel como dominio principal.
2. Cambiar `SITIO` en `lib/seo.js`, y el dominio en `public/sitemap.xml` y
   `public/robots.txt` (los tres están marcados con `TODO(dominio)`).
3. Configurar en Vercel el subdominio viejo como **redirección 301 permanente**
   al nuevo, no como alias: si los dos responden 200, se duplica el sitio entero.
4. Dar de alta la propiedad nueva en Search Console y usar la
   **herramienta de cambio de dirección**.
5. Reenviar el sitemap nuevo y volver a pedir indexación de las 11 URLs.
6. Actualizar las URLs de privacidad y eliminación de datos en Play Console.

---

## Expectativa realista

El SEO orgánico **no** pone un dominio nuevo en el primer puesto de términos
competidos en un mes. Google necesita indexarlo y luego meses de señales
(enlaces, clics, permanencia). Lo normal para un dominio recién nacido son
**3 a 6 meses** en consultas con competencia real.

Lo alcanzable en 30 días, contados **desde que el dominio definitivo esté vivo**:

| Plazo | Meta alcanzable |
|---|---|
| Días 1–3 | Indexado; `site:pagoya.pe` devuelve las 11 páginas |
| Días 3–7 | Puesto 1 por la marca: «PagoYa», «PagoYa app» |
| Días 7–30 | Top 10 en *long-tail* de baja competencia (las 3 guías) |
| Día 1 (pagando) | Arriba de todo vía Google Ads en los términos comerciales |
| Mes 3–6 | Pelear el top 3 de «yape falso», «parlante yape» |

**Para estar arriba ya, la única palanca es pagada.** El SEO de este repo es lo
que hace que en el mes 4 dejes de pagar por cada clic.

---

## Mapa de URLs y palabras clave

Una intención por página. Que dos páginas peleen la misma consulta
(canibalización) es el error que más frena a los sitios nuevos.

| URL | Consulta principal | Intención | Competencia |
|---|---|---|---|
| `/` | anunciador de pagos yape, app que anuncia yape | comercial | media |
| `/yape-falso/` | yape falso, cómo saber si un yape es falso | informativa | media-alta, mucho volumen |
| `/parlante-para-yape/` | parlante para yape, qr parlante precio | comercial | media |
| `/yape-comision-negocios/` | yape cobra comisión negocios, yape empresa comisión, cobrar con yape sin ruc | **comercial, alta intención** | media |
| `/para-delivery/` | confirmar pago delivery, motorizado captura yape | comercial por segmento | muy baja |
| `/no-me-llegan-notificaciones-yape/` | no me llegan las notificaciones de yape | soporte | **baja — la victoria más rápida** |
| `/preguntas-frecuentes/` | qué es pagoya, pagoya es seguro, pagoya precio | marca + confianza | baja |
| `/guias/` | — (hub de enlazado interno) | navegación | — |
| `/ayuda/` | soporte pagoya, contacto pagoya | marca | baja |
| `/consultas/` | contacto | conversión | — |
| `/privacidad/`, `/terminos/`, `/eliminar-datos/` | — (exigidas por Play Console) | legal | — |

### El racimo de mayor intención: comisión y RUC

`/yape-comision-negocios/` es la página con más intención comercial de todo el
sitio. Quien busca «¿Yape cobra comisión?» o «cobrar con Yape sin RUC» **está
decidiendo en ese momento**, y la respuesta estructural de PagoYa (no tocamos el
dinero → no hay comisión ni RUC) es exactamente lo que necesita oír.

Reglas para esta página, porque publica datos de un tercero:

- **Responder primero, vender después.** La página rankea por resolver la duda
  con honestidad, no por promocionar. La parte de PagoYa va al final y dice
  explícitamente que no compite con Yape.
- **Nada sin verificar.** Solo se publica lo confirmado en fuentes públicas: el
  2.95 % del perfil Yape Empresa (vigente desde fines de abril de 2024) y el
  umbral de 5 UIT mensuales. El umbral se expresa **en UIT, nunca en soles**,
  porque la UIT se actualiza cada año.
- **Enlace a la fuente oficial** de Yape, con `nofollow`, y nota de vigencia con
  fecha al pie.
- **Revisar cada trimestre.** Si Yape cambia condiciones y la página queda
  desactualizada, deja de ser un activo y pasa a ser un riesgo.

Este mismo criterio aplica a la tarjeta «Yape Empresa» de la sección de
comparación de la home.

### ⚠️ Solapamiento a vigilar

La home y `/preguntas-frecuentes/` publican **las dos** un `FAQPage` con temas que
se pisan (seguridad, precios, compatibilidad, iPhone). No es grave, pero si en
Search Console ves que las dos URLs aparecen para la misma consulta y ninguna
sube, la salida es dejar el `FAQPage` **solo** en `/preguntas-frecuentes/` y
mantener en la home el acordeón visible sin su JSON-LD. La FAQ de la home debería
responder dudas de *compra*; la de `/preguntas-frecuentes/`, dudas de *uso*.

---

## 🚧 Lo que NO se publica todavía (y por qué)

`CRECIMIENTO.md` §2 define dos garantías. **Ninguna de las dos está en la landing,
a propósito**, y conviene que siga así hasta que el producto las respalde:

| Garantía | Estado | Cuándo se publica |
|---|---|---|
| **De Transparencia** — «PagoYa te avisa cuando no puede avisarte» | El **modo ciego** todavía no existe (`ESCALA.md` §2: es el hueco del 20 %) | En cuanto el modo ciego esté en producción. Es el mejor argumento de venta disponible y **nadie más lo ofrece** |
| **De Aviso** — «si no te avisamos, ese mes no lo pagas» | Requiere el Capturador | Etapa D. Con 80 % de precisión es una máquina de reembolsos |

Publicar una garantía que el producto no cumple es la forma más rápida de quemar
la marca — lo dice el propio `CRECIMIENTO.md` §9. Cuando el modo ciego salga, la
Garantía de Transparencia merece **sección propia en la home**, no una línea
suelta: es el único diferencial que un competidor no puede igualar sin admitir
que él tampoco es infalible.

## Lo que ya está resuelto en el código

- Títulos dentro del límite de ~60 caracteres, con `template` heredado.
- `description` única por página, canónica en todas, dominio centralizado en
  `lib/seo.js`.
- Open Graph completo (con dimensiones) y Twitter Card; `og.png` con la marca
  oficial.
- Datos estructurados: `Organization` (con logo raster 512) + `WebSite` global,
  `SoftwareApplication` con los tres planes, `FAQPage`, `Article` y
  `BreadcrumbList`.
- Directivas `robots` explícitas con `max-image-preview: large`.
- `sitemap.xml` con las 11 URLs y `robots.txt` apuntando a él.
- Favicons oficiales (32, 192, 512, apple-touch) y `site.webmanifest`.
- Enlazado interno: cabecera, pie, bloque de la home, hub de guías y
  relacionadas. Todo se alimenta de `lib/guias.js`.
- H1 de la home con la palabra clave («Cuando te yapean…»).
- Jerarquía de encabezados correcta (un solo h1 por página, sin saltos de nivel).
- Exportación estática: sin servidor, carga mínima en datos móviles.

## Lo que falta y solo puedes hacer tú

Ordenado por impacto.

### 1. Comprar `pagoya.pe` 🔴
Ver la sección de arriba. Bloquea el arranque de SEO.

### 2. Número de WhatsApp real 🔴
`lib/enlaces.js` sigue con `51999999999`. **Todos los botones del sitio llevan a
un número inexistente.** Además hay tres sitios donde el número se muestra como
texto `+51 9XX XXX XXX` y hay que escribirlo: `app/ayuda/page.jsx` y
`app/consultas/page.jsx` (marcados con `TODO(whatsapp)`).

### 3. Google Search Console 🔴
1. Da de alta el dominio en [search.google.com/search-console](https://search.google.com/search-console).
2. Verifica por DNS (recomendado) o por etiqueta HTML: si eliges la etiqueta,
   descomenta `verification.google` en `app/layout.jsx` y despliega.
3. **Sitemaps → Enviar** → `sitemap.xml`.
4. **Inspección de URLs** → las 11 URLs → *Solicitar indexación*. Esto baja el
   tiempo de indexación de semanas a días.

### 4. Analytics
Descomenta el script de Plausible en `app/layout.jsx` (o cámbialo por GA4) y
**corrige el `data-domain`**, que hoy dice `pagoya.pe` mientras el sitio corre en
Vercel: con el dominio mal, no registra nada. Los eventos `WhatsApp` (con la prop
`donde`) y `DemoVoz` ya se disparan solos.

### 5. Ficha de Google Business Profile
Gratis y lo que más rápido te hace visible en Perú. Categoría «Empresa de
software», con enlace al sitio. Aparece en días, no en meses.

### 6. Google Ads (la vía rápida de verdad)
Presupuesto de arranque S/ 15–25/día, Perú, español.
- Grupo 1 → `/parlante-para-yape/`: «parlante para yape», «qr parlante precio».
- Grupo 2 → `/yape-falso/`: «yape falso», «como saber si un yape es falso».
- Grupo 3 → `/`: «app anunciar pagos yape», «anunciador de pagos».
Negativas obligatorias: «gratis apk», «hackear», «generador».
Las landings ya existen y son relevantes: eso baja el costo por clic.

### 7. Enlaces entrantes (lo que decide del mes 3 en adelante)
- Perfiles con enlace: TikTok, Facebook, Instagram (ya en el pie), LinkedIn.
- Ficha de Google Play cuando la app se publique → enlaza al sitio.
- Directorios de emprendimiento peruano y de apps fintech.
- Notas de prensa locales sobre el yape falso: es tema con interés periodístico.
- Grupos de bodegueros en Facebook y WhatsApp: compartir la guía de
  notificaciones (útil de verdad, no publicidad) trae visitas y enlaces.

### 8. Pendientes menores
- Badge de Google Play en la home cuando la app esté publicada.
- Verificar las URLs finales de TikTok/Facebook/Instagram en `components/Pie.jsx`
  (hoy asumen `@pagoya.pe`) — alimentan el `sameAs` de `Organization`.

---

## Ritmo de contenido sugerido

Una guía nueva cada 10–14 días. Cada una: entrada en `lib/guias.js`, carpeta en
`app/`, URL en `public/sitemap.xml`, y reenvío del sitemap en Search Console.
El enlazado interno se actualiza solo.

Próximas candidatas, por orden de facilidad para rankear:

1. «Cómo cobrar con Yape en mi negocio: guía para bodegas»
2. «Yape vs Plin para negocios: cuál conviene»
3. «Cómo cuadrar la caja de tu bodega al final del día»
4. «¿Yape cobra comisión a los negocios?»
5. «Trabajadores y caja: cómo controlar los cobros sin estar en la tienda»

## Páginas por segmento (racimos de keywords nuevos)

`MERCADO.md` abre tres segmentos fuera de la bodega que merecen página propia.
Cada uno es un racimo de consultas con competencia **casi nula** y una intención
que la home no cubre bien:

| Página | Consultas objetivo | Estado |
|---|---|---|
| `/para-delivery/` | «confirmar pago delivery», «motorizado captura yape», «cómo saber si el cliente pagó el delivery», «cobrar contra entrega yape» | ✅ **publicada** |
| `/para-taxistas/` | «yape de mi esposa en mi celular», «cobrar taxi sin sacar el celular», «taxista yape falso» | pendiente — más volumen |
| `/para-transporte/` | «cobrador combi yape», «cobrar pasaje con yape» | pendiente — ciclo largo |

Van con la misma plantilla que las guías (`meta` + `Article` + `FAQPage` +
`BreadcrumbList` + `CajaCta`), pero con copy propio del segmento. El catálogo
vive en `lib/segmentos.js`: cada entrada tiene un campo `tarjeta` que la enlaza
con su tarjeta de `#para-quien` en la home, y el pie de página las lista solo.
**Una tarjeta sin página no muestra enlace**, así se publican de a uno sin tocar
la home.

## Cómo revisar que todo sigue bien

- **Datos estructurados:** [validator.schema.org](https://validator.schema.org/) y la
  Prueba de resultados enriquecidos de Google, una URL por vez.
- **Velocidad:** PageSpeed Insights en móvil. Objetivo LCP < 2.5 s; el sitio es
  estático, así que si baja de ahí es por una imagen pesada.
- **Indexación:** busca `site:<dominio>` en Google. Deben salir las 11 páginas.
- **Enlaces rotos:** tras agregar una guía, comprueba que su ruta en
  `lib/guias.js` coincide exactamente con la carpeta en `app/` (con `/` final).
