# Tope de dispositivos por plan — estado y pendientes

Baranda de servidor para que el número de teléfonos vinculados a un comercio no
pase del tope del plan. El límite del cliente Android es evadible (APK
modificado); esto lo hace cumplir Firestore.

## Qué se implementó (opción A: contador denormalizado + reglas puras)

Se descartó la opción B (Cloud Function callable `unirseAComercio`) porque **este
proyecto no tiene Cloud Functions configurado** (no hay carpeta `functions/`, ni
`firebase-functions` en `package.json`, ni bloque `functions` en `firebase.json`)
y arranca en plan Spark. Montar Functions + pasar a Blaze solo para esto
contradice la arquitectura actual ("tiempo real sin Cloud Functions", ver
README). El contador denormalizado cierra el hueco sin salir de Spark.

- **Fuente canónica del tope** (`backend/firestore.rules`,
  `maxDispositivosDePlan(plan)`): `gratis=1`, `caserito=3`, `patron=10`. Sin
  suscripción, o plan no reconocido → `gratis` (1).
  - Espejo en el cliente: `app-android/.../core/Plan.kt` (`Plan.maxDispositivos`).
    Si cambian los topes, hay que tocar **los dos** lugares.
- **Contador**: `comercios/{id}.numDispositivos` = nº de docs en
  `comercios/{id}/miembros` (el dueño cuenta).
  - Nace en **1** al crear el comercio (la regla de `create` lo exige).
  - **+1 en el mismo lote** que crea un miembro trabajador. La regla de `create`
    del miembro exige que `getAfter(comercio).numDispositivos ==
    get(comercio).numDispositivos + 1`, y la regla de `update` del comercio
    exige que ese +1 **no pase del tope del plan**. Un miembro sin el +1, o un
    +2 de golpe, o un +1 que rebase el tope → **lote rechazado entero**.
  - **-1 en el mismo lote** que borra un miembro (salvo que el comercio
    desaparezca en el mismo lote: eliminación total del negocio). Nunca baja de 1.
- **Carrera**: el +1/-1 es un `update` sobre el **mismo** documento del comercio,
  que Firestore serializa con contención optimista. Dos altas simultáneas no
  pueden ambas incrementar 2→3: una reintenta y vuelve a chocar con el tope.

Cliente Android alineado (`app-android/.../nube/ComercioRepo.kt`):
- `crearComercio`: escribe `numDispositivos: 1` en el comercio.
- `unirseConCodigo`: escribe miembro + `FieldValue.increment(1)` en un
  `WriteBatch`. Mantiene además un gate previo (primera capa, UX) que lee el
  contador y tira `LimiteDispositivos(plan)` antes de intentar el lote.

Pruebas: `backend/test-reglas.mjs` cubre gratis=1 (rechaza el 2do), Caserito=3
(llena y rechaza el 4to), los trucos (+2 de golpe, miembro sin contador, salir
sin decrementar) y el decremento que libera cupo. `npm run test:reglas` en verde.

## PENDIENTES (huecos abiertos)

### 1. Migración de comercios legacy sin `numDispositivos` (IMPORTANTE)
Los comercios creados **antes** de este cambio no tienen el campo. La regla usa
`get('numDispositivos', 1)` como base, así que un comercio legacy con N miembros
reales se trataría como si tuviera **1**, permitiendo inflar hasta el tope+1
partiendo de una base falsa.

**Acción**: correr una migración única (script con Admin SDK, o backfill manual
desde la consola) que setee `numDispositivos = (nº real de docs en miembros)` en
cada comercio existente. Hasta que se corra, el tope no es fiable para comercios
previos a este despliegue. Los comercios nuevos ya nacen correctos.

### 2. Borrado de miembros: batch obligatorio (delete + increment(-1))
Quién puede borrar un doc de `miembros`: uno mismo, el dueño, o **un operador
solo si el doc es un `trabajador`** (nunca el dueño — desmantelaría el comercio;
si al doc le falta `rol` se trata como dueño, fail-safe). En las tres vías la
regla exige que el **mismo `WriteBatch`** baje `numDispositivos` en 1 (piso 1),
salvo que el comercio se borre entero en el mismo lote.

- **App Android** (aún no implementa "expulsar"/"salir"): cuando lo haga,
  `WriteBatch` con `delete(miembro)` + `update(comercio, "numDispositivos",
  increment(-1))`.
- **Panel del operador** (soporte, ya habilitado en reglas): igual —
  `delete(miembro)` + `increment(-1)` en un mismo batch. Solo trabajadores.
  Tests en `test-reglas.mjs`: operador quita trabajador con -1 (permitido),
  operador quita dueño (denegado), operador borra sin -1 (denegado).

### 3. Re-entrada de un miembro que ya existe
`unirseConCodigo` no vuelve a incrementar si el uid ya es miembro, pero re-escribir
el propio doc de miembro es un `update` que la regla actual solo permite al dueño
(`hasOnly(['puedeCapturar'])`). Es un caso preexistente (ya pasaba antes de este
cambio) y raro; si se vuelve real, ajustar la regla de `update` de miembro.

### 4. Degradación de plan con más miembros que el nuevo tope
Si un comercio en `patron` (10) con 8 teléfonos cae a `gratis` (1) por impago, el
contador queda en 8 > tope. Las reglas **no** expulsan a nadie (no se corta en
seco a mitad de jornada, por diseño). El efecto es solo que **no puede sumar más
teléfonos** hasta bajar de 1. Si se quisiera forzar la baja, eso es política de
producto y va en el panel/operador, no en reglas.

## Si algún día se pasa a la opción B (Cloud Function)
Cuando el proyecto migre a Blaze y tenga Functions, la vía más robusta es una
callable `unirseAComercio(codigo)` que en una transacción lea plan→tope, cuente
miembros reales (o el contador) y cree miembro + perfil solo si hay cupo. Eso
elimina la dependencia del contador denormalizado y del pendiente #1.
