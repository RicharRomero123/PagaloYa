# PagoYa

Anunciador de voz de pagos Yape/Plin para comercios peruanos. Captura las
notificaciones reales del sistema en Android y las anuncia por voz en el local y
a distancia. Anti "Yape falso": si no suena, no te pagaron.

## Documentos maestros (leer antes de trabajar)
- `PLAN.md` — negocio, competencia, precios, presupuesto, riesgos
- `MERCADO.md` — mapa de segmentos (bodegas, taxis, delivery, transporte) y qué
  implica cada uno para el producto y la landing
- `BRAND.md` — nombre, tono criollo, colores, voz del parlante, planes
- `ROADMAP.md` — fases, mapa del sistema, checklist de tareas
- `ESCALA.md` — cómo escalar: del 80 % de precisión al 100 %, iOS, 0 % comisión,
  capa de ingesta multi-fuente y por qué NO volverse pasarela ni billetera
- `CRECIMIENTO.md` — factor diferencial, ventaja competitiva por capas, la
  Garantía PagoYa, los 4 motores de crecimiento y las etapas 0 → 2,000 comercios
- `REDES.md` — plan de redes, contenido, prompts de flyers, crecimiento orgánico
- `PANEL.md` — panel de operador: roles, membresías, cobertura, resellers

## Estructura
- `app-android/` — app Kotlin nativa (captura + escucha) → agente `app-android`
- `backend/` — Firebase (Firestore, FCM, Functions, Remote Config) → agente `backend-firebase`
- `panel/` — panel web del **operador** (tuyo, no del cliente) → agente `panel-web`
- `landing/` — pagoya.pe → agente `landing-page`
- `hardware/` — fase 2: soundbox OEM, homologación MTC, comodato

## Reglas transversales
1. **Anti-fake es la regla de oro**: los pagos solo nacen de notificaciones reales
   del sistema capturadas por la app Android. Ningún otro componente crea pagos.
2. **Patrones de billeteras viven en Remote Config**, nunca hardcodeados en el APK.
3. **Marca**: tono cercano y criollo (BRAND.md). Naranja #FF6B1A + azul #1A2B4A.
   Prohibido usar el logo/morado de Yape en la **identidad de PagoYa**, en la
   ficha de Play, capturas o marketing, o implicar asociación con BCP.
   Excepción acotada: los logos de Yape/Plin SÍ se muestran **dentro de la app,
   solo en la lista de pagos** (badge de billetera), como uso descriptivo para
   indicar de qué billetera vino cada cobro. Nunca fuera de ahí.
4. **Idioma**: todo el producto y los commits en español.
5. **Datos personales** (nombres de pagadores): minimizar, proteger (Ley 29733).
