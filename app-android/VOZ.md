# PagoYa — La voz de la caja

> La voz **es** el producto. Un comerciante no mira la app: la escucha. Si suena
> a robot barato, el negocio parece barato; si suena como un parlante de banco,
> PagoYa parece un banco.

## Por qué suena a robot (el diagnóstico)

No es una limitación de Android. Son tres cosas que se acumulan:

1. **El motor.** Casi todos los teléfonos traen varios motores de texto a voz.
   El de Google (`com.google.android.tts`) tiene voces neuronales que suenan a
   persona; los de fábrica (Samsung TTS, Pico) son sintéticos de los viejos.
   Muchos equipos vienen con el malo por defecto.
2. **La voz elegida dentro del motor.** Aun con Google instalado, el sistema
   suele quedarse con una voz `QUALITY_NORMAL` local en vez de la neuronal.
3. **El acento.** No existe voz `es-PE`. Android cae a `es-ES` (España) y en
   una bodega de Lima eso suena ajeno. `es-US` (latina) es lo más cercano.

## Nivel 1 — Lo que ya hace la app

`core/voz/` + **Más → Elegir otra voz**.

- Enumera todas las voces del teléfono, filtra las de español y las **puntúa**:
  latina > española, nítida > básica, y **sin internet > con internet** (en el
  mercado el dato se cae y la venta igual tiene que sonar).
- Si el usuario no elige nada, PagoYa toma la mejor sola.
- Tarjeta por voz: tocarla la elige **y la reproduce al instante** con una frase
  real ("María te yapeó 25 soles con 50"), no con un "hola mundo".
- Velocidad y tono ajustables — más lenta se entiende mejor con bulla.
- Si el motor no es el de Google, sale un aviso con botón para instalarlo.

Esto ya saca al 80% de los teléfonos del sonido robótico, gratis y sin assets.

## Nivel 2 — El sello sonoro: **"¡PagoYaaa!"**

Es el activo de marca más valioso del producto. Yape lo entendió con su
"¡Yapeee!": el sonido se vuelve el nombre. Y en el caso de PagoYa hay una
ventaja que Yape no tiene — **cada venta de cada puesto es un anuncio gratis
para los puestos vecinos** (BRAND.md ya lo dice: cada anuncio es publicidad).

Sintetizarlo con TTS es un desperdicio: siempre es la misma frase, y ninguna voz
sintética va a tener carácter. Tiene que ser **una grabación**.

### El enganche ya está implementado

Solo falta el archivo:

```
app/src/main/res/raw/sello_pagoya.ogg
```

Se busca por nombre en tiempo de ejecución (`SelloSonoro.kt`), así que la app
compila y funciona igual sin él. Apenas exista:

- suena el sello grabado y **después** habla el TTS con el monto
- el `Anunciador` recorta el "¡Pago Ya!" del inicio de la plantilla para que no
  se diga dos veces

### Cómo debe ser (especificación)

| | |
|---|---|
| Duración | **menos de 1 segundo**. Si tarda más, estorba en hora punta |
| Voz | femenina peruana, alegre, con calle — no locutora de banco |
| Entonación | **termina subiendo** ("¡PagoYaaa!↗"). El tono ascendente lee como buena noticia; el descendente, como aviso |
| Frecuencias | fuerza entre **1–4 kHz**. Los parlantes de teléfono barato no tienen graves: lo que se pierde en bulla de mercado es todo lo que esté por debajo |
| Volumen | normalizado alto (pico cerca de 0 dBFS, sin saturar) |
| Formato | `.ogg` mono 22 kHz — pesa poco y suena igual en ese parlante |

### ⚠️ Lo único que no puede hacer

**No puede parecerse al "¡Yapeee!"** — ni en melodía, ni en cadencia, ni en
timbre. Toda la postura legal del proyecto es "compatible con Yape, no somos
Yape" (BRAND.md, PLAN.md §7). Un sello que suene a imitación del suyo es
exactamente el tipo de cosa que convierte una carta del BCP en un problema real,
y además te quita la identidad propia que estás buscando construir.

Tiene que ser **tu** melodía. Distinta a propósito.

### Dónde crearlo

**Paso 1 — Prototipo hoy, gratis, con IA.** Sirve para escuchar 5 versiones
distintas y decidir cuál quieres antes de pagarle a nadie:

| Herramienta | Para qué sirve aquí |
|---|---|
| **ElevenLabs** | La mejor calidad. Tiene voces en español latino y "voice design" para inventar una voz con carácter. Ojo: el uso comercial suele requerir plan de pago — revisa los términos del tuyo |
| **Google Cloud TTS** (Chirp/Neural2, `es-US`) | Baratísimo y con uso comercial claro. Menos "personalidad" que ElevenLabs |
| **Amazon Polly** (neural, voces `es-US`/`es-MX`) | Similar, con capa gratuita generosa |

Genera 5–6 variantes, mételas en el teléfono y escúchalas **en un puesto real**.

**Paso 2 — El definitivo, con un locutor peruano de verdad.** Lo "peculiar" que
quieres —el gancho que hace que un sonido se pegue— sale de una persona con
carácter, no de un modelo. Y sale barato:

- **Fiverr** o **Bunny Studio**: filtra por español latino. Una frase corta de
  marca ronda los US$ 20–60. Rápido y con revisiones incluidas.
- **Local en Lima** (lo mejor para acento peruano real): locutores de radio
  haciendo freelance, estudios de doblaje, o alumnos de comunicaciones. Suele
  ser más barato que las plataformas y el acento es el de tu cliente.

Pídele siempre **5 o 6 tomas** con entonaciones distintas y el archivo **WAV sin
comprimir**, no un MP3.

### ⚠️ El detalle que casi todos olvidan: los derechos

Pide por escrito la **cesión de derechos para uso comercial ilimitado**
(*buyout*). Sin eso no eres dueño del sonido, y **no puedes registrarlo como
marca sonora** — que es justo lo que hace que valga la pena.

Por la misma razón, para el sello **definitivo** conviene la voz humana: la
titularidad de lo generado por IA es terreno resbaladizo y no quieres esa duda
encima de tu activo de marca.

### El encargo, listo para copiar y pegar

> Necesito un *sound logo* para una app peruana de pagos. Una sola palabra:
> **"¡PagoYaaa!"**, con la última vocal estirada.
>
> - Voz femenina peruana, joven, alegre, con calle. No locutora de banco.
> - Menos de 1 segundo. La entonación **termina subiendo**, como buena noticia.
> - Se va a escuchar en el parlante de un celular barato, en un mercado con
>   bulla: necesito energía y presencia, no dulzura.
> - Mándame 5 o 6 tomas con entonaciones distintas, en WAV sin comprimir.
> - Incluye cesión de derechos para uso comercial ilimitado.

### Post-proceso (esto es lo que separa un sello que corta de uno que se pierde)

Con **Audacity** (gratis) o `ffmpeg`, en este orden:

1. **Recortar** los silencios de los extremos, al milisegundo.
2. **Filtro paso alto a ~180 Hz.** El parlante del celular no reproduce graves:
   lo único que hacen es comerse el volumen disponible.
3. **Compresión suave**, para que suene parejo y fuerte.
4. **Normalizar** con el pico cerca de −1 dB, sin saturar.

```
ffmpeg -i sello.wav -af "highpass=f=180,dynaudnorm,alimiter=limit=0.95" \
       -ac 1 -ar 22050 -c:a libvorbis -q:a 5 sello_pagoya.ogg
```

Deja el resultado en `app/src/main/res/raw/sello_pagoya.ogg` y ya está: la app
lo detecta sola.

### Cómo elegir la toma ganadora

No con audífonos. **En el celular más barato que tengas, dentro del bolsillo, en
un mercado.** Si ahí se entiende y llama la atención, es la buena. Y prueba las
finalistas con 3 o 4 comerciantes: la que repitan de memoria es la que se pega.

### Registrarlo

Un sello sonoro se puede registrar en Indecopi como **marca sonora**. Ya está
previsto registrar PagoYa en clases 9 y 36 (BRAND.md): vale la pena preguntar
por el sonido en el mismo trámite. Si el sello funciona como esperas, en dos
años vale más que el logo.

## Nivel 3 — Banco de audio pregenerado (calidad Alexa, sin costo por venta)

Es lo que hacen los soundbox de Alipay, Paytm e Izipay, y por eso suenan tan
bien: **no sintetizan nada en el momento — reproducen clips grabados.**

El anuncio de PagoYa es una plantilla con una sola parte libre:

```
[¡PagoYa!]  [María]  [te yapeó]  [veinticinco]  [soles]  [con]  [cincuenta]
  fijo      variable    fijo       0–99 + cientos  fijo    fijo     0–99
```

Todo menos el nombre es un conjunto **finito y chico**:

| Piezas | Cantidad |
|---|---|
| Números 0–99 | 100 |
| Cientos (100, 200 … 900) y "mil" | 10 |
| "soles", "sol", "con", "céntimos" | 4 |
| "te yapeó", "pago Plin de", "recibiste" | 3 |
| Sello "¡PagoYa!" | 1 |

≈ **120 clips**. A 22 kHz mono en `.ogg` son unos **1–1.5 MB**: entra en el APK
sin discusión. Generarlos una sola vez con Google Cloud TTS (voz Neural2/Chirp
es-US) o ElevenLabs cuesta **centavos** — son ~2 000 caracteres en total.

Ventajas frente a cualquier TTS en vivo:

- **Latencia cero.** No espera al motor: concatena y suena.
- **Costo cero por venta.** No hay API que pagar en cada pago.
- **Funciona sin internet.** Crítico en mercados.
- **Idéntico en todos los teléfonos.** Hoy la misma app suena distinta en un
  Xiaomi que en un Samsung; con el banco, suena igual en todos.
- Se convierte en argumento de venta: "voz premium" para el plan Caserito.

## El nombre del pagador — ya implementado, apagado por defecto

**Más → Elegir otra voz → Qué dice → "Decir el nombre de quien paga"**, con el
interruptor **apagado de fábrica**.

Por defecto se dice *"¡Pago Ya! Te yapearon 25 soles con 50"*. El nombre siempre
se ve en pantalla, suene o no.

Dos razones apuntan al mismo lado:

- **Legal y ético.** Gritar el nombre de un cliente en un mercado lo expone
  delante de desconocidos. Es el dato personal que la Ley 29733 y la regla 5 de
  `CLAUDE.md` piden minimizar.
- **De sonido.** Cuando llegue el banco de audio (nivel 3), mezclar clips
  premium con un nombre en TTS sonaría **peor** que un TTS consistente. Los
  soundbox del mundo no dicen nombres: dicen "recibiste 25 soles".

### Cómo está hecho

Las plantillas de voz viven en Remote Config, con dos por billetera:

```json
"vozPlantillaSinNombre": "¡Pago Ya! Te yapearon {monto}",
"vozPlantilla":          "¡Pago Ya! {nombre} te yapeó {monto}"
```

`BilleteraParser.fraseDeVoz(pago, conNombre)` elige una u otra, y en el camino
sin nombre **borra cualquier `{nombre}` que se haya colado**: una plantilla mal
escrita en la consola de Firebase no puede terminar gritando el dato de un
cliente. La decisión se toma en un solo sitio, `Anunciador.anunciarPago()`.

⚠️ Al actualizar el parámetro `billeteras_json` en Remote Config hay que incluir
`vozPlantillaSinNombre` en cada billetera. Si falta, se usa el respaldo genérico
("¡Pago Ya! Entraron {monto}"), que funciona pero suena pobre.

### Cómo se implementaría

`SelloSonoro.kt` ya establece el patrón: buscar el recurso por nombre y caer al
TTS si no está. El banco es lo mismo pero con una lista:

```
Anunciador.anunciar(pago)
  └─ BancoDeAudio.piezas(pago) → ["sello", "te_yapeo", "n25", "soles", "con", "n50"]
       ├─ ¿están todas en res/raw?  → reproducirlas en cadena (premium)
       └─ falta alguna              → TTS con la voz elegida (respaldo actual)
```

El respaldo en TTS se queda para siempre: cubre montos raros y teléfonos donde
algo falle.

## Lo que NO conviene

- **TTS en la nube en tiempo real** (llamar a ElevenLabs/Google en cada pago):
  agrega latencia justo en el momento crítico, cuesta por venta, y muere sin
  internet. Todo lo contrario de lo que necesita un mostrador.
- **Bundlear un motor TTS neuronal en el APK**: pesa decenas de MB y los
  teléfonos de gama baja de los comercios no tienen ni espacio ni RAM.

## Pendientes

- **Grabar `res/raw/sello_pagoya.ogg`** → nivel 2 activo al instante, sin tocar
  una línea de código. Es lo de mayor impacto por menor esfuerzo de todo el
  proyecto.
- Preguntar en Indecopi por el registro del sello como marca sonora, junto con
  el trámite de las clases 9 y 36.
- Pedir foco de audio (`AudioManager.requestAudioFocus`) para bajar la radio de
  la bodega mientras anuncia.
- Banco de audio pregenerado (nivel 3).
