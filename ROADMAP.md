# PagoYa — Plan de acción

> **Posición:** PagoYa no compite por cobrar, compite por avisar. Es la capa de
> certeza sobre el riel que el comerciante ya usa — por eso sin comisión y sin RUC.
> *Cobra como quieras. Entérate siempre.*
>
> **Estrategia:** construir el producto completo directamente (sin MVP desechable),
> validando en paralelo con landing + entrevistas para no invertir a ciegas.
>
> Actualizado 3 de agosto de 2026 con `ESCALA.md` (ruta al 100 %) y
> `CRECIMIENTO.md` (motores, etapas, garantías).

## Bitácora de sesiones

### 2026-08-08 — Multi-billetera, Modo ciego y release 0.3.0

**Hecho hoy (todo commiteado en `dev`):**
- **Billeteras nuevas: Prexpe (`air.PrexPeru`) y Lemon Cash (`com.applemoncash`)** —
  patrones de parseo probados contra notificaciones reales (monto en título +
  nombre en cuerpo, con lookahead y respaldo por monto), iconos en la lista de
  pagos, y **publicadas en Remote Config** (`billeteras_json`) → llegan a los
  teléfonos ya instalados **sin release**.
- **Modo ciego completo (app + backend), desplegado a producción**: latido de
  presencia del capturador, banner rojo + alerta hablada en los escuchas, push a
  apps cerradas (Function programada `vigilarCiego` cada 2 min), y registro de
  periodos ciegos (base de la Garantía de Aviso). Umbrales en Remote Config
  (`ciego_umbral_seg`=240, `ciego_cadencia_seg`=90, horario 7–22).
- **Config "Billeteras que escucho"** (pantalla en *Más*): activar/desactivar por
  billetera, **todas activas por defecto** (modelo opt-out; billetera nueva de
  Remote Config queda activa sola).
- **Infra**: reglas Firestore + índices desplegados. **Remote Config ahora se
  versiona** en `backend/remoteconfig.template.json` (antes estaba vacío).
- **Release**: `versionCode` 3→**4**, `versionName` **0.3.0**. AAB firmado con la
  clave de subida real en `app-android/pagoya-0.3.0-v4.aab`, **listo para Play**.
- **Redes**: portada de Facebook en `docs/redes/portada-facebook.png`.

**En vivo en producción** (`pagoya-45018`): Remote Config, reglas, índices y
`vigilarCiego`. Las funciones de pago (`fanoutPago`, `trialAlCrearComercio`) no se
tocaron.

**Dónde continuamos (mañana):**
- [ ] **Subir el AAB 0.3.0 a Google Play** — recomendado ir primero a *Prueba
      interna* y validar en un teléfono real: que suene Prexpe/Lemon y que aparezca
      el banner rojo del Modo ciego.
- [ ] Del gap analysis, aún sin hacer: **(A)** campos `fuente`+`confianza` en `Pago`
      + semáforo ✅/🔵/🔴; **(B)** hora pico + comparativo hoy/ayer/semana + total
      mensual en Caja; **watchdog "horas sin pagos en horario"**; **subir el modo
      aprendizaje al backend**.
- [ ] Menor: `firebase-functions` desactualizado; migrar functions nodejs20→22
      antes del 2026-10-30; limpiar imágenes GCR del último deploy.
- [ ] Courier SaaS: proyecto aparte (VPS), pendiente que Richar pase la ruta local.

## Mapa del sistema

```
   FUENTES DE INGESTA (intercambiables — ver ESCALA.md §4)
   ┌──────────────────────────────────────────────────────┐
   │  A. App Android en el teléfono del dueño   (fase 1)  │
   │  B. Capturador PagoYa: equipo dedicado     (fase 3)  │ ← el que rompe el 80 %
   │  C. Webhook de PSP / API de billetera      (futuro)  │
   └───────────────────────┬──────────────────────────────┘
                           │  evento crudo
                           ▼
              ┌────────────────────────────┐
              │  BACKEND  Firebase          │  auth, FCM, Remote Config
              │  · normaliza · deduplica    │  patrones, historial
              │  · puntúa confianza         │
              │  · vigila el latido         │  ← modo ciego
              └───┬─────────┬─────────┬────┘
                  │         │         │
        ┌─────────▼──┐ ┌────▼─────┐ ┌─▼──────────────┐
        │ APP escucha │ │PANEL WEB │ │ PARLANTE IoT   │
        │ (Android +  │ │(Next.js) │ │ (fase 4,       │
        │  iOS fase 4)│ │ OPERADOR │ │ soundbox OEM)  │
        └─────────────┘ └──────────┘ └────────────────┘

  LANDING (pagoya.pe) → captación, waitlist, venta de planes
```

**Quién usa qué:**

- **App móvil** → el comerciante. Captura, anuncia y **es donde el dueño ve todos
  sus reportes**. El dueño nunca entra al panel web.
- **Panel web** → tú y tu equipo. Membresías, cobertura, resellers, comisiones.
  Consola de operador, no producto de cliente. Ver `PANEL.md`.

## Estructura del repo

```
PagoYa/
├── PLAN.md · BRAND.md · MERCADO.md      → documentos maestros
│   ROADMAP.md · ESCALA.md · CRECIMIENTO.md
│   REDES.md · PANEL.md
├── app-android/                          → app Kotlin (captura + escucha)
├── backend/                              → Firebase (functions, rules, config)
├── panel/                                → panel web del OPERADOR (Next.js)
├── landing/                              → pagoya.pe (estática, waitlist)
├── hardware/                             → fase 3–4: Capturador y soundbox OEM
└── .claude/agents/                       → agentes especializados por área
```

## Cómo se lee este roadmap

Cada fase técnica corresponde a una etapa de crecimiento de `CRECIMIENTO.md` §6.
**Nada se construye antes de su etapa**, y cada compuerta se cumple antes de pasar.

| Fase técnica | Etapa de crecimiento | Meta de comercios | Qué desbloquea |
|---|---|---|---|
| 0 — Validación | — | 0 | Saber si el mercado existe |
| 1 — Producto software | A. Prueba | 0 → 10 | Que funcione en la calle |
| 2 — Lanzamiento | B. Mercado Modelo | 10 → 100 | **Garantía de Transparencia** |
| 3 — Multi-billetera + Capturador | C. Réplica | 100 → 500 | **Punto de equilibrio** |
| 4 — Escala | D. Escala | 500 → 2,000 | **Garantía de Aviso**, iOS, parlante |

---

## Fase 0 — Validación en paralelo (semanas 1–3, NO bloquea el desarrollo)

- [ ] Comprar dominio pagoya.pe + reservar @pagoya.pe en TikTok/Instagram/Facebook
- [ ] Búsqueda fonética en Indecopi → registrar marca (clases 9 y 36)
- [ ] Publicar landing con lista de espera (primera pieza de código que sale)
- [ ] 20 entrevistas a comerciantes: ¿les pasó el yape fake? ¿cuánto pagarían?
      ¿quién atiende cuando el dueño no está?
- [ ] **Darse de alta en Yape Empresa con una cuenta real** ← lo más urgente.
      Resolver: ¿la comisión aplica a **todo** cobro o solo al QR de Empresa?
      ¿los 5 ayudantes reciben aviso sonoro o tienen que mirar la pantalla?
      ¿hay tramo gratuito? → define el copy de venta entero (`ESCALA.md` §14)
- [ ] **Elegir el Mercado Modelo**: 80–200 puestos, cerca de ti, con movimiento.
      Todo lo demás depende de esta elección (`CRECIMIENTO.md` §5)
- [ ] **Meta de validación: ≥ 100 registros en waitlist o ≥ 10 comercios que
      confirmen "lo pago"** antes de gastar en hardware o marketing pagado

---

## Fase 1 — Producto completo software (semanas 1–8) · Etapa A: 0 → 10

### Backend (semanas 1–3)
- [ ] Proyecto Firebase: Auth (teléfono), Firestore multi-comercio, FCM
- [ ] Modelo de datos: comercio → dueño/trabajadores → dispositivos → pagos
- [ ] Remote Config: patrones de billeteras (Yape primero) actualizables sin release
- [ ] Cloud Functions: fan-out de notificación capturada → todos los escuchas en < 2 s
- [ ] **Capa de ingesta desde el diseño** (`ESCALA.md` §4) — aunque hoy solo exista
      la fuente A. Retrofitear esto después duele mucho:
  - [ ] Evento normalizado con campo `fuente` y `confianza`
  - [ ] Deduplicación por `(monto, ventana ±3 min, nombre/dígitos, billetera)`
  - [ ] Latido del capturador cada 60 s + detección de ausencia

### App Android (semanas 2–6)
- [ ] NotificationListenerService + foreground service + exclusión de batería
- [ ] Parser Yape (patrones desde Remote Config) + modo aprendizaje
- [ ] TTS: "¡PagoYa! Juan te yapeó 25 soles" ← **la voz dice la marca: es el
      Motor 1 de crecimiento, no vanidad** (`CRECIMIENTO.md` §4)
- [ ] Modo captura (teléfono del dueño) y modo escucha (trabajadores)
- [ ] Onboarding criollo: 3 pantallas, pedir permisos sin asustar
- [ ] Historial del día + total acumulado
- [ ] **Guardián de Yape** (crítico): onboarding por marca (autostart Xiaomi/Samsung/
      Huawei/Oppo + batería sin restricción para Yape), detector de Yape en estado
      detenido con alerta hablada, y watchdog de horas sin pagos en horario de negocio
- [x] **Modo ciego** ← la mejora con mejor relación esfuerzo/valor del proyecto: *(2026-08-08, en producción)*
  - [x] Sin latido → banner rojo en **todos** los dispositivos escuchando
  - [x] Alerta hablada + push al dueño: *"dejé de escuchar tu Yape hace 3 minutos"* (Function `vigilarCiego`)
  - [x] Registro de periodos ciegos (después sustenta la Garantía de Aviso)
- [ ] Indicador de confianza por pago: ✅ confirmado · 🔵 probable · 🔴 ciego ← **próximo (gap A)**

### Reportes en la app (semanas 4–6)
- [ ] La pestaña Caja lee el historial del comercio desde Firestore, no solo el
      registro local del teléfono
- [ ] Hoy vs. ayer vs. semana pasada (el comparativo es lo que engancha)
- [ ] **Hora pico**: a qué hora vende más. Nadie se lo da y le cambia el día
- [ ] Totales por semana y mes, desglose por billetera
- [ ] Cierre de caja con corte y compartir por WhatsApp

### Panel web del OPERADOR (semanas 4–7) — ver `PANEL.md`
- [ ] Rol `operadores` en las reglas de Firestore (+ prueba en emulador)
- [ ] Login y lista de comercios con su estado de membresía
- [ ] Ficha de comercio + **activar/renovar plan manual** ← con esto ya cobras
- [ ] Alta de campo mobile-first: GPS, foto de fachada, código de 6 dígitos
- [ ] Mapa de cobertura (Leaflet + OpenStreetMap, sin Google Maps)
- [ ] Resellers: atribución de altas y comisión recurrente
- [ ] Salud: comercios sin pagos hace 48h, altas que nunca activaron
- [ ] **Minutos ciegos por comercio** en la pantalla de Salud ← es la métrica de
      precisión real del producto

### Landing (semanas 1–2, primero waitlist, luego venta)
- [ ] pagoya.pe: promesa anti-fake, demo en video, waitlist → planes y precios
- [ ] Botón de contacto por WhatsApp (canal de venta real en Perú)
- [ ] **Calculadora "cuánto te cobraría Yape Empresa este mes"**: el visitante pone
      su venta diaria y su ticket promedio → sale el 2.95 % + S/ 0.29 contra los
      S/ 9.90. **Probablemente la mejor pieza de conversión de la landing.**
      Citar la tarifa como fuente y usar los números que declare el visitante, sin
      inflarlos: si hace la cuenta y no cuadra, pierdes lo único que vendes
- [ ] **"PagoYa no te pide RUC"** como bloque propio ← para el informal no es una
      comodidad, es poder cobrar o no poder (`ESCALA.md` §10.4)

### Cierre de fase — compuerta a Fase 2
- [ ] Beta cerrada: 10–20 comercios reales del Mercado Modelo, 2–4 semanas, gratis
- [ ] Cobro de suscripciones: **todo manual** (efectivo o Yape) y activación desde
      el panel. La pasarela se agrega cuando el volumen lo justifique, sin tocar
      nada de lo construido
- [ ] ✅ **Compuerta: 10 comercios usándolo 3 semanas seguidas**

---

## Fase 2 — Lanzamiento y Mercado Modelo (meses 3–5) · Etapa B: 10 → 100

- [ ] Publicar en Play Store (declarar bien el permiso de notificaciones)
- [ ] Activar precios: Gratis / Caserito S/ 9.90 / Patrón S/ 24.90
- [ ] **Lanzar la Garantía de Transparencia**: *"PagoYa te avisa cuando no puede
      avisarte."* Ya está construida (modo ciego); esto es ponerla en la landing,
      en la app y en el discurso de venta. Nadie más la ofrece
- [ ] **Saturar el Mercado Modelo — meta: 30 % de los puestos.** No abrir un
      mercado nuevo antes (`CRECIMIENTO.md` §5)
- [ ] **Sticker del mostrador** producido y repartido ← es el activo de marca en el
      punto de venta y el amplificador del Motor 1
- [ ] Contenido: los 5 TikToks de `REDES.md` §4, grabados **en el Mercado Modelo**
- [ ] Primeros resellers: buscar al que ya entra a 40 bodegas por semana
      (distribuidor, repartidor, mayorista), no contratar vendedores
- [ ] ✅ **Compuerta: 30 % de un mercado + churn < 8 %**

**No hacer en esta fase:** publicidad pagada, otros mercados, hardware, iOS.

---

## Fase 3 — Multi-billetera y Capturador (meses 6–10) · Etapa C: 100 → 500

Aquí se construye la **ventaja durable**, porque la de hoy (precio y RUC) es
prestada y puede caerse cuando Yape quiera (`CRECIMIENTO.md` §3).

### Multi-billetera — sube de prioridad
- [ ] **Plin** (apps de Interbank, BBVA y Scotiabank; Plin no es app propia)
- [ ] Registro de **efectivo** a mano, marcado como "sin verificar"
- [ ] **Cierre de caja único**: Yape + Plin + efectivo en un solo corte
      ← esto Yape Empresa **no lo puede hacer nunca**. Es el foso real
- [ ] Desglose por billetera en reportes y voz

### Capturador PagoYa — piloto
> Sin ruta de correo (verificado: las billeteras solo emiten push), **el Capturador
> es el único camino al 100 % y a iOS** sin depender de un acuerdo con la billetera.

- [ ] Piloto con 10–20 unidades: Android de entrada dedicado (S/ 250–350),
      solo Yape + PagoYa, enchufado 24/7, configurado por PagoYa
- [ ] Medir: minutos ciegos con Capturador vs. sin él ← justifica todo lo demás
- [ ] Caso del dueño con iPhone: Yape personal en el iPhone, Yape del negocio en
      el Capturador. **Resuelve iOS sin API de Apple**
- [ ] Contrato de comodato con abogado (equipo propiedad de PagoYa)
- [ ] Definir si va en plan Patrón, con garantía inicial o plan anual — el
      recupero es de 8–12 meses, más lento que el parlante solo

### Réplica comercial
- [ ] Repetir el libreto del Mercado Modelo en 4–6 mercados, uno a la vez
- [ ] Panel de resellers completo con comisión recurrente
- [ ] ✅ **Compuerta: 400–500 comercios pagando = punto de equilibrio**

---

## Fase 4 — Escala (mes 11+) · Etapa D: 500 → 2,000

- [ ] **Garantía de Aviso**: *"si te cayó un Yape y PagoYa no te avisó, ese mes no
      lo pagas."* Solo con el Capturador desplegado y minutos ciegos ≈ 0.
      Condiciones en `CRECIMIENTO.md` §2.2. **Prometerla antes es una máquina de
      reembolsos y reseñas malas**
- [ ] **App iOS de escucha**: recibe push y habla con `AVSpeechSynthesizer`.
      Nunca prometer que lee notificaciones — prometer *"tu iPhone te avisa"*
- [ ] **Parlante de mostrador**: muestras de soundbox por AliExpress → probar que
      apunte a servidor propio → OEM en Alibaba, lote 100 con logo → homologación
      MTC + VUCE. Evaluar si Capturador y parlante son **un solo aparato Android**
- [ ] **Capa de operación**: roles avanzados, multi-sucursal, conciliación,
      exportables ← la ventaja más durable de todas
- [ ] Otras ciudades, siempre con el libreto de densidad
- [ ] Pasarela de pago para cobrar suscripciones (recién aquí lo justifica el volumen)

---

## Métricas norte

Cinco, en orden. Si una está mal, no sirve mirar las de abajo.

| # | Métrica | Meta | Qué te dice |
|---|---|---|---|
| 1 | Comercios activos pagando | 400–500 = equilibrio | ¿Existe el negocio? |
| 2 | Churn mensual | < 5 % | ¿El producto se queda? |
| 3 | **Minutos ciegos por comercio/mes** | → 0 | **Precisión real.** La métrica más honesta que tienes |
| 4 | Altas por mercado / altas existentes | > 1 | ¿El Motor del sonido está prendido? |
| 5 | % que pasa de Gratis a pago | > 15 % | ¿El plan gratis alimenta o canibaliza? |

La #3 es la obsesión: si tiende a cero, la Garantía de Aviso se vuelve barata y el
100 % deja de ser promesa para volverse hecho medible.

---

## Vigilancia competitiva (trimestral)

Revisar precio, requisito de RUC y tramos gratuitos de **Yape Empresa**
(`ESCALA.md` §10.5). Dos movimientos suyos cambiarían el plan:

| Si Yape… | Se cae | Reacción |
|---|---|---|
| quita el requisito de RUC | la ventaja "sin RUC" | acelerar multi-billetera y hardware |
| regala los ayudantes en cuenta personal | la ventaja de precio | acelerar la capa de operación |

En ninguno de los dos casos se compite de frente: se reposiciona hacia lo durable.

---

## Agentes del proyecto (.claude/agents/)

| Agente | Área | Cuándo usarlo |
|---|---|---|
| `app-android` | App Kotlin | Todo el desarrollo móvil (listener, parser, TTS, modo ciego, UI) |
| `backend-firebase` | Backend | Firestore, FCM, Remote Config, Functions, capa de ingesta, seguridad |
| `panel-web` | Panel Next.js | Consola del **operador**: membresías, cobertura, resellers, salud |
| `landing-page` | pagoya.pe | Landing, waitlist, calculadora Yape Empresa, SEO, conversión |
| `marketing-copy` | Contenido | Copy criollo, guiones TikTok, textos de venta y de la app |
