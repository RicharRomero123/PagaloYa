# PagoYa — Panel de operador

> **El panel es TUYO, no de tus clientes.** Es la consola con la que administras
> el negocio: quién usa PagoYa, dónde está, quién lo vendió, quién pagó y a quién
> le toca comisión.
>
> El dueño del comercio ve **sus** reportes en la app móvil. Nunca entra aquí.

## 1. Para qué existe

Cuatro trabajos, en orden de importancia:

1. **Membresías** — activar, renovar y cortar planes. Todo manual al inicio.
2. **Cobertura** — mapa con cada local que usa PagoYa y sus coordenadas exactas.
3. **Resellers** — quién vendió qué, y cuánta comisión le toca.
4. **Salud del producto** — comercios activos, mudos, o que nunca activaron.

Lo que **no** hace: mostrarle reportes al comerciante. Eso es la app.

## 2. Los tres roles

Colecciones separadas, nunca un campo `rol` dentro del usuario — así nadie se
auto-asciende editando su propio documento.

```
operadores/{uid}   → tú y tu equipo interno. Ven todo.
resellers/{uid}    → vendedores de calle. Ven solo lo suyo.
usuarios/{uid}     → comerciantes. Ni saben que el panel existe.
```

### Qué ve cada uno

| | Operador | Reseller | Comerciante |
|---|---|---|---|
| Todos los comercios | ✅ | ❌ solo los que registró | ❌ solo el suyo |
| Ubicación y contacto | ✅ | ✅ los suyos | — |
| Estado de membresía | ✅ | ✅ los suyos | ✅ el suyo |
| **Monto de ventas del comercio** | ✅ | 🚫 **NUNCA** | ✅ el suyo |
| Activar/cortar plan | ✅ | ❌ | ❌ |
| Comisiones | ✅ todas | ✅ la suya | — |

### 🚫 El reseller no ve cuánto vende el comercio

Esto va en las reglas de Firestore desde el primer día, no después.

Es información privada del negocio. El día que un vendedor se pase a la
competencia con la lista de cuánto factura cada bodega del mercado, te quema el
producto. Un reseller ve que el negocio existe, si está activo y cuándo vence.
Nada más.

## 3. Modelo de datos

Se agrega a lo que ya existe (ver `backend/README.md`).

```
operadores/{uid}
  nombre · activo

resellers/{uid}
  nombre · telefono · zonas[] · comisionPct · activo · creadoEn

altas/{altaId}                    ← el reseller la crea parado afuera del local
  codigoAlta        (6 dígitos, es lo que el dueño escribe en la app)
  resellerUid
  negocio           { nombre, direccion, referencia }
  contacto          { nombreDueno, telefono }
  ubicacion         GeoPoint
  fotoFachadaUrl
  estado            "registrada" | "activada" | "descartada"
  comercioId        (se llena cuando el dueño activa)
  creadaEn          serverTimestamp

comercios/{id}                    ← campos nuevos
  suscripcion   { plan, estado, vigenteHasta, origen: "manual"|"pasarela" }
  ubicacion     { geo: GeoPoint, direccion, referencia, fotoFachadaUrl }
  contacto      { nombreDueno, telefono }
  registradoPor resellerUid | null   ← la atribución, inmutable

comercios/{id}/pagosMembresia/{pagoId}
  monto · metodo ("efectivo"|"yape"|"transferencia"|"tarjeta")
  periodoDesde · periodoHasta
  cobradoPor    (uid del operador o reseller)
  comprobanteUrl · nota · creadoEn serverTimestamp

comisiones/{id}
  resellerUid · comercioId · periodo ("2026-08")
  monto · estado ("pendiente"|"pagada") · pagadaEn
```

### Reglas que no se negocian

- **El dueño NO puede escribir su propia `suscripcion`.** Solo un operador.
  Si el cliente puede activarse el plan solo, no tienes negocio.
- **`registradoPor` es inmutable.** Se escribe una vez, al activar. Nadie —ni un
  operador— lo cambia después, o las comisiones dejan de ser confiables.
- `pagosMembresia` es **solo-crear**, como los pagos. Un cobro no se edita ni se
  borra: se corrige con un asiento nuevo. Es tu contabilidad.

## 4. Flujo de alta con reseller

El reseller trabaja desde el **panel web en su celular**, no desde la app. El
navegador le da GPS y cámara igual que una app nativa, y así puedes cambiar el
proceso de venta cuando quieras sin pasar por revisión de Play — que es justo lo
que vas a hacer semana a semana los primeros meses.

```
1. El reseller, parado afuera del local
   ├─ captura GPS (navegador), foto de fachada
   ├─ llena nombre del negocio, dirección, referencia, contacto del dueño
   └─ el panel genera un codigoAlta de 6 dígitos
                              │
2. Instala PagoYa en el teléfono del dueño (el que tiene el Yape del negocio)
   y el dueño entra el codigoAlta
                              │
3. La app crea comercios/{id} con
   ├─ duenoUid      = el dueño   ← su data es suya desde el minuto uno
   ├─ registradoPor = resellerUid
   └─ ubicacion     = la que capturó el reseller
                              │
4. altas/{altaId}.estado = "activada"
```

**Por qué en dos tiempos y no todo desde el reseller:** si el reseller creara el
comercio, `duenoUid` quedaría mal y el comerciante no sería dueño de sus propios
datos. Además, separar "registrada" de "activada" te regala un **embudo**: qué
reseller vende de verdad y cuál solo camina.

## 5. Membresías: híbrido manual + pasarela

**Al inicio, todo manual.** Cobras en efectivo o por Yape y activas desde el
panel. Es lo que ya dice `PLAN.md` y es lo correcto: no montas una pasarela para
diez clientes.

La clave del diseño: **los dos caminos escriben el mismo documento**.

```
Cobro manual (hoy)          Tarjeta (después)
        │                          │
        └──────────┬───────────────┘
                   ▼
    comercios/{id}.suscripcion
      { plan, estado, vigenteHasta, origen }
    + comercios/{id}/pagosMembresia/{id}
```

La app y las reglas solo miran `vigenteHasta`. Les da igual de dónde vino la
plata. Así la pasarela se agrega después **sin tocar nada** de lo ya construido.

### ⚠️ La pasarela es lo único que te va a exigir un servidor

Un sitio estático no recibe webhooks. Cuando agregues tarjeta (Culqi, Mercado
Pago), la confirmación del pago tiene que llegar a un endpoint: Cloud Functions
(plan Blaze) **o** un VPS propio.

Hasta entonces, todo corre gratis en el plan Spark.

## 6. Comisiones

**Recurrente, no por alta.** Es la decisión que define si esto funciona.

Si pagas una comisión única por registrar, el reseller registra a cualquiera,
cobra y desaparece: terminas con 200 comercios de los cuales 40 usan la app.

Si pagas un porcentaje **mientras el comercio siga pagando**, al reseller le
conviene que el comerciante siga activo. Se convierte en tu soporte gratis en la
calle. Es cómo lo hacen los ISP y por eso funciona.

### Contra el fraude

- La comisión se gana con la **primera membresía pagada**, no con el registro.
- GPS + foto de fachada hacen que inventar altas falsas no valga la pena.
- El embudo (registradas vs. activadas) delata al que solo llena formularios.

## 7. Mapa de cobertura

**Leaflet + OpenStreetMap.** No Google Maps.

Google te va a pedir tarjeta y facturación. Para unos cientos de pines, Leaflet
con tiles de OSM es gratis, sin API key, sin billing, y se ve igual de bien.

No te metas con geohashing ni consultas por radio: carga todos los comercios y
filtra en el navegador. Con cientos de locales sobra, y te ahorras una capa de
complejidad que no vas a necesitar en años.

Cada pin, coloreado por estado:

| Color | Estado |
|---|---|
| 🟢 Verde | Activo y al día |
| 🟡 Ámbar | Vence en menos de 7 días |
| 🔴 Rojo | Vencido |
| ⚪ Gris | Alta registrada, nunca activó |
| 🔇 Morado | Activo pero **sin pagos en 48h** ← el que hay que llamar hoy |

Ese último es el más valioso del panel: es un cliente que está por irse y todavía
no lo sabe.

## 8. Pantallas

1. **Mapa** (home) — cobertura con filtros por estado, reseller y zona.
2. **Comercios** — lista y buscador; ficha con contacto, ubicación, historial de
   membresía y quién lo vendió. Botón grande de **activar/renovar plan**.
3. **Membresías** — quién vence esta semana, quién ya venció, cobros del mes.
4. **Resellers** — altas, tasa de activación, comisión pendiente y pagada.
5. **Alta nueva** — el formulario de campo, mobile-first, con GPS y cámara.
6. **Salud** — comercios sin pagos hace 48h, altas que nunca activaron, comercios
   sin permisos completos.

## 9. Stack y despliegue

- **Next.js con `output: 'export'`** (estático) + Tailwind, Firebase JS SDK.
- **Firebase Hosting**, plan Spark, gratis. Dominio propio con SSL incluido.
- ⚠️ Nada de SSR ni API routes: eso despliega a Cloud Run y te exige Blaze. No
  hace falta — el navegador habla directo con Firestore y las reglas protegen.
- **Mobile-first de verdad.** El reseller lo usa parado en un mercado, con una
  mano, con sol en la pantalla. Botones grandes, contraste alto.
- Identidad de `BRAND.md`: naranja #FF6B1A, azul noche #1A2B4A. Nada de morado.

### Cuidado con la cuota de lecturas

Spark da ~50 000 lecturas de Firestore al día. Un panel con listeners en vivo
abierto toda la tarde las quema rápido. Consulta con `limit`, no re-suscribas en
cada render, y usa `get()` en vez de `onSnapshot` donde no necesites tiempo real
(que es casi todo el panel: solo el mapa y "salud" ganan algo con vivo).

## 10. Lo que el panel JAMÁS hace

- **No crea pagos.** Regla de oro: los pagos solo nacen de notificaciones reales
  capturadas por la app Android. Las reglas ya lo impiden en el servidor; que
  quede claro también en el diseño.
- **No edita ni borra pagos.** Son inmutables.
- No muestra datos de ventas a un reseller.

## 11. Privacidad y Ley 29733

El panel concentra la información más sensible del proyecto: ubicación, teléfono
y facturación de cada comercio, más nombres de pagadores.

- **Registra quién consultó qué** desde el inicio. Cuando tengas vendedores en la
  calle, ese rastro te va a servir.
- El reseller solo ve sus propios comercios, y sin cifras de venta.
- Al dar de baja un reseller, su acceso se corta pero su atribución histórica
  queda (las comisiones ya ganadas no desaparecen).
- Minimiza: el panel no necesita mostrar nombres de pagadores en ningún lado.

## 12. Orden de construcción

1. Rol operador en las reglas + colección `operadores` (con prueba en emulador)
2. Login y lista de comercios
3. Ficha de comercio + activar/renovar membresía manual → **con esto ya cobras**
4. Alta de campo con GPS y foto
5. Mapa de cobertura
6. Resellers y comisiones
7. Salud del producto
8. Pasarela de pago (recién cuando el volumen lo justifique)

Los pasos 1–3 son los que te desbloquean el cobro. El resto puede esperar.
