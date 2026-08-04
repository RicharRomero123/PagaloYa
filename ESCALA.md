# PagoYa — Cómo escalar: de leer notificaciones a certeza total

> Documento de estrategia de crecimiento. Fecha: 3 de agosto de 2026.
> Responde una sola pregunta: **cómo pasar del 80 % de precisión al 100 %, en
> Android y iOS, sin cobrar comisión al comerciante y sin dejar de ser PagoYa.**
> Complementa `PLAN.md` (negocio hoy), `MERCADO.md` (segmentos) y `ROADMAP.md` (fases).

---

## 1. La tesis

Hoy PagoYa depende de una sola fuente de verdad: el `NotificationListenerService`
de un teléfono Android. Esa fuente tiene tres fallas conocidas:

1. Xiaomi, Huawei, Oppo, Vivo y compañía matan procesos en segundo plano.
2. iOS no permite leer notificaciones de otras apps. Sin excepción, sin workaround.
3. Si Yape cambia el texto, el parser se cae.

La conclusión que mucha gente saca de esto es "hay que volverse pasarela". **Es la
conclusión equivocada.** El problema no es que PagoYa no mueva el dinero. El
problema es que PagoYa tiene **una sola fuente de datos, frágil y en el dispositivo
equivocado**.

> **La certeza es un problema de fuente de datos, no de modelo de negocio.**
> Se arregla moviendo la captura fuera del teléfono del comerciante y sumando
> fuentes redundantes — no metiendo la mano en el flujo del dinero.

Y esto importa comercialmente, porque el día que el dinero pasa por PagoYa,
la comisión deja de ser opcional: pasa a ser un costo estructural. La promesa
"0 % de comisión" **solo es sostenible si PagoYa nunca custodia fondos.**
Todo el resto del documento sale de esa frase.

---

## 2. Qué significa "100 %" (definición honesta)

Prometer que nunca se perderá un evento es mentira en cualquier sistema
distribuido. Lo que sí se puede prometer, y es lo que el bodeguero realmente
compra, son dos garantías:

| Garantía | Qué significa | Estado hoy |
|---|---|---|
| **Cero falsos positivos** | PagoYa nunca anuncia un pago que no ocurrió | ✅ Ya resuelto (regla de oro: los pagos nacen de notificaciones reales) |
| **Cero fallos silenciosos** | Si PagoYa no puede ver los pagos, **lo dice en voz alta** en vez de callarse | ❌ Este es el hueco del 20 % |

El 80 % actual no duele porque se pierda un pago. Duele porque **el comerciante no
sabe que lo perdió**. Un sistema que dice "estoy ciego, no confíes en mí ahora
mismo" es percibido como 100 % confiable aunque técnicamente falle.

> **El eslogan tiene que evolucionar:**
> hoy es *"si no suena, no te pagaron"*.
> Mañana es *"si no suena, no te pagaron. Y si yo no puedo escuchar, te aviso."*

Eso es lo primero que hay que construir, cuesta poco y sube la percepción de
precisión más que cualquier integración.

---

## 3. La escalera de fuentes de certeza

No hay una solución mágica: hay una escalera. Cada peldaño se puede subir sin
romper el anterior, y varios pueden convivir.

| # | Fuente | Precisión | Plataforma | Costo | Requiere |
|---|---|---|---|---|---|
| 0 | Notificación en el teléfono del dueño | ~80 % | Android | S/ 0 | nada — **es lo de hoy** |
| 1 | Notificación + guardián + latido (heartbeat) | ~90 % percibido 99 % | Android | S/ 0 | solo software |
| 2 | **Capturador PagoYa** (equipo dedicado) | ~99 % | Android (equipo propio) | hardware | comodato |
| ~~3~~ | ~~Correo/SMS de la billetera → parseado en el servidor~~ | — | — | — | **DESCARTADO** (ver abajo) |
| 4 | Redundancia: dos fuentes confirmando el mismo pago | ~99.9 % | cualquiera | S/ 0 | tener 2 de las anteriores |
| 5 | API oficial de la billetera / cuenta negocio | 100 % | cualquiera | acuerdo comercial | negociación |
| 6 | QR propio de PagoYa sobre un PSP | 100 % | cualquiera | **comisión por transacción** | contrato PSP |
| 7 | Billetera propia (EEDE) | 100 % | cualquiera | capital + licencia | SBS |

> **Peldaño 3 descartado (verificado agosto 2026):** Yape, Plin y las demás
> billeteras **no emiten correo ni SMS por cobro recibido**. Solo push. No hay
> fuente de servidor que capturar. La ruta "parsear correo" no existe.

**Esto cambia la recomendación: subir 1 → 2 → 4.** Sin el peldaño 3, el
**Capturador PagoYa deja de ser una opción y pasa a ser el único camino real al
100 % y a iOS** sin depender de un acuerdo con la billetera. Los peldaños 1 y 4
son software puro; el 2 es el que de verdad rompe el techo.

---

## 4. El cambio arquitectónico que habilita todo: capa de ingesta

Hoy el teléfono **es** el sistema. Hay que degradarlo a **una fuente entre varias**.

```
   FUENTES DE INGESTA (intercambiables)
   ┌──────────────────────────────────────────────────┐
   │  A. App Android (NotificationListener)           │
   │  B. Capturador PagoYa (equipo dedicado) ← clave  │
   │  C. Webhook de PSP / API de billetera (futuro)   │
   │  D. Registro manual del comerciante (sin verificar)│
   └───────────────────────┬──────────────────────────┘
                           │  evento crudo normalizado
                           ▼
              ┌────────────────────────────┐
              │   NÚCLEO PagoYa             │
              │  · normaliza                │
              │  · deduplica (monto+hora+   │
              │    últimos dígitos+emisor)  │
              │  · puntúa confianza         │
              │  · vigila el latido         │
              └─────────────┬───────────────┘
                            │ evento con nivel de confianza
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
   App Android         App iOS / Web        Parlante IoT
   (voz)               (voz + push)         (voz)
```

Tres consecuencias enormes de este solo cambio:

1. **iOS deja de ser un problema.** Cualquier fuente que no viva en el iPhone del
   dueño (B, C) funciona igual para él. La app iOS solo escucha.
2. **Agregar una billetera o un país es agregar una fuente**, no reescribir la app.
3. **La redundancia se vuelve gratis.** Dos fuentes que reportan el mismo pago = 100 %.

Encaja con lo que ya decidiste: los patrones viven en Remote Config, no en el APK.
Esto es lo mismo, un nivel más arriba.

---

## 5. Ruta A — Certeza sin tocar el dinero (la recomendada)

### 5.1 Latido y modo ciego (peldaño 1) — hacer ya, cuesta días

- El capturador manda un latido cada 60 s.
- Si el backend deja de recibirlo, **todos los dispositivos escuchando cambian a
  MODO CIEGO**: banner rojo, "PagoYa no está escuchando el Yape del negocio —
  verifica manualmente".
- Alerta hablada y push al dueño: *"Compadre, dejé de escuchar tu Yape hace 3 minutos."*
- Se suma al **Guardián de Yape** que ya está en `ROADMAP.md` (autostart por
  marca, detección de Yape detenido, watchdog de horas sin pagos).

Esto no sube la precisión técnica. Sube la **confianza**, que es lo que se vende.
Es la mejora con mejor relación esfuerzo/valor de todo el documento.

### 5.2 ~~Correo del banco parseado en el servidor~~ — DESCARTADO

**Verificado (agosto 2026): las billeteras peruanas no mandan correo ni SMS por
cobro recibido. Solo notificación push.** No hay nada que reenviar ni que parsear
en el servidor. La ruta queda cerrada.

Consecuencia estratégica, y es importante: **no existe ningún atajo de software
puro hacia iOS ni hacia el 100 %.** Todo camino pasa por (a) controlar el equipo
que captura, o (b) un acuerdo con quien emite el pago. Como (b) no depende de ti,
**(a) es el plan**. Eso convierte al Capturador de "buena idea" en **la columna
vertebral del producto**.

Único residuo que vale una tarde de prueba, no más: las **alertas de movimiento de
cuenta bancaria** que sí se configuran por correo en la banca por internet
(transferencias interbancarias, no yapeos). Cubre un caso de uso distinto y menor.

### 5.3 El Capturador PagoYa (peldaño 2) — el que de verdad rompe el 80 %

El 20 % que se pierde no es culpa del código: es culpa de que el capturador es
**el teléfono personal del dueño**, con 200 apps, batería al 4 %, MIUI matando
servicios y el dueño llevándoselo a su casa.

La solución no es más código. Es **cambiar de equipo**:

```
   Hoy                          Mañana
   ────────────────────         ─────────────────────────────
   Yape del negocio en          Yape del negocio en el
   el celular personal          CAPTURADOR PagoYa:
   del dueño                    · Android barato dedicado
                                · solo Yape + PagoYa
                                · enchufado 24/7 al mostrador
   → lo apaga, se lo lleva,     · sin apps que lo maten
     se queda sin batería,      · configurado por PagoYa
     MIUI mata el servicio      → nunca se apaga, nunca falla
```

Esto ya estaba insinuado en `PLAN.md` ("un Android barato puede incluirse en el
paquete de comodato"). **Hay que ascenderlo de nota al pie a producto central.**

Resuelve de golpe, sin depender de nadie:

- El fabricante que mata procesos → el equipo lo elige PagoYa.
- **El dueño con iPhone** → su Yape personal se queda en el iPhone; el Yape del
  negocio vive en el Capturador. El iPhone solo escucha. *Problema iOS resuelto
  sin API de Apple.*
- El taxista / motorizado cuya cuenta es de la esposa → el Capturador se queda en
  casa enchufado y él escucha desde donde esté (ver `MERCADO.md` §4.3).
- Y es el mismo movimiento del parlante en comodato de la fase 3: **el parlante y
  el capturador pueden ser el mismo aparato** si el soundbox OEM corre Android.

Costo: un Android de entrada ronda S/ 250–350 en Perú, o un soundbox Android OEM.
En comodato con plan Patrón se recupera en 8–12 meses — más lento que el parlante
solo, así que conviene como **plan anual o con garantía inicial**, no regalado.

### 5.4 Redundancia y puntaje de confianza (peldaño 4)

Con dos fuentes activas, cada pago llega con un nivel:

| Confianza | Cuándo | Qué muestra la app |
|---|---|---|
| **Confirmado** | 2+ fuentes coinciden | ✅ verde, voz normal |
| **Probable** | 1 fuente, sistema sano | 🔵 azul, voz normal |
| **Sin verificar** | reportado a mano, sin fuente | ⚠️ ámbar, no se anuncia como pago |
| **Ciego** | sin latido del capturador | 🔴 rojo, "no puedo confirmar" |

La deduplicación se hace por `(monto, ventana de tiempo ±3 min, nombre/últimos
dígitos, billetera)`. Es la pieza técnica central de la capa de ingesta y hay que
diseñarla bien desde el principio, porque retrofitearla después duele.

---

## 6. Ruta B — QR propio sobre un PSP (cuándo sí, y por qué no todavía)

Emitir un QR de PagoYa apoyado en un PSP/adquirente (Izipay, Niubiz, Culqi,
Mercado Pago, Openpay) da el 100 % de certeza porque **la orden nace en tu sistema**.

Pero tiene un costo por transacción, y ahí se rompe la promesa. Los números:

- Si el costo del PSP fuera ~1 % y el plan Patrón es S/ 24.90/mes, la suscripción
  solo cubre **~S/ 2,500 de volumen mensual**.
- Una bodega chica mueve bastante más que eso por Yape.
- Conclusión: **con un plan plano barato, absorber la comisión no cierra.**

Formas honestas de ofrecerlo sin romper el "0 %":

1. **0 % hasta un tope de volumen** incluido en el plan, y de ahí en adelante costo
   al costo, sin margen. ("PagoYa no gana un sol de tus cobros.")
2. **Pass-through puro**: el comerciante paga la tarifa del PSP, PagoYa cero margen.
3. **Módulo opcional** para quien quiera tarjeta y links de pago — que la Ruta A
   nunca cubrirá, porque ahí sí hay que procesar.

**Cuándo activarla:** cuando haya volumen para negociar tarifas y cuando un
segmento lo pida (restaurantes con tarjeta, e-commerce chico, transporte por flota).
Nunca como camino principal de la certeza.

---

## 7. Ruta C — Billetera propia (por qué no, hoy)

El puente que describiste — el dinero entra a PagoYa y sale a la cuenta del
comercio — **funciona técnicamente y es exactamente lo que regula la ley.**
En Perú, emitir dinero electrónico o custodiar fondos de terceros cae bajo la
Ley 29985 y la supervisión de la SBS: sociedad constituida para ese fin, capital
mínimo, fideicomiso de los fondos, KYC, prevención de lavado, oficial de
cumplimiento, auditoría y reportes.

No es "difícil": es **otra empresa**. Es la meta a 3–5 años con tracción y capital,
no el atajo para arreglar un parser.

> Regla práctica: **si el dinero se detiene un segundo en tu cuenta, ya no eres
> una app, eres una entidad financiera.** Y de paso pierdes el "0 % de comisión",
> porque el costo de compliance hay que pagarlo con algo.

---

## 8. iOS, resuelto sin pelear con Apple

| Rol | iPhone | Cómo |
|---|---|---|
| **Escuchar** | ✅ desde el día 1 | App iOS nativa que recibe push (FCM/APNs) y habla con `AVSpeechSynthesizer`. Voz, historial, caja, reportes: todo igual que Android. |
| **Capturar** | ❌ imposible en el propio iPhone | Pero **sí** vía Capturador PagoYa (§5.3) o fuente de servidor (§5.2) |

O sea: **"PagoYa en iPhone" es cierto hoy mismo** para el 100 % de las funciones
que le importan al dueño que no está en el local, y el Capturador cierra el resto.
La única regla de comunicación: no prometer "lee tus notificaciones en iPhone" —
prometer *"tu iPhone te avisa"*, que es verdad y es lo que compran.

Orden sugerido: panel web (ya en el plan) → app iOS de escucha cuando haya ≥ 50
comercios pidiéndola. Antes de eso es esfuerzo mal gastado.

---

## 9. Por qué el 0 % de comisión es sostenible (los números)

Costo real de un comercio por mes en la Ruta A:

| Concepto | Costo/comercio/mes |
|---|---|
| Firestore + FCM (≈1,500 pagos/mes, fan-out a 3 dispositivos) | < S/ 0.10 |
| Soporte (1 persona cada ~800 comercios) | ~S/ 1.90 |
| **Costo de cobrar la suscripción** ← el asesino silencioso | ver abajo |

**El costo por transacción del comerciante es cero, porque PagoYa no procesa
transacciones.** El dinero va directo de cliente a comerciante, como siempre.
PagoYa vende **certeza y coordinación**, no procesamiento. Por eso la promesa
"0 % de comisión, siempre" es estructural y no una promoción — y por eso es un
arma contra Izipay que ellos no pueden copiar sin canibalizarse.

**El costo que sí duele es cobrar la mensualidad.** Con tarifa típica de pasarela
(≈4 % + S/ 1.20 fijo), cobrar S/ 9.90 con tarjeta se lleva ~S/ 1.60: **16 % del
ingreso**. Cobrar S/ 99 anuales se lleva ~5 %. De ahí tres decisiones:

1. **Empezar cobrando por Yape/efectivo con activación manual desde el panel**
   (ya está en `ROADMAP.md`): costo de cobro ≈ 0 %.
2. **Empujar el plan anual con descuento** — baja el costo de cobro y mata el churn.
3. **Cobro recurrente automático solo cuando el volumen lo justifique.**

Contribución por comercio en plan Caserito (S/ 9.90), cobrando por Yape:
≈ **S/ 7.90/mes**. Punto de equilibrio de una operación de una persona:
**~400–500 comercios pagando.** Con 1,000 comercios el software se paga solo
diez veces. **Nada de esto necesita comisiones.**

### El plan mensual mínimo sostenible

| Plan | Precio | Para quién | Qué incluye |
|---|---|---|---|
| **Gratis** | S/ 0 | taxista, mototaxi, ambulante | 1 dispositivo, voz, sin historial. Motor de adopción y de marca (`MERCADO.md` §4.3) |
| **Caserito** | S/ 9.90/mes o S/ 99/año | bodega, puesto de mercado | multi-dispositivo, historial, caja, hora pico, modo ciego |
| **Patrón** | S/ 24.90/mes | negocio con personal, delivery | + parlante/Capturador en comodato, roles, conciliación, multi-sucursal |

**S/ 9.90 es el piso.** Por debajo, el costo de cobrar y de dar soporte se come el
margen y el negocio deja de ser negocio. Y no hace falta bajar de ahí: contra los
S/ 129 de un QR Parlante de Izipay más su comisión, S/ 9.90 sin comisión no
compite por precio — compite por evidencia.

---

## 10. Yape Empresa: el competidor que te acaba de poner precio

Yape lanzó **Yape Empresa**, y hay que mirarlo de frente porque incluye lo que
hasta ayer era el diferenciador de PagoYa:

| Lo que ofrece Yape Empresa | ¿Colisiona con PagoYa? |
|---|---|
| **Hasta 5 ayudantes validando ventas en tiempo real** | ⚠️ **Sí. Es el modo dueño-remoto.** |
| Reportes de ventas y movimientos, 90 días | ⚠️ Sí, parcial |
| Recibe yapeos de hasta S/ 999.99 por pago | No |
| Atención por WhatsApp | No |
| **Costo: 2.95 % + S/ 0.29 por cobro** | ✅ **Aquí está tu negocio entero** |
| **Requisito: RUC + cuenta o tarjeta de negocio** | ✅ **Aquí está tu mercado cautivo** |

### 10.1 El fijo de S/ 0.29 es la grieta

En tickets de bodega, el cargo fijo domina y la comisión efectiva se dispara:

| Ticket promedio | Comisión efectiva real |
|---|---|
| S/ 5 | **8.75 %** |
| S/ 10 | **5.85 %** |
| S/ 20 | 4.40 % |
| S/ 50 | 3.53 % |
| S/ 100 | 3.24 % |

Bodega típica: 40 yapeos/día de S/ 15 promedio → S/ 18,000/mes, 1,200 cobros.

```
   Yape Empresa   2.95% × 18,000  = S/ 531
                  1,200 × S/ 0.29 = S/ 348
                                    ─────────
                                    S/ 879 / mes

   PagoYa Caserito                  S/ 9.90 / mes
                                    ─────────
                                    89 veces más barato
```

**Punto de equilibrio: S/ 170–280 de venta mensual** (según ticket), o sea unos
**S/ 7 al día**. Cualquier negocio que exista ya está muy por encima. No hay un
solo comercio real para el que Yape Empresa salga más barato que PagoYa.

### 10.2 La lectura correcta: esto es una validación, no una amenaza

Credicorp acaba de confirmar, con precio de mercado, que **la validación
multi-dispositivo en tiempo real vale dinero**. Ese era el punto que había que
demostrarle a un inversionista o a un comerciante escéptico. Ya está demostrado.

> **El pitch se escribe solo:**
> *"Lo mismo que te da Yape Empresa, sin que te descuenten de cada venta."*
> Y en la landing: la calculadora "cuánto te cobraría Yape Empresa este mes" es,
> probablemente, la mejor pieza de conversión que puede tener PagoYa.

### 10.3 Lo que sí cambia en la estrategia

1. **"Multi-teléfono en tiempo real" deja de ser el diferenciador** y pasa a ser
   la mesa de juego. El diferenciador nuevo es: **0 % de comisión + voz +
   multi-billetera + funciona con la cuenta que ya tienes.**
2. **Toda la comunicación se reordena alrededor del costo**, no de la función.
3. **Multi-billetera sube de prioridad.** Yape Empresa solo ve Yape y **jamás va a
   anunciar un Plin**. Un cierre de caja único con Yape + Plin + efectivo es
   territorio que ellos no pueden pisar por definición.

### 10.4 El requisito de RUC parte el mercado en dos

**Verificado (ago-2026): Yape Empresa exige RUC y una cuenta o tarjeta de negocio
vinculada.** Eso no es un detalle administrativo: es una frontera que deja fuera a
la mitad del Perú que cobra por billetera, y define dos defensas de naturaleza
distinta.

| Segmento | ¿Tiene RUC? | ¿Yape Empresa lo alcanza? | Defensa de PagoYa |
|---|---|---|---|
| Bodega formal | sí | **Sí** | **Económica**: 89× más barato |
| **Bodega informal** | no | **No** | **Estructural**: no puede comprarlo |
| Restaurante/botica con motorizados | sí | **Sí** | **Económica** + multi-billetera |
| **Taxista / mototaxista** | casi nunca | **No** | **Estructural** |
| **Ambulante, feria, puesto de mercado** | no | **No** | **Estructural** |
| Cobrador de combi | el dueño de la unidad, a veces | Parcial | Mixta |
| Peluquería, taller, lavandería de barrio | mixto | Parcial | Mixta |

Tamaño de lo estructuralmente cautivo, con las cifras de `MERCADO.md`:

- **Bodegas sin formalizar: entre 121,000 y 321,000.** (Las fuentes no concuerdan:
  ~414,000 formales vs. ~535,000 totales con 60 % informales. La contradicción está
  en `MERCADO.md` §3 y sigue sin resolverse — usar el rango, nunca un número solo.)
- **Mototaxistas: 300,000 a 600,000**, estimación poco confiable por la informalidad.
- Más ambulantes, ferias y puestos de mercado, sin cifra confiable.

Fácilmente **medio millón de unidades económicas a las que Yape Empresa no le puede
vender aunque quiera.** Ahí PagoYa no compite: es la única opción que existe.

> **El argumento de venta que esto habilita, y que vale más que el precio:**
> *"PagoYa no te pide RUC."*
> Para un informal eso no es una comodidad, es la diferencia entre poder y no poder.

**La tensión honesta, que hay que decir en voz alta:** el mercado estructuralmente
cautivo es también **el que menos puede pagar** y el más caro de cobrar — es
exactamente el perfil que `MERCADO.md` §4.3 marca como "disposición a pagar baja".
Y el mercado que sí paga con gusto — bodega formal, restaurante con motorizados —
es justo donde Yape Empresa **sí** puede competir.

De ahí que la estrategia tenga que ser doble, no única:

| | Formales (con RUC) | Informales (sin RUC) |
|---|---|---|
| **Cómo se gana** | por costo: S/ 9.90 vs ~S/ 879 | por existencia: no hay alternativa |
| **Plan** | Caserito / Patrón, mensual o anual | Gratis → anual barato |
| **Rol en el negocio** | **de aquí sale la caja** | **de aquí sale el volumen y la marca** |
| **Cobro** | tarjeta o anual | Yape/efectivo, o anual único |

No son dos productos: es el mismo producto con dos discursos y dos formas de cobrar.

### 10.5 El riesgo existencial real (y cómo cubrirse)

No es que Yape Empresa exista. Son dos movimientos concretos, en este orden de gravedad:

| # | Movimiento de Yape | Impacto | Probabilidad hoy |
|---|---|---|---|
| 1 | **Quitar el requisito de RUC** | Destruye la defensa estructural: medio millón de informales dejan de ser cautivos | Baja-media. Yape Empresa **es** su embudo de formalización; abrirlo lo desarma |
| 2 | **Regalar los ayudantes en la cuenta personal** | Destruye el core: la validación multi-dispositivo pasa a ser gratis | Baja. El 2.95 % *es* su forma de monetizar comercios |

Los dos juntos serían terminales. Pero ya movieron el piso una vez con la
interoperabilidad Yape–Plin, así que hay que asumir que puede pasar y tener
construido lo que no depende de ellos.

**Coberturas (ninguna es nueva; todas suben de prioridad):**

| Cobertura | Por qué Yape no puede replicarla |
|---|---|
| **Multi-billetera + efectivo en un solo cierre** | Es un competidor de Plin, no va a integrarlo. **Sobrevive a los dos movimientos.** |
| **Voz en el mostrador y parlante físico** | No va a repartir hardware a 500,000 bodegas. **Sobrevive a los dos movimientos.** |
| **Capa de operación**: caja, hora pico, roles, conciliación, multi-sucursal | Es un producto de software distinto, no una función de billetera. **Sobrevive a los dos.** |
| **Sin RUC**: taxista con el Yape de la esposa, ambulante, bodega informal | Depende de que Yape mantenga el requisito. **Cae con el movimiento 1.** |
| **Precio 0 %** | Depende de que Yape mantenga la comisión. **Cae con el movimiento 2.** |

Léase así: las tres primeras son el foso real; las dos últimas son ventaja
prestada. **Hay que usar la ventaja prestada para crecer rápido y el tiempo que
compra para construir las tres primeras.**

**Métrica de vigilancia (trimestral):** precio, requisito de RUC y tramos gratuitos
de Yape Empresa. Si cae el RUC o aparece un tramo sin comisión, no competir de
frente: reposicionar hacia multi-billetera, operación y hardware.

---

## 11. El foso: qué hace difícil copiar a PagoYa

El QR se copia en una semana. Lo que no se copia:

1. **La red de capturadores instalados.** Cada equipo en un mostrador es un nodo
   que un competidor tendría que salir a reemplazar físicamente.
2. **El corpus de patrones.** El modo aprendizaje convierte a cada comercio en un
   sensor: cada formato nuevo de cada billetera y cada banco alimenta Remote
   Config y mejora a todos los demás. Es un activo que crece solo y con el tiempo
   se vuelve imposible de alcanzar desde cero.
3. **La operación del negocio, no el cobro.** Caja, hora pico, cierre, roles,
   conciliación. El comerciante cambia de procesador sin dolor; no cambia el
   sistema donde tiene su historial y a su gente.
4. **La marca de la confianza.** "Si no suena, no te pagaron" es una posición
   mental. Izipay vende procesamiento; PagoYa vende que no te estafen.

---

## 12. Fases y compuertas de decisión

Cada fase se abre **solo** si la anterior cumplió su métrica. Nada se construye antes.

| Fase | Qué se construye | Compuerta para pasar a la siguiente |
|---|---|---|
| **E0 — hoy** | Producto de `ROADMAP.md` fase 1, **con el modo ciego incluido desde el inicio** (latido, Guardián, confianza por pago). Es barato y hace confiable la propia beta | 10 comercios usándolo 3 semanas |
| **E1 — Garantía de Transparencia** | Poner el modo ciego en la landing, la app y el discurso de venta. Saturar el Mercado Modelo | 30 % de un mercado, churn < 8 % |
| **E2 — Capturador** | Android dedicado / soundbox Android en comodato ← **adelantado: sin ruta de correo, es el único camino al 100 %** | 200 comercios, churn < 5 %/mes |
| **E3 — Multiplataforma** | Capa de ingesta multi-fuente + app iOS de escucha | 30 % de los comercios con Capturador o 2ª fuente |
| **E4 — Integración oficial** | Conversación con billeteras/PSP desde una posición de volumen | 1,000 comercios activos |
| **E5 — Módulo pagos** | QR propio sobre PSP, 0 % hasta tope de volumen | demanda explícita de tarjeta/links |
| **E6 — Infraestructura** | Evaluar licencia o alianza profunda | escala y capital que hoy no existen |

**Métricas norte, en este orden:** comercios activos semanales → % que pasa de
gratis a pago → churn mensual → pagos capturados por comercio (proxy de precisión
real) → minutos en modo ciego por comercio (proxy inverso de confiabilidad).

Esa última métrica es la que hay que perseguir: **minutos ciegos por comercio al
mes.** Si tiende a cero, la precisión es 100 % en la práctica y en la percepción.

---

## 13. Qué NO hacer

- ❌ **No custodiar dinero** para arreglar un problema de datos. Es cambiar un bug
  por una licencia de la SBS.
- ❌ **No competir de frente con Izipay en procesamiento.** Tienen adquirencia,
  bancos detrás y músculo comercial. Compite en la capa de arriba.
- ❌ **No prometer que el iPhone lee notificaciones.** Es falso y se cae en la
  primera demo. Prometer "tu iPhone te avisa".
- ❌ **No bajar de S/ 9.90** para ganar volumen. El volumen sin margen es un trabajo
  no pagado con soporte incluido.
- ❌ **No pelear con Yape Empresa por funcionalidad.** Ellos ganan esa pelea con
  distribución. La pelea es por **costo** y por **multi-billetera**, donde no
  pueden seguirte.
- ❌ **No esconder los fallos.** Callar cuando el sistema está ciego destruye lo
  único que se está vendiendo. El modo ciego es una función, no una vergüenza.
- ❌ **No construir E3–E6 antes de tiempo.** Cada uno tiene su compuerta.

---

## 14. Por verificar (pendientes con dueño y fecha)

Estas son suposiciones del documento, no hechos. Ninguna debe usarse en la landing
ni en una decisión de gasto hasta confirmarse:

- [x] ~~**Correos de cobro**~~ → **VERIFICADO NEGATIVO (ago-2026):** las billeteras
      no emiten correo ni SMS por cobro, solo push. Ruta cerrada. Consecuencia:
      el Capturador (E3) sube de prioridad y adelanta a E2.
- [x] ~~**¿Existe Yape para negocios?**~~ → **SÍ: Yape Empresa**, con 5 ayudantes en
      tiempo real, reportes de 90 días y **costo de 2.95 % + S/ 0.29 por cobro**.
      Analizado en §10. **No tiene API pública conocida** — confirmar.
- [x] ~~**¿Yape Empresa exige RUC?**~~ → **SÍ, verificado (ago-2026):** requiere RUC
      **y** una cuenta o tarjeta de negocio vinculada. Deja estructuralmente fuera a
      ~500,000 unidades económicas informales. Analizado en §10.4.
- [ ] **Yape Empresa, letra chica restante** (lo más urgente del documento):
      - ¿la comisión aplica a **todo** cobro recibido o solo a los del QR de cobro?
      - ¿los 5 ayudantes ven el pago en la app Yape, o hay aviso sonoro?
      - ¿existe algún tramo o plan sin comisión?
      → Se resuelve dándose de alta con una cuenta real. **Define el copy de venta.**
- [ ] **Tarifas reales de PSP** en Perú para QR de billetera vs tarjeta, con y sin
      volumen. → define si la Ruta B es viable algún día.
- [ ] **Soundbox OEM con Android** (no solo firmware cerrado) que permita correr una
      app propia. → define si Capturador y parlante son un solo aparato o dos.
- [ ] **Costo real de cobro recurrente** en Perú para tickets de S/ 9.90 (Culqi,
      Mercado Pago, débito automático, Yape recurrente). → define el precio piso.
- [ ] **Marco legal exacto** (Ley 29985, SBS) para cualquier variante que toque
      fondos, incluso "por un segundo". → consulta con abogado antes de E5.

---

## 15. Resumen en cinco líneas

1. El 20 % que falta **no se arregla moviendo dinero**, se arregla moviendo la
   captura fuera del teléfono personal. Y como **no existe la ruta del correo**,
   el **Capturador PagoYa es el único camino** al 100 % y a iOS.
2. **100 % = nunca inventar un pago + nunca callar cuando estás ciego.** El modo
   ciego es la mejora más barata y más valiosa del roadmap.
3. **iOS ya está resuelto**: escucha desde el día 1; captura vía Capturador PagoYa.
4. **Yape Empresa no te destruye: te pone precio y te deja medio mercado.** Cobra
   2.95 % + S/ 0.29 (5–9 % reales en ticket de bodega, ~S/ 879/mes contra tus
   S/ 9.90) **y exige RUC**, lo que deja fuera a ~500,000 informales que no pueden
   comprarlo aunque quieran. Tu diferenciador se muda de "multi-teléfono" a
   **"sin RUC, sin comisión y multi-billetera"**.
5. **0 % de comisión es sostenible solo si el dinero nunca pasa por PagoYa.**
   S/ 9.90 es el piso, el negocio cierra en 400–500 comercios, y el activo
   defendible es la red de capturadores más el corpus de patrones.
