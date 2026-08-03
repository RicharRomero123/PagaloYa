# PagoYa — Mapa de mercado

> Análisis de segmentos. Fecha: 3 de agosto de 2026.
> Complementa `PLAN.md` (negocio y presupuesto) y `BRAND.md` (tono).

## 1. El hallazgo: el producto es más grande que la bodega

Hasta ahora PagoYa se describía como «anunciador de voz para bodegas». Eso es un
**caso de uso**, no el producto. El producto real es:

> **El Yape de uno, en el oído de otro.**
> Una cuenta de billetera vive en un teléfono. Las personas que necesitan saber
> del pago están en otros teléfonos, a veces a kilómetros. PagoYa las conecta en
> segundos, sin darles acceso a la cuenta.

En cuanto se nombra así, el mercado deja de ser «bodegas» y pasa a ser
**cualquier situación donde quien cobra no es quien tiene la cuenta**. En el Perú
informal eso está en todas partes.

El anuncio de voz no es el producto: es la *forma de entrega* correcta para gente
con las manos ocupadas. El anti-yape-falso no es el producto: es la *consecuencia*
de que el aviso nazca de la notificación real del sistema.

## 2. Los cuatro dolores que resuelve el mismo mecanismo

Todo segmento entra por al menos uno. Los mejores segmentos entran por tres o cuatro.

| Eje | El dolor | Quién lo sufre |
|---|---|---|
| **A. Dueño ≠ operador** | Quien cobra no tiene la cuenta y no puede confirmar nada sin llamar | Bodega con trabajadores, combi, delivery, restaurante, ferretería |
| **B. Seguridad** | No quiero exponer el teléfono donde está mi plata | Taxista, mototaxista, ambulante, cobrador |
| **C. Manos ocupadas** | No puedo mirar la pantalla en el momento del cobro | Bodega en hora punta, conductor manejando, motorizado con casco |
| **D. Anti-fraude** | Me pueden pasar una captura editada | Todos; más agudo donde el cliente se va y no vuelve: taxi, delivery, combi |

## 3. Contexto de mercado (cifras verificadas)

- **Yape: 16.4 millones de usuarios activos mensuales** (Credicorp, Q1 2026), y
  **entre 50 % y 60 % de sus transacciones van hacia un negocio**. El cobro por
  billetera ya es la norma, no la excepción.
- **Bodegas: ~414 000 formales**; la Asociación de Bodegueros habla de **~535 000
  en total** con 60 % informales, y unas **150 000 solo en Lima**.
- **Motos y mototaxis: más de 3 millones** en el parque automotor peruano. Las
  estimaciones de mototaxis van de 300 000 a 600 000 y ninguna es confiable por
  la informalidad.
- **Robo de celulares: ~4 500 equipos al día** (Osiptel). Este es *el* dato del
  segmento transporte: explica por qué un taxista no saca su teléfono principal.
- **Transporte público:** el Metropolitano y los corredores ya aceptan recarga
  con Yape y Plin (4.9 millones de operaciones hasta enero de 2026), y hay
  sistemas de QR en buses. Pagar el pasaje con billetera ya está normalizado.
- **Delivery:** Rappi declaraba ~12 000 motorizados en Perú (2023). El grueso del
  reparto peruano no está en las apps grandes sino en **motorizados propios de
  restaurantes, boticas y tiendas de barrio**, que es donde vive el dolor.

> Fuentes: Credicorp, Asociación de Bodegueros del Perú, Osiptel, ATU, prensa
> económica peruana. Ninguna de estas cifras debe publicarse en la landing sin
> citar la fuente y el año.

## 4. Los segmentos

### 4.1 Bodegas y puestos de mercado — **el ancla**

- **Ejes:** A, C, D
- **Tamaño:** ~500 000 negocios
- **Dolor:** hora punta, yape falso, el dueño que no está en el local
- **Quién paga:** el dueño; plan Caserito
- **Canal:** asociaciones de bodegueros, distribuidoras mayoristas, TikTok
- **Dificultad:** baja. Ya es el mensaje actual de la landing.

Sigue siendo el segmento ancla: mayor volumen, canal identificable y el que
mejor tolera una mensualidad. No se toca.

### 4.2 Delivery, couriers y motorizados — **la mejor oportunidad nueva**

- **Ejes:** A, C, D — los tres, y de forma aguda
- **Dolor:** el motorizado **no tiene la cuenta**. Hoy el flujo real es: el
  cliente le muestra el pago, él **toma una captura de pantalla** y se la manda
  al dueño por WhatsApp para que confirme. Es lento, se cae en zonas sin señal,
  y la captura es justo lo que se falsifica.
- **Quién paga:** el restaurante, la botica o la tienda dueña del motorizado
- **Canal:** restaurantes y boticas de barrio; venta uno a uno o por zona
- **Dificultad:** baja-media

**Por qué es la mejor apuesta:** el proceso que reemplaza (captura + WhatsApp +
esperar respuesta) es tan malo y tan visible que la demo se vende sola. Además el
que paga es un negocio, no una persona, así que tolera mejor la mensualidad. Y un
restaurante con tres motorizados es una venta de tres teléfonos conectados.

### 4.3 Taxistas y mototaxistas — **el más grande y el más difícil de cobrar**

- **Ejes:** B, C, D
- **Dolor doble:**
  1. **Seguridad.** Con 4 500 robos de celular al día, muchos conductores no
     trabajan con su teléfono principal. Llevan uno barato o ninguno, y ahí no
     está el Yape.
  2. **La cuenta es de otro.** Muchísimos trabajan con el Yape de la esposa, del
     dueño del auto o de un familiar. No pueden confirmar el pasaje.
- **Quién paga:** el conductor; disposición a pagar **baja** (ingreso diario,
  mentalidad de gasto variable)
- **Canal:** paraderos, grifos, gremios, TikTok, asociaciones de mototaxis
- **Dificultad:** media-alta para monetizar, **baja para crecer**

**Estrategia recomendada: usarlo como motor de adopción, no de ingresos.** El
plan Gratis les calza perfecto (1 celular escuchando la cuenta de otro) y son un
público enorme y muy conversador en redes: el video de «el taxista que escucha el
Yape de su esposa sin sacar el celular» se comparte solo. Monetización realista:
un plan anual barato, o simplemente aceptarlos como volumen y marca.

### 4.4 Transporte público: combis, buses, cobradores

- **Ejes:** A, C, D
- **Dolor:** el cobrador maneja el efectivo pero el Yape es del dueño de la
  unidad o de la empresa. Cobrar por billetera hoy le obliga a confiar en la
  pantalla del pasajero, que se baja en la siguiente esquina.
- **Quién paga:** el dueño de la unidad o la empresa de transporte
- **Canal:** empresas y comités de ruta — venta por flota
- **Dificultad:** alta (gremios, informalidad, decisión colectiva), pero
  **ticket alto**: una empresa son decenas de unidades de golpe.

El pago de pasaje por QR ya está normalizado (Metropolitano, corredores,
TuBoleto), así que el mercado está educado. Es el segmento de mayor palanca por
venta pero el de ciclo más largo. **No atacarlo hasta tener tracción demostrable.**

### 4.5 Extensiones naturales (mismo mecanismo, cero desarrollo extra)

Restaurantes con mozos · Ferreterías · Boticas · Peluquerías y barberías ·
Talleres mecánicos · Lavanderías · Ambulantes y ferias · Colegios y academias ·
Veterinarias · Cabinas y locutorios.

Todos comparten el eje A: hay un dueño con la cuenta y hay personal cobrando.
No requieren producto nuevo, solo copy propio.

## 5. Prioridad

| # | Segmento | Tamaño | Dolor | Paga | Facilidad | Veredicto |
|---|---|---|---|---|---|---|
| 1 | Bodegas | ●●●●● | ●●●● | ●●●● | ●●●● | Ancla. Mantener. |
| 2 | Delivery / motorizados | ●●● | ●●●●● | ●●●● | ●●●● | **Atacar ya.** |
| 3 | Taxis y mototaxis | ●●●●● | ●●●● | ●● | ●●● | Motor de adopción y marca. |
| 4 | Restaurantes y locales con personal | ●●●● | ●●● | ●●●● | ●●● | Extensión barata. |
| 5 | Transporte público | ●●● | ●●●● | ●●●● | ● | Fase 2. Ticket alto, ciclo largo. |

> **Actualización agosto 2026 — Yape Empresa cruza este cuadro.** Yape lanzó
> ayudantes multi-dispositivo en tiempo real, pero **exige RUC** y cobra
> **2.95 % + S/ 0.29 por cobro**. Eso parte los segmentos en dos:
>
> - **Sin RUC** (bodega informal, taxista, mototaxi, ambulante, feria): Yape
>   Empresa **no puede venderles**. PagoYa es la única opción. Defensa estructural.
> - **Con RUC** (bodega formal, restaurante con motorizados, botica): Yape Empresa
>   sí compite, pero 89× más caro en ticket de bodega. Defensa por precio.
>
> Ojo con el #2, delivery: el que paga es un restaurante **con RUC**, así que es el
> segmento donde Yape Empresa sí puede aparecer. Sigue siendo la mejor apuesta —
> el dolor de la captura por WhatsApp es real y la comisión los expulsa — pero el
> pitch ahí es de costo y multi-billetera, no de "somos los únicos".
> Análisis completo en `ESCALA.md` §10.

## 6. Qué implica para el producto

Nada de esto requiere funciones nuevas: **el modo dueño-remoto ya es todo el
producto para estos segmentos.** Lo que sí cambia:

1. **El modo remoto deja de ser una función del plan Caserito y pasa a ser el
   corazón del producto.** Hoy la landing lo cuenta como un extra («¿saliste?
   igual te enteras»). Para taxi, delivery y combi es la razón de instalar.
2. **El teléfono que captura puede no ser el del que trabaja.** El caso «el Yape
   está en el teléfono de mi esposa, en la casa» exige que el equipo capturador
   sea estable y esté siempre encendido. La guía de configuración de batería
   pasa a ser crítica, no un detalle.
3. **Anuncio discreto.** Un taxista con pasajero o un motorizado con casco no
   siempre quiere que su teléfono grite el monto. Vale la pena una voz baja,
   un tono corto o solo vibración + pantalla.
4. **Sin nombre en voz alta, por defecto.** Ya está resuelto en la FAQ, pero en
   transporte y delivery importa más.

## 7. Qué implica para la landing

**Riesgo a evitar:** hablarle a todos y no conectar con nadie. La home tiene una
voz de bodega muy lograda; volverla genérica sería un retroceso.

**Estructura recomendada:**

1. **La home mantiene el ancla de bodega**, pero suma una sección
   «¿Para quién es PagoYa?» después de «Cómo funciona» — una vez que el visitante
   ya entendió el mecanismo, se le muestra en cuántas situaciones aplica.
   *(Implementado: sección `#para-quien`.)*
2. **Páginas por segmento**, cada una con su copy, su dolor y su SEO propio:
   - `/para-delivery/` → «delivery yape captura», «confirmar pago motorizado»
     *(✅ publicada)*
   - `/para-taxistas/` → «taxista yape esposa», «cobrar sin sacar el celular»
   - `/para-transporte/` → «cobrador combi yape»

   Es lo que permite hablarle a cada uno en su idioma sin diluir la home, y abre
   tres racimos de palabras clave nuevos con competencia casi nula. El catálogo
   está en `landing/lib/segmentos.js`; una tarjeta de `#para-quien` solo muestra
   enlace cuando su segmento ya tiene página.
3. **El mensaje transversal** que une todos los segmentos, y que debería
   convivir con «si no suena, no te pagaron»:

   > **Tu Yape se queda donde está. El aviso llega donde estés.**
