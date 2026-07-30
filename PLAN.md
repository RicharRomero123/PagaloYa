# PagoYa — Plan de negocio y presupuesto

> Anunciador de voz para pagos Yape/Plin, con modo "dueño remoto" para comercios
> donde el dueño no está en el local. Fecha del plan: julio 2026.

## 1. Problema

- Estafas con "Yape falso" (apps y capturas que simulan pagos) siguen activas en Perú.
- En locales con trabajadores, el dueño tiene la cuenta Yape en su teléfono y los
  trabajadores no pueden verificar los pagos sin llamarlo.

## 2. Solución

App Android que lee las notificaciones reales del sistema (Yape, Plin, BCP) y:

1. Anuncia por voz: "Juan te yapeó 25 soles".
2. Reenvía la notificación a la nube → los teléfonos de los trabajadores (y luego
   el dispositivo IoT del mostrador) la anuncian al instante, aunque el dueño no esté.
3. Solo anuncia notificaciones reales del sistema operativo → si no suena, no pagaron
   (anti Yape-fake).

**Restricción clave:** Yape no tiene API pública. Todo depende de leer notificaciones
en Android (NotificationListenerService). Por eso:

- Android-only (iOS no permite leer notificaciones de otras apps).
- El dispositivo IoT nunca es autónomo: siempre se alimenta del teléfono del dueño
  (por Bluetooth o vía backend).

## 3. Competencia

| Competidor | Qué es | Precio | Debilidad |
|---|---|---|---|
| Izipay QR Parlante | Dispositivo con chip, 15 billeteras | S/ 129 | Obliga a afiliarse a Izipay como procesador |
| yaPagué (app) | Anuncia Yape/Plin por voz | Freemium | Solo local, sin modo multi-teléfono/dueño remoto |
| Yapay (app/web) | Registra pagos en Google Sheets, avisa al equipo | ? | Menos enfocado en voz en mostrador |

**Diferenciador de PagoYa:** modo dueño-remoto multi-teléfono + funciona con el Yape
que el comercio ya tiene (sin cambiar de procesador de pagos).

## 4. Decisión IoT vs teléfono

- **Fase 1 (ahora): solo app en teléfono.** 90% del valor, costo mínimo, iteración rápida.
  Sirve para validar demanda antes de invertir en hardware.
- **Fase 2 (con tracción validada): dispositivo IoT en comodato — este es el
  diferenciador central del negocio.** Parlante de mostrador (ESP32 + parlante I2S +
  carcasa con logo), conectado **por WiFi al backend** (no solo BLE al teléfono),
  para que funcione aunque el dueño no esté en el local. Versión con chip SIM
  después, para locales sin WiFi. BOM estimado S/ 50–70/unidad en volumen bajo.

### Por qué el hardware diferencia

- Siempre encendido en el mostrador, suena fuerte, no depende del celular del trabajador.
- Refuerza el anti-fake: "si la caja no habló, no te pagaron".
- Presencia física = confianza + publicidad (logo en cada mostrador).
- Vs Izipay: sin cambiar de procesador. Vs apps gratuitas: experiencia dedicada y profesional.

### Modelo comodato (tipo módem de ISP)

- El equipo es propiedad de PagoYa; se entrega **a S/ 0 de entrada** mientras la
  membresía esté activa.
- Contrato de **comodato** con cláusula de reposición por daño/pérdida (S/ 80–100)
  y opcionalmente garantía inicial de S/ 30–50 devuelta al retornar el equipo.
- Recupero del costo del equipo en 2–3 meses de membresía; margen desde el mes 4.
- Riesgos: consume capital (se financia la flota), churn implica logística de
  recuperación de equipos, soporte y reparaciones.

### Stack técnico (decidido)

- **App de captura (teléfono con el Yape del negocio): Kotlin nativo.** El
  NotificationListenerService, el foreground service y la exclusión de batería son
  API nativas Android. Este rol es Android-only por obligación: iOS no permite leer
  notificaciones de otras apps, sin excepción.
- **Apps/canales de escucha (trabajadores, dueño remoto):** solo reciben push del
  backend, así que funcionan en cualquier plataforma — panel web (React/Next.js),
  app iOS opcional más adelante, y el dispositivo IoT.
- **Backend: Firebase** (FCM para sync en tiempo real, Remote Config para patrones).
- **Firmware IoT: ESP32** (C/Arduino o ESP-IDF), audio pregrabado/TTS en servidor.

### Estrategia iOS

- Captura: imposible en iPhone (limitación de Apple, sin workaround).
- Escucha: iPhone cubierto vía panel web desde el día 1; app iOS nativa de escucha
  solo si la demanda lo justifica.
- Dueño con iPhone y Yape personal: ofrecer "número Yape del negocio" en un equipo
  Android dedicado en la tienda (un Android barato puede incluirse como opción en
  el paquete de comodato).

### Soporte multi-billetera

Arquitectura de parser modular: cada billetera = `{ paquete Android, patrones regex,
plantilla de voz }`. Los patrones se descargan por **Firebase Remote Config**, nunca
van incrustados en el APK → si Yape cambia su texto o se agrega una billetera nueva,
se actualiza en el servidor sin republicar la app.

| Billetera | App(s) a escuchar | Prioridad |
|---|---|---|
| Yape | app Yape (BCP) | 1 — lanzamiento |
| Plin | apps de Interbank, BBVA y Scotiabank (Plin no es app propia) | 2 |
| Agora / BCP app / otras | según demanda | 3 |

Notas:
- Yape–Plin son interoperables desde 2023: un pago Plin al QR/número Yape llega como
  notificación de Yape → el soporte de Yape ya cubre esos cobros.
- **Modo aprendizaje**: notificaciones de apps financieras que no matchean ningún
  patrón se reportan al backend (con consentimiento) para descubrir formatos nuevos.
- La voz anuncia la billetera: "Te yapearon 20 soles de Juan" / "Pago Plin de 20 soles".

## 5. Modelo de cobro

- **Gratis:** 1 teléfono, anuncio de voz básico (gancho de entrada).
- **Premium app — S/ 9.90 a 14.90 /mes por comercio:**
  - Modo dueño remoto (multi-teléfono, hasta N trabajadores)
  - Historial de pagos y cierre de caja diario
  - Filtro anti-fake y código de seguridad
  - Voz/volumen personalizado
- **Membresía con parlante en comodato (fase 2) — S/ 19.90 a 29.90 /mes:**
  - Todo lo del premium + dispositivo de mostrador incluido a S/ 0 de entrada
  - Argumento de venta vs Izipay: "sin pagar S/ 129 y sin cambiar tu Yape"
  - Alternativa de venta directa del equipo (S/ 99–149) para quien la prefiera.

Meta de referencia: 300 comercios × S/ 12/mes ≈ S/ 3,600/mes recurrentes.

## 6. Presupuesto

### Fase 1 — MVP app (6–10 semanas)

| Concepto | Si lo hago yo | Si contrato |
|---|---|---|
| Desarrollo app Android + backend | S/ 0 | S/ 8,000 – 18,000 |
| Cuenta Google Play (pago único) | ~S/ 95 (US$ 25) | igual |
| Backend (Firebase, plan gratis al inicio) | S/ 0 | S/ 0 |
| Dominio + landing | ~S/ 100/año | igual |
| **Total arranque** | **< S/ 300** | **S/ 8,500 – 18,500** |

Costos operativos al crecer: Firebase/servidor ~S/ 100–300/mes recién con miles de usuarios.

### Fase 2 — IoT (solo con tracción)

**Decisión: NO fabricar — comprar "payment soundbox" OEM white-label.**

El dispositivo ya existe como producto genérico. Se llama **"payment soundbox"** /
**"QR payment soundbox"** / **"cloud speaker"**: parlante 4G/WiFi que anuncia pagos
por voz, el mismo tipo de equipo que usan Paytm en India e Izipay en Perú. Se compra
en Alibaba a fabricantes de Shenzhen (ej. modelos Aisino Q181, JHL510; proveedores
como Shenzhen Tousei) desde ~US$ 10–25/unidad, con MOQ desde 10 unidades.

Personalización OEM que ofrecen los fabricantes:
- Logo impreso en la carcasa (serigrafía/tampografía) — setup pequeño o gratis en MOQ 100+
- Color de carcasa, sonido de arranque y voz personalizados
- Caja/empaque con tu marca

**Pregunta clave al proveedor antes de comprar** (filtro eliminatorio): "¿el equipo
puede apuntar a MI servidor (API/MQTT propio) o solo funciona con la nube del
fabricante?" Se necesita integración con backend propio para el modo dueño-remoto.
Pedir 2–3 muestras de distintos fabricantes (~US$ 20–50 c/u) y probar la API antes
de la compra en volumen. ESP32 queda solo como plan B si ningún OEM da API abierta.

**Disponibilidad en Perú (verificado jul-2026):** no se venden soundboxes "libres"
localmente. El único hecho es el QR Parlante de Izipay, cerrado a su plataforma.
→ Ruta de compra: muestras sueltas por **AliExpress** (envío a Perú sin MOQ,
US$ 15–35 c/u, buscar "payment soundbox 4G"); lote con logo por **Alibaba**.
Ventaja estratégica: no existe en Perú un soundbox independiente del procesador
de pagos — espacio de mercado vacío.

| Concepto | Estimado |
|---|---|
| Muestras de 2–3 modelos OEM para evaluar | S/ 300 – 600 |
| Primera flota en comodato (100 unidades OEM con logo, US$ 12–20 c/u + flete) | S/ 5,500 – 9,000 |
| Impuestos de importación (IGV 18% + ad valorem) | ~20% del valor |
| **Homologación MTC** (obligatoria: equipo emite WiFi/4G) — tasa S/ 86.40 + gestión | S/ 100 – 1,500 |
| Permiso de internamiento (VUCE) + registro de casa comercializadora | trámite, costo menor |
| Empaque, logística local, merma | S/ 1,000 – 2,000 |
| Contrato de comodato (redacción con abogado) | S/ 300 – 800 |

Notas:
- La homologación MTC es por marca/modelo, una sola vez, certificado indefinido.
  Verificar antes de comprar si el modelo ya está homologado en Perú (plataforma MTC).
- En comodato la flota es inversión de capital, no venta — con membresía de
  S/ 19.90–29.90/mes cada equipo se paga solo en 2–3 meses. Presupuestar ~10% de
  merma anual (daños/pérdidas no recuperadas).

### MVP técnico (alcance mínimo)

1. App Android: permiso de notificaciones, parser de Yape/Plin/BCP, TTS en español.
2. Login simple (comercio) + rol dueño/trabajador.
3. Sync por Firebase Cloud Messaging: dueño → trabajadores en < 2 segundos.
4. Historial del día con total acumulado.

## 7. Riesgos

- Yape cambia el formato de notificaciones → parser roto (mantenimiento constante,
  diseñar parser tolerante + actualización remota de patrones).
- Yape/BCP lanza su propio anunciador o dispositivo oficial.
- Google Play restringe el permiso de lectura de notificaciones → declarar el caso
  de uso claramente en la ficha y el formulario de permisos.
- Legal: no usar logo de Yape ni decir "verificado por Yape"; dejar claro que se
  lee la notificación oficial del sistema. Cuidado con datos personales (nombres de
  pagadores) → cifrar y cumplir Ley de Protección de Datos Personales (Ley 29733).
