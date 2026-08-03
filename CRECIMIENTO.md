# PagoYa — Plan de crecimiento

> Cómo se pasa de 0 a 2,000 comercios. Fecha: 3 de agosto de 2026.
> Este documento responde **tres cosas**: cuál es el factor diferencial, cuál es
> la ventaja competitiva defendible, y cómo se convierte el "100 % de aviso" en
> algo que el comerciante pueda comprar.
>
> Complementa: `ESCALA.md` (cómo se logra el 100 % técnico), `MERCADO.md`
> (a quién), `REDES.md` (contenido orgánico), `PANEL.md` (mecánica de resellers).

---

## 1. El factor diferencial

Todo el mundo compite por **cobrar**. Yape, Plin, Izipay, Niubiz, Culqi, Mercado
Pago: todos quieren ser el riel por donde pasa la plata, y todos cobran por serlo.

**PagoYa no compite por cobrar. Compite por avisar.**

```
   ┌──────────────────────────────────────────────┐
   │  CAPA DE COBRO   Yape · Plin · efectivo ·    │  ← ellos pelean aquí
   │                  tarjeta · lo que sea         │     y cobran comisión
   └──────────────────────────────────────────────┘
                          ▲
   ┌──────────────────────────────────────────────┐
   │  CAPA DE CERTEZA   ¿me pagaron? ¿cuánto?      │  ← PagoYa vive aquí
   │  PagoYa            ¿ya entró? ¿quién lo vio?  │     nadie la ocupa
   └──────────────────────────────────────────────┘
```

De esa sola decisión salen las tres promesas, y no son eslóganes: son
consecuencias estructurales de no tocar el dinero.

| Promesa | Por qué se puede sostener |
|---|---|
| **Sin comisión** | El dinero nunca pasa por PagoYa. No hay costo por transacción que trasladar. |
| **Sin RUC** | No se emite comprobante ni se procesa nada. No hay nada que formalizar. |
| **Con la cuenta que ya tienes** | No hay que migrar de billetera ni de procesador. |

> **La frase de posicionamiento:**
> ### Cobra como quieras. Entérate siempre.
>
> Convive con la que ya existe — *"si no suena, no te pagaron"* — pero esa es la
> promesa **anti-fake**; esta es la promesa **de cobertura**. Se necesitan las dos.

**En la landing (agosto 2026):** este apartado está implementado como la sección
`#por-que-sin-comision` de la home — las dos capas dibujadas, las tres promesas
explicadas como consecuencias y la frase de posicionamiento como remate — más la
guía `/yape-comision-negocios/`, que captura la búsqueda comercial de mayor
intención del mapa. La tarjeta «Yape Empresa» se sumó a la comparación de la home.
Las dos garantías de §2 **no** están publicadas todavía: ver `landing/SEO.md`.

---

## 2. La Garantía PagoYa: cómo se vende el 100 %

El 100 % no se vende diciendo "somos 100 % precisos". Nadie te cree y además no es
comprobable. Se vende **poniendo plata detrás**. Nadie en este mercado lo hace.

Va en dos tiempos, y el orden importa porque prometer el segundo antes de tiempo
te quiebra.

### 2.1 Garantía de Transparencia — **se puede lanzar ya**

> **"PagoYa te avisa cuando no puede avisarte."**

Si el capturador deja de escuchar, todos los dispositivos entran en **modo ciego**:
banner rojo, alerta hablada, push al dueño. El comerciante nunca opera creyendo
que está cubierto cuando no lo está.

- **Costo de implementarla:** días de trabajo (§5.1 de `ESCALA.md`).
- **Por qué convierte:** el comerciante no compra precisión, compra *no llevarse
  una sorpresa*. Un sistema que admite cuando falla se percibe más confiable que
  uno que promete perfección.
- **Nadie más la ofrece.** Yape Empresa no te dice si tu ayudante tiene el celular
  apagado.

### 2.2 Garantía de Aviso — **solo después del Capturador**

> **"Si te cayó un Yape y PagoYa no te avisó, ese mes no lo pagas."**

Esta es el arma pesada. Convierte tu debilidad histórica (el 80 %) en la razón
para comprarte.

**No se lanza hasta tener:** modo ciego + Capturador PagoYa + cierre de caja que
permita detectar el faltante. Con 80 % de precisión, esta garantía te arruina.
Con el Capturador, el reclamo es raro y el costo está acotado a S/ 9.90.

**Condiciones que la hacen viable** (hay que redactarlas desde el día uno):

- Aplica sobre el equipo capturador entregado o configurado por PagoYa.
- No aplica si el equipo está apagado, sin internet o sin los permisos activos
  — y el modo ciego ya deja registro de esos periodos, así que es verificable.
- Un reclamo por comercio por trimestre. Si hay más, el problema es la
  instalación y va soporte, no reembolso.

**Costo real esperado:** con 99 % de precisión y 1,000 comercios, incluso si el
2 % reclama un mes al trimestre, son ~S/ 200 al trimestre. Es presupuesto de
marketing, no un pasivo.

---

## 3. Las cuatro capas de ventaja competitiva

Ordenadas de la más frágil a la más durable. **La estrategia es usar las frágiles
para crecer rápido y el tiempo que compran para construir las durables.**

| # | Ventaja | Contra quién | Durabilidad | Muere si… |
|---|---|---|---|---|
| 1 | **Precio: 0 % de comisión** | Yape Empresa (2.95 % + S/ 0.29) | ⚠️ Prestada | Yape regala los ayudantes |
| 2 | **Sin RUC** | Yape Empresa | ⚠️ Prestada | Yape quita el requisito |
| 3 | **Multi-billetera + efectivo en un solo cierre** | Todos | ✅ Durable | Nadie: Yape no va a integrar a Plin |
| 4 | **La caja física que habla** (parlante/Capturador) | Todos | ✅ Durable | Nadie reparte hardware a 500,000 bodegas |
| 5 | **La capa de operación** (caja, hora pico, roles, conciliación) | Todos | ✅✅ La más durable | Es otro producto, no una función de billetera |

**Traducción a calendario:**

```
  Hoy ─────────────────────► 12 meses ─────────────────► 24 meses
  Vender con 1 y 2          Construir 3 y 4            Vivir de 5
  (precio y sin RUC)        (multi-billetera,          (el comerciante ya no
  Crecer rápido             hardware)                   se puede mudar)
```

El error fatal sería crecer con 1 y 2, quedarse cómodo, y que Yape mueva el piso
antes de que 3, 4 y 5 estén listos.

---

## 4. Los cuatro motores de crecimiento

Un motor no es un canal: es algo que hace que **crecer produzca más crecimiento**.
PagoYa tiene cuatro, y el primero es raro y muy suyo.

### 🔊 Motor 1 — El sonido (el que ningún competidor puede copiar)

El producto **hace ruido en el espacio público**. Cuando un puesto de mercado
anuncia *"¡PagoYaaa! Te yapearon 25 soles"*, lo escuchan los seis puestos vecinos
y todos los que pasan.

```
   Un puesto instala PagoYa
            │
            ▼
   El anuncio suena en el pasillo
            │
            ▼
   3 vecinos preguntan "¿qué es eso?"
            │
            ▼
   El propio comerciante lo explica  ← venta gratis, hecha por el cliente
            │
            ▼
   1 instala ──────────► y vuelve a empezar
```

**Esto es publicidad involuntaria y continua, incluida en el producto.** Ninguna
app silenciosa la tiene. Yape Empresa, que muestra el pago en la pantalla del
ayudante, no hace ruido y por lo tanto no se propaga.

Cómo se amplifica:
- **El sticker del mostrador** (ya en `REDES.md` §11): cuando preguntan, hay dónde
  mirar. Es el único activo de marca que vive en el punto de venta.
- **La voz dice la marca.** El anuncio empieza con "¡PagoYa!" — no es vanidad, es
  el mecanismo de propagación.
- **Densidad** (§5): el motor solo prende si hay varios puestos en el mismo lugar.

**Métrica del motor:** altas nuevas por mercado / altas existentes en ese mercado.
Si es > 1, el motor está prendido y hay que echarle más leña ahí antes que ir a
otro lado.

### 👥 Motor 2 — El multi-dispositivo (crecimiento dentro de la cuenta)

Cada comercio que activa el modo dueño-remoto instala PagoYa en **2 a 5 teléfonos
más**: trabajadores, la esposa, el motorizado, el hijo que ayuda los sábados.

Cada uno de esos:
- ya tiene la app instalada y sabe usarla;
- muchos tienen su propio negocio chico, o lo tendrán;
- son evangelizadores naturales porque el producto les hace la vida más fácil.

**Por eso el plan Gratis tiene que permitir escuchar**, no solo capturar. Un
trabajador con la app es un futuro cliente instalado a costo cero.

### 🤝 Motor 3 — El reseller con comisión recurrente

Ya está diseñado en `PANEL.md` §6 y la decisión clave es correcta: **comisión
recurrente, no por alta.** Si cobra por registrar, registra y desaparece. Si cobra
mientras el comercio siga pagando, se convierte en tu soporte en la calle gratis.

Lo que este documento agrega: **el reseller ideal ya está dentro del mercado.**
No es un vendedor contratado, es el que reparte gaseosas, el distribuidor
mayorista, el que ya entra a 40 bodegas por semana y a quien todos le abren la
puerta. Ese es el canal, no una fuerza de ventas propia.

### 📈 Motor 4 — El corpus de patrones (el foso que se construye solo)

Cada comercio, con el modo aprendizaje, reporta formatos de notificación que no
matchearon. Eso alimenta Remote Config y **mejora la precisión de todos los demás**.

```
  Más comercios → más formatos capturados → mejor parser
       ▲                                          │
       └────── menos fallos, menos churn ◄────────┘
```

Es el único activo que un competidor nuevo no puede comprar ni copiar: hay que
haber estado escuchando el mercado peruano durante años. **A los dos años, es la
razón por la que nadie te alcanza.**

---

## 5. La estrategia geográfica: densidad, no dispersión

El error clásico sería vender 100 comercios repartidos por todo Lima. Sería el
peor resultado posible: el motor del sonido no prende, el soporte cuesta una
fortuna en desplazamientos y no hay prueba social.

**La jugada correcta es tomar UN mercado y saturarlo.**

| | 100 comercios dispersos | 100 comercios en 3 mercados |
|---|---|---|
| Motor del sonido | ❌ apagado | ✅ prendido |
| Prueba social | "hay una app por ahí" | "acá todos lo usan" |
| Costo de visita/soporte | alto | una tarde cubre 30 |
| Instalación de parlantes (fase 2) | logística imposible | una ruta |
| Historia para vender | ninguna | **"el mercado X entero ya lo usa"** |

### El Mercado Modelo

Elegir **un mercado** —mediano, con 80–200 puestos, en una zona que te quede
cerca— y ponerse una meta absurda: **30 % de los puestos.**

Ese mercado deja de ser un cliente y se convierte en:

- **Showroom** — llevas a cualquier prospecto a verlo funcionando.
- **Set de grabación** — todo el contenido de `REDES.md` sale de ahí.
- **Laboratorio** — pruebas precios, voz, onboarding, con feedback en el día.
- **Caso de venta** — "en el mercado X hay 40 puestos con PagoYa" abre cualquier
  puerta, incluida la de un distribuidor o un gremio.

Luego se replica el mismo libreto mercado por mercado. **Nunca se abre un mercado
nuevo sin haber saturado el anterior.**

---

## 6. Las cuatro etapas

| Etapa | Meta | Jugada principal | Compuerta para avanzar |
|---|---|---|---|
| **A. Prueba** | 0 → 10 | Beta gratis, tú instalando a mano, en un solo mercado | 10 comercios usándolo 3 semanas seguidas |
| **B. Mercado Modelo** | 10 → 100 | Saturar un mercado. Motor del sonido + sticker + contenido | 30 % de un mercado, churn < 8 % |
| **C. Réplica** | 100 → 500 | Repetir el libreto en 4–6 mercados + resellers ya con caso probado | **400–500 pagando = punto de equilibrio** |
| **D. Escala** | 500 → 2,000 | Capturador/parlante, app iOS, Garantía de Aviso, otras ciudades | Churn < 5 %, CAC < S/ 50 |

**Qué se lanza en cada etapa** (esto es lo que evita construir de más):

- **A:** nada nuevo. El producto de `ROADMAP.md` fase 1.
- **B:** modo ciego + Garantía de Transparencia. Sticker. Cierre de caja.
- **C:** multi-billetera completa (Plin) + efectivo. Panel de resellers.
- **D:** Capturador, Garantía de Aviso, iOS, capa de operación.

Nótese que **la Garantía de Aviso y el iOS son de etapa D**, no de ahora. Prometer
cualquiera de las dos antes es la forma más rápida de quemar la marca.

---

## 7. Economía del crecimiento

Con plan Caserito de S/ 9.90 y cobro por Yape (costo de cobro ≈ 0):

```
   Ingreso por comercio           S/ 9.90 / mes
   − Infraestructura              S/ 0.10
   − Soporte (1 por 800)          S/ 1.90
   ────────────────────────────────────────
   Contribución                   S/ 7.90 / mes

   Con churn 5 % → vida ≈ 20 meses → LTV ≈ S/ 158
   Con reseller al 20 % → contribución S/ 5.92 → LTV ≈ S/ 118
```

**Techo de CAC: S/ 40–50.** Un tercio del LTV, que es el estándar sano.

| Canal | CAC estimado | Comentario |
|---|---|---|
| Motor del sonido (referido en el mercado) | **≈ S/ 0** | El mejor. Por eso la densidad es la estrategia. |
| Orgánico TikTok/Facebook (`REDES.md`) | bajo, pero lento | Tiempo, no plata |
| Venta puerta a puerta propia | ~S/ 40 (8 visitas, 2 cierres, día de S/ 80) | Sostenible, no escala solo |
| Reseller | ≈ S/ 0 adelantado, 20 % recurrente | **El que escala.** Paga con margen, no con capital |
| Publicidad pagada | ⚠️ no medido | **No gastar hasta etapa C.** Sin caso probado, quemas plata |

La lectura importante: **PagoYa puede crecer sin capital**, porque los dos motores
más fuertes (sonido y reseller) no exigen inversión adelantada. El capital recién
hace falta en etapa D, para la flota de hardware.

---

## 8. Métricas norte

Cinco, en orden. Si una está mal, no sirve mirar las de abajo.

| # | Métrica | Meta | Qué te dice |
|---|---|---|---|
| 1 | **Comercios activos pagando** | 400–500 = equilibrio | ¿Existe el negocio? |
| 2 | **Churn mensual** | < 5 % | ¿El producto se queda? |
| 3 | **Minutos ciegos por comercio/mes** | → 0 | **Proxy real de precisión.** La métrica más honesta que tienes |
| 4 | **Altas por mercado / altas existentes** | > 1 | ¿El motor del sonido está prendido? |
| 5 | **% que pasa de Gratis a pago** | > 15 % | ¿El plan gratis alimenta o canibaliza? |

La #3 es la que hay que perseguir con obsesión: **si los minutos ciegos tienden a
cero, la Garantía de Aviso se vuelve barata y el 100 % deja de ser una promesa
para volverse un hecho medible.**

---

## 9. Qué NO hacer para crecer

- ❌ **No dispersar.** 100 comercios en 20 distritos valen menos que 40 en un
  mercado. El motor del sonido necesita densidad o no existe.
- ❌ **No prometer la Garantía de Aviso antes del Capturador.** Con 80 % de
  precisión es una máquina de reembolsos y de reseñas malas.
- ❌ **No gastar en publicidad pagada antes de la etapa C.** Sin un caso probado
  ni un embudo medido, es quemar plata en aprender lo que la calle te dice gratis.
- ❌ **No contratar fuerza de ventas propia.** El canal ya existe: distribuidores,
  repartidores y mayoristas que entran a 40 bodegas por semana.
- ❌ **No cobrar por alta al reseller.** Registra basura y desaparece (`PANEL.md` §6).
- ❌ **No quitarle al plan Gratis la función de escuchar.** Es el Motor 2 completo.
- ❌ **No confiarse en el precio y el RUC.** Son ventaja prestada. El reloj corre.

---

## 10. Resumen

1. **El diferencial no es cobrar, es avisar.** PagoYa es la capa de certeza sobre
   el riel que el comerciante ya usa — y por eso puede ser sin comisión y sin RUC.
2. **El 100 % se vende con garantía, no con adjetivos.** Transparencia ahora,
   Garantía de Aviso cuando el Capturador la haga barata.
3. **La ventaja de hoy es prestada** (precio y RUC). Sirve para crecer rápido
   mientras se construye la durable: multi-billetera, hardware y operación.
4. **El motor único es el sonido**, y solo prende con densidad geográfica. Un
   mercado saturado vale más que una ciudad salpicada.
5. **Se puede crecer sin capital** hasta el punto de equilibrio: 400–500 comercios,
   CAC bajo S/ 50, con el mercado vendiéndose a sí mismo.
