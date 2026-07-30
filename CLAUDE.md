# PagoYa

Anunciador de voz de pagos Yape/Plin para comercios peruanos. Captura las
notificaciones reales del sistema en Android y las anuncia por voz en el local y
a distancia. Anti "Yape falso": si no suena, no te pagaron.

## Documentos maestros (leer antes de trabajar)
- `PLAN.md` — negocio, competencia, precios, presupuesto, riesgos
- `BRAND.md` — nombre, tono criollo, colores, voz del parlante, planes
- `ROADMAP.md` — fases, mapa del sistema, checklist de tareas
- `REDES.md` — plan de redes, contenido, prompts de flyers, crecimiento orgánico

## Estructura
- `app-android/` — app Kotlin nativa (captura + escucha) → agente `app-android`
- `backend/` — Firebase (Firestore, FCM, Functions, Remote Config) → agente `backend-firebase`
- `panel/` — panel web del dueño, Next.js → agente `panel-web`
- `landing/` — pagoya.pe → agente `landing-page`
- `hardware/` — fase 2: soundbox OEM, homologación MTC, comodato

## Reglas transversales
1. **Anti-fake es la regla de oro**: los pagos solo nacen de notificaciones reales
   del sistema capturadas por la app Android. Ningún otro componente crea pagos.
2. **Patrones de billeteras viven en Remote Config**, nunca hardcodeados en el APK.
3. **Marca**: tono cercano y criollo (BRAND.md). Naranja #FF6B1A + azul #1A2B4A.
   Prohibido usar logo/morado de Yape o implicar asociación con BCP.
4. **Idioma**: todo el producto y los commits en español.
5. **Datos personales** (nombres de pagadores): minimizar, proteger (Ley 29733).
