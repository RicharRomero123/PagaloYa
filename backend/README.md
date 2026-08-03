# PagoYa — Backend (Firebase)

Proyecto Firebase: `PagoYa` (consola: console.firebase.google.com)

## Modelo de datos (Firestore)

```
operadores/{uid}                     → { nombre }   ← tú y tu equipo (PANEL.md)
usuarios/{uid}                       → { comercioId }
codigos/{codigo6digitos}             → { comercioId }
comercios/{id}                       → { nombre, duenoUid, codigoVinculacion, creadoEn,
                                         numDispositivos, suscripcion, ubicacion?, contacto? }
    suscripcion                      → { estado, plan, vigenteHasta, inicioPrueba?, origen? }
comercios/{id}/miembros/{uid}        → { rol: "dueno"|"trabajador", nombre, puedeCapturar }
comercios/{id}/pagos/{pagoId}        → { billeteraId, billeteraNombre, pagador,
                                         monto, timestamp, origenUid, recibidoEn }
comercios/{id}/pagosMembresia/{id}   → { monto, metodo, periodoDesde, periodoHasta,
                                         cobradoPor, creadoEn, nota?, comprobanteUrl? }
```

### Contrato de `suscripcion` (fuente de verdad del plan y la prueba)

```
suscripcion: {
  estado: "prueba" | "activa" | "vencida",      // string
  plan:   "gratis" | "caserito" | "patron",     // string (id del plan)
  inicioPrueba: <millis>,   // number (epoch ms), OPCIONAL — informativo
  vigenteHasta: <millis>,   // number (epoch ms) — FUENTE DE VERDAD del vencimiento
  origen: "manual" | "pasarela" | "sistema",    // OPCIONAL — procedencia
}
```

- **`vigenteHasta` manda.** No hay Cloud Function que voltee el estado al vencer:
  el plan se degrada **solo por fecha**. El plan EFECTIVO (el que realmente
  cuenta ahora) es el `plan` **solo si** `estado ∈ {prueba, activa}` **y**
  `vigenteHasta >= ahora`; en cualquier otro caso cae a **Gratis (1 teléfono)**.
  Esta lógica vive idéntica en tres lugares: `planEfectivo()` en
  `firestore.rules` (hace cumplir el tope de dispositivos del lado servidor),
  `ComercioRepo.planDe()` en la app Android, y el panel.
- **`origen` e `inicioPrueba` son opcionales.** La app Android siembra la prueba
  SIN `origen` (solo `estado`, `plan`, `inicioPrueba`, `vigenteHasta`); las
  Functions y el panel sí ponen `origen`. Las reglas validan `origen` e
  `inicioPrueba` solo si están presentes, así un update posterior del operador
  sobre una suscripción sembrada por la app no se rechaza.

### Prueba gratis de 30 días (dónde nace)

Todo comercio nuevo nace con una prueba de 30 días con fecha de finalización
explícita. Se siembra en el **primer** punto que exista:

- **App Android** (caso normal): al crear el comercio escribe la `suscripcion`
  con `estado:"prueba"`, `plan:"caserito"`, `inicioPrueba: ahora`,
  `vigenteHasta: ahora + 30 días`. Las reglas la aceptan como **semilla de
  prueba acotada** (`suscripcionPruebaSemilla`): solo `estado:"prueba"`, solo
  plan `caserito`/`gratis`, y `vigenteHasta` a lo sumo ~31 días en el futuro. El
  cliente NO puede auto-regalarse `activa`, `patron`, ni una prueba de años.
- **Cloud Function `trialAlCrearComercio`** (respaldo): si un comercio llega a
  crearse SIN `suscripcion` (alta por servidor, migración), la Function siembra
  el mismo trial de 30 días con el Admin SDK (`origen:"sistema"`). Si ya trae
  `suscripcion`, **no la pisa**.

### Qué controla el panel del operador (vive en `panel/`, no en `backend/`)

El panel es una carpeta aparte (`panel/`), fuera de este backend. Para controlar
las pruebas, el panel lee y edita el mismo contrato de `suscripcion` de arriba:

- **Ver:** fecha de vencimiento (`vigenteHasta`), estado (`prueba`/`activa`/
  `vencida`) y **días restantes** (`vigenteHasta − ahora`). Ya implementado en
  `panel/src/componentes/FichaComercio.tsx` y `panel/src/lib/comercios.ts`.
- **Editar/extender:** cambiar `estado`, `plan` y `vigenteHasta` (extender la
  prueba, promover a pagado, cortar el plan). Ya implementado en
  `panel/src/lib/membresia.ts` (`ajustarSuscripcion`, `cobrarYActivar`,
  `darPrueba`, `cortarPlan`) y sus formularios. Las reglas exigen que estos
  updates de `suscripcion` los haga un **operador** (`suscripcionValida`).
- ⚠️ Nota de contrato (panel, fuera de scope de este backend): el tipo
  `Suscripcion` en `panel/src/lib/comercios.ts` declara `origen` como
  `"manual" | "pasarela"` y **no** contempla `"sistema"` ni `inicioPrueba`.
  Como el panel solo LEE esos campos, no rompe en runtime, pero conviene ampliar
  el tipo a `"manual" | "pasarela" | "sistema"` y sumar `inicioPrueba?: number`
  para que coincida con lo que siembran la app y las Functions.

La `suscripcion` la escribe **solo un operador** (ruta de update) o la propia app
como semilla de prueba acotada al crear: si el cliente pudiera activarse un plan
pagado, no habría negocio.

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

## Tope de dispositivos por plan (baranda de servidor)

PagoYa cobra por nº de teléfonos vinculados a un comercio. El tope se hace
cumplir en las reglas (el límite del cliente Android es evadible):

- Topes canónicos (`maxDispositivosDePlan` en `firestore.rules`): gratis=1,
  caserito=3, patron=10. Sin suscripción → gratis (1). Espejo en el cliente:
  `app-android/.../core/Plan.kt`.
- Contador denormalizado `comercios/{id}.numDispositivos` (= nº de docs en
  `miembros`, el dueño incluido). Nace en 1 al crear el comercio. Unirse suma +1
  y salir resta -1, **en el mismo lote atómico** que crea/borra el miembro; las
  reglas exigen esa coherencia y que el +1 no pase del tope del plan. La carrera
  se descarta porque el ±1 es un update sobre el mismo doc del comercio, que
  Firestore serializa.
- Detalle, migración de comercios legacy y pendientes: ver
  `backend/PENDIENTE-LIMITE-DISPOSITIVOS.md`.

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

El **primer** dueño se crea a mano. De ahí en adelante, él da de alta al resto
desde el panel (Equipo).

1. Consola Firebase → **Authentication → Users** → copia tu **UID**.
   (Si aún no apareces, entra una vez al panel: la pantalla "sin acceso" te
   muestra el UID con un botón para copiarlo.)
2. Firestore Database → **Iniciar colección** → id `operadores`.
3. Id del documento = **tu UID exacto**, no "ID automático", no tu correo.

Con estos tres campos:

| Campo | Tipo | Valor |
|---|---|---|
| `nombre` | string | tu nombre |
| `nivel` | string | `dueno` |
| `activo` | boolean | `true` |

⚠️ **`nivel` tiene que decir `dueno`.** Sin ese campo entras al panel pero no
puedes administrar el equipo: las reglas tratan como operador raso a cualquier
documento sin `nivel`.

### Cómo se administra el equipo después

- Solo un `nivel: dueno` puede crear, editar o eliminar operadores.
- **Nadie se edita ni se borra a sí mismo**, ni siquiera un dueño. Es para que
  el último dueño no se degrade por error y deje el negocio sin quien administre
  el acceso. Si pasa, se arregla desde esta consola.
- Baja suave con `activo: false`: pierde el acceso al instante pero su nombre
  sigue apareciendo en los cobros que registró.
- Para dar de alta a alguien necesitas su UID. Que entre al panel y te lo dicte
  desde la pantalla "sin acceso" — buscar usuarios por correo exigiría el Admin
  SDK en un servidor, o sea plan Blaze.

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
