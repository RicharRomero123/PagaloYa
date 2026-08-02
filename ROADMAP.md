# PagoYa — Plan de acción

> Estrategia: construir el producto completo directamente (sin MVP desechable),
> validando en paralelo con landing + entrevistas para no invertir a ciegas.

## Mapa del sistema

```
                        ┌─────────────────┐
  Notificación Yape →   │  APP ANDROID     │ ← teléfono con el Yape del negocio
                        │  (Kotlin)        │   captura + anuncia por voz
                        └───────┬─────────┘
                                │
                        ┌───────▼─────────┐
                        │  BACKEND         │  Firebase: auth, FCM, Remote Config
                        │  (Firestore/FCM) │  patrones de billeteras, historial
                        └───┬────┬────┬───┘
                            │    │    │
              ┌─────────────▼┐ ┌─▼──────────┐ ┌▼──────────────┐
              │ APP escucha  │ │ PANEL WEB  │ │ PARLANTE IoT   │
              │ (trabajador/ │ │ (Next.js)  │ │ (fase 2,       │
              │ dueño remoto)│ │ OPERADOR   │ │ soundbox OEM)  │
              └──────────────┘ └────────────┘ └───────────────┘

  LANDING (pagoya.pe) → captación, waitlist, venta de planes
```

**Quién usa qué** (importante, cambió respecto al plan original):

- **App móvil** → el comerciante. Captura, anuncia y **es donde el dueño ve todos
  sus reportes**. El dueño nunca entra al panel web.
- **Panel web** → tú y tu equipo. Membresías, mapa de cobertura, resellers y
  comisiones. Es una consola de operador, no un producto para el cliente.
  Especificación completa en `PANEL.md`.

## Estructura del repo

```
PagoYa/
├── PLAN.md, BRAND.md, ROADMAP.md   → documentos maestros
├── app-android/                     → app Kotlin (captura + escucha)
├── backend/                         → Firebase (functions, rules, config)
├── panel/                           → panel web del dueño (Next.js)
├── landing/                         → pagoya.pe (estática, waitlist)
├── hardware/                        → fase 2: integración soundbox, docs OEM
└── .claude/agents/                  → agentes especializados por área
```

## Fase 0 — Validación en paralelo (semanas 1–3, NO bloquea el desarrollo)

- [ ] Comprar dominio pagoya.pe + reservar @pagoya.pe en TikTok/Instagram/Facebook
- [ ] Búsqueda fonética en Indecopi → registrar marca (clases 9 y 36)
- [ ] Publicar landing con lista de espera (primera pieza de código que sale)
- [ ] 20 entrevistas a comerciantes (bodegas, puestos de mercado): ¿les pasó el yape
      fake? ¿cuánto pagarían? ¿quién atiende cuando el dueño no está?
- [ ] **Meta de validación: ≥ 100 registros en waitlist o ≥ 10 comercios que
      confirmen "lo pago"** antes de gastar en hardware/marketing pagado

## Fase 1 — Producto completo software (semanas 1–8)

### Backend (semanas 1–3)
- [ ] Proyecto Firebase: Auth (teléfono), Firestore multi-comercio, FCM
- [ ] Modelo de datos: comercio → dueño/trabajadores → dispositivos → pagos
- [ ] Remote Config: patrones de billeteras (Yape primero) actualizables sin release
- [ ] Cloud Functions: fan-out de notificación capturada → todos los escuchas en < 2 s

### App Android (semanas 2–6)
- [ ] NotificationListenerService + foreground service + exclusión de batería
- [ ] Parser Yape (patrones desde Remote Config) + modo aprendizaje
- [ ] TTS: "¡PagoYa! Juan te yapeó 25 soles"
- [ ] Modo captura (teléfono del dueño) y modo escucha (trabajadores)
- [ ] Onboarding criollo: 3 pantallas, pedir permisos sin asustar
- [ ] Historial del día + total acumulado
- [ ] **Guardián de Yape** (crítico): onboarding por marca (autostart Xiaomi/Samsung/
      Huawei/Oppo + batería sin restricción para Yape), detector de Yape en estado
      detenido con alerta hablada, y watchdog de horas sin pagos en horario de negocio

### Reportes en la app (semanas 4–6) — antes eran del panel
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

### Landing (semanas 1–2, primero waitlist, luego venta)
- [ ] pagoya.pe: promesa anti-fake, demo en video, waitlist → planes y precios
- [ ] Botón de contacto por WhatsApp (canal de venta real en Perú)

### Cierre de fase
- [ ] Beta cerrada: 10–20 comercios reales, 2–4 semanas, gratis
- [ ] Cobro de suscripciones: **todo manual** (efectivo o Yape) y activación
      desde el panel. La pasarela (Culqi/Mercado Pago) es lo único que exige un
      servidor para el webhook — se agrega cuando el volumen lo justifique, sin
      tocar nada de lo construido

## Fase 2 — Lanzamiento comercial (semanas 9–12)

- [ ] Publicar en Play Store (declarar bien el permiso de notificaciones)
- [ ] Activar precios: Gratis / Caserito S/ 9.90 / Patrón S/ 24.90
- [ ] Marketing: TikTok orgánico (el parlante anunciando es muy viral) + venta
      puerta a puerta en 2–3 mercados de Lima con demo en vivo
- [ ] Métricas norte: comercios activos/semana, % que pasa de gratis a pago, churn

## Fase 3 — Hardware (mes 4+, solo si Fase 2 valida: ~200 comercios activos)

- [ ] Comprar 2–3 muestras de soundbox por AliExpress → probar API/servidor propio
- [ ] Elegir OEM en Alibaba, negociar lote 100 con logo PagoYa
- [ ] Homologación MTC (verificar si el modelo ya está homologado) + VUCE
- [ ] Contrato de comodato con abogado
- [ ] Lanzar Plan Patrón con parlante incluido

## Agentes del proyecto (.claude/agents/)

| Agente | Área | Cuándo usarlo |
|---|---|---|
| `app-android` | App Kotlin | Todo el desarrollo móvil (listener, parser, TTS, UI) |
| `backend-firebase` | Backend | Firestore, FCM, Remote Config, Functions, seguridad |
| `panel-web` | Panel Next.js | Consola del **operador**: membresías, cobertura, resellers |
| `landing-page` | pagoya.pe | Landing, waitlist, SEO, conversión |
| `marketing-copy` | Contenido | Copy criollo, guiones TikTok, textos de venta y de la app |
