# PagoYa — Backend (Firebase)

Proyecto Firebase: `PagoYa` (consola: console.firebase.google.com)

## Modelo de datos (Firestore)

```
operadores/{uid}                     → { nombre }   ← tú y tu equipo (PANEL.md)
usuarios/{uid}                       → { comercioId }
codigos/{codigo6digitos}             → { comercioId }
comercios/{id}                       → { nombre, duenoUid, codigoVinculacion, creadoEn,
                                         suscripcion?, ubicacion?, contacto? }
comercios/{id}/miembros/{uid}        → { rol: "dueno"|"trabajador", nombre, puedeCapturar }
comercios/{id}/pagos/{pagoId}        → { billeteraId, billeteraNombre, pagador,
                                         monto, timestamp, origenUid, recibidoEn }
comercios/{id}/pagosMembresia/{id}   → { monto, metodo, periodoDesde, periodoHasta,
                                         cobradoPor, creadoEn, nota?, comprobanteUrl? }
```

`suscripcion` = `{ plan, estado, vigenteHasta, origen }`. La escribe **solo un
operador**: si el cliente pudiera activarse el plan, no habría negocio.

- `pagoId` es determinista (`uid-timestamp-centavos`) → subir dos veces la misma
  notificación no duplica el pago. **Las reglas exigen que el id cuadre con el
  contenido**, así que tampoco se puede colar el mismo pago con otro id.
- `recibidoEn` lo pone el **servidor** (`serverTimestamp`). Es la hora
  autoritativa: ordena el historial y manda en el modo escucha.
- `timestamp` es la hora de la notificación según el teléfono que capturó. Queda
  como dato informativo y acotado (ni futuro ni más de 7 días atrás), porque el
  reloj de un teléfono no es confiable.
- Los pagos **no se editan ni se borran** (regla anti-fake: el registro es inmutable).

## Anti-fake: tres capas

La promesa del producto es "si no suena, no te pagaron". Eso solo se sostiene si
un pago **no puede nacer de otro lado** que no sea una notificación real del
sistema. Tres capas lo defienden:

1. **Rol de captura (`puedeCapturar`).** Solo el teléfono con el Yape del negocio
   crea pagos. Los trabajadores están en modo escucha: aunque alguien modifique
   el APK, el servidor le rechaza cualquier escritura en `pagos/`. El dueño puede
   habilitar un segundo teléfono de captura desde `miembros/{uid}`.
   Compatibilidad: los miembros creados antes de que existiera el campo se
   resuelven por rol (el dueño captura, el trabajador no).
2. **Validación estricta en las reglas.** Esquema cerrado (ni un campo de más),
   monto entre 0 y 20 000, hora sellada por el servidor, id coherente con el
   contenido, y `origenUid` obligado a ser quien escribe.
3. **App Check con Play Integrity.** Certifica que quien habla con Firestore es
   el APK real de PagoYa. Sin esto, las dos capas anteriores solo obligan a un
   atacante a usar la app oficial — con esto, tiene que usarla de verdad.

Lo que queda fuera del alcance de las reglas: el dueño podría, con un APK
modificado, inventar pagos **en su propio negocio**. No es una amenaza (es su
caja y su plata); la amenaza real es el cliente con la captura falsa y el
trabajador que quiere cuadrar un faltante, y ambas están cubiertas.

## Anti-fraude de membresías

Mismo principio, aplicado al cobro:

- La `suscripcion` solo la escribe un **operador**. El dueño puede renombrar su
  comercio y nada más.
- Un comercio **no puede nacer con membresía**: las reglas rechazan el campo
  `suscripcion` al crear.
- `pagosMembresia` es **solo-crear**. Un cobro no se edita ni se borra: se
  corrige con un asiento nuevo. Es contabilidad, no un formulario.
- El monto se valida (0 < monto ≤ 1000), el método es de una lista cerrada y
  `creadoEn` lo pone el servidor.

⚠️ Nota de costos: cada `get()`/`exists()` dentro de las reglas se cobra como
lectura. Por eso las condiciones van siempre en el orden
`esMiembro(...) || esOperador()` — el `||` hace cortocircuito y el comerciante
nunca paga la lectura extra del chequeo de operador.

## Cómo funciona el tiempo real (sin Cloud Functions, plan gratis)

- El teléfono del dueño captura la notificación → la sube a `pagos/`.
- Los teléfonos "escucha" (trabajadores) mantienen un listener de Firestore desde
  el servicio de primer plano → anuncian por voz cada pago nuevo que no capturaron
  ellos mismos. Latencia típica: ~1 segundo.
- El listener **no filtra por hora**: toma como base la primera foto que trae
  Firestore (esos pagos ya sonaron) y de ahí en adelante solo anuncia lo que
  llega nuevo. Así el desfase de reloj entre teléfonos deja de importar.
- Más adelante (plan Blaze): Cloud Function + FCM para despertar teléfonos aunque
  el sistema haya matado el proceso.

## PASO PENDIENTE MANUAL 1: publicar las reglas de seguridad

1. Consola Firebase → **Firestore Database → Reglas**.
2. Borrar lo que haya y pegar el contenido completo de `firestore.rules`.
3. **Publicar**.

Sin este paso, la base queda cerrada (modo producción rechaza todo) y la app
no podrá guardar nada.

## PASO PENDIENTE MANUAL 2: crearte como operador

Los documentos de `operadores/` **no los puede escribir ningún cliente** — esa es
justamente la garantía de que nadie se auto-asciende. Se crean a mano:

1. Consola Firebase → **Authentication → Users** → copia tu **UID**.
2. Firestore Database → **Iniciar colección** → id `operadores`.
3. Id del documento = tu UID. Un campo: `nombre` (texto).

Sin esto, el panel te va a rechazar aunque inicies sesión bien.

Para dar de baja a alguien del equipo, borra su documento: el acceso se corta al
instante, sin tocar reglas ni republicar nada.

## PASO PENDIENTE MANUAL 3: activar App Check

App Check es **gratis en el plan Spark**. Orden importante: primero registrar el
token de depuración, al final activar la aplicación forzosa.

1. Consola Firebase → **App Check** → pestaña **Apps** → app Android `pe.pagoya.app`.
2. Registrar el proveedor **Play Integrity** (pide la app publicada o al menos
   creada en Google Play Console, con las huellas SHA-256 en la configuración
   del proyecto Firebase).
3. **Token de depuración (para desarrollar):** correr la app en debug y buscar en
   Logcat la línea de `DebugAppCheckProvider` con un UUID. Copiarlo a
   App Check → app Android → menú ⋮ → **Administrar tokens de depuración**.
4. Solo cuando los pasos anteriores estén listos: App Check → **APIs** →
   **Cloud Firestore** → **Aplicar** (enforcement).

⚠️ Si se activa la aplicación forzosa antes de registrar el token de depuración,
la app de desarrollo deja de poder escribir en Firestore. Se revierte desde la
misma pantalla.

## Probar las reglas

```
cd backend
npm install
npm run test:reglas
```

Levanta el emulador de Firestore y corre `test-reglas.mjs`, que replica el flujo
real de la app (crear comercio, unirse con código, subir pago, modo escucha) y
además ataca las reglas como lo haría alguien que quiere inventar pagos.

## Pendiente conocido

- Los códigos de vinculación son 6 dígitos al azar sin verificar colisión: si dos
  comercios sacan el mismo, la creación del segundo falla ruidosamente
  (`[codigo] ...`). Con volumen habrá que reintentar con otro código.
