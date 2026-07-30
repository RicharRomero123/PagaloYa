# PagoYa — Panel de operador

Consola interna: membresías, cobertura, resellers. **No es el panel del
comerciante** — sus reportes están en la app móvil.

📄 Especificación completa: [`../PANEL.md`](../PANEL.md)

## Estado

Pasos 1–3 de 8 (ver `PANEL.md §12`): rol operador en las reglas, login con lista
de comercios, y **ficha con cobro y activación de membresía**.

**Con esto ya puedes cobrar.** Lo que sigue es el alta de campo con GPS.

### Cómo cobrar

Toca un comercio → se abre su ficha → elige plan, tiempo y método de pago →
"Cobrar y activar". Se escriben dos cosas **en un solo lote**: la suscripción y
el asiento en `pagosMembresia`. Si se hicieran por separado y fallara el segundo,
tendrías un comercio activo sin registro de quién cobró.

Detalle que importa: **renovar no le quita días al cliente**. Si todavía le
quedan 10 días, el nuevo periodo arranca cuando vence lo que ya pagó, no hoy.

También hay **"Dar 30 días de prueba"**: activa el plan sin generar cobro. Es lo
que vas a usar en el beta cerrado de 10–20 comercios.

## Arrancar

**1. Crear la app web en Firebase** (si no existe): consola → ⚙ Configuración
del proyecto → Tus apps → **Agregar app → Web**. Copia la configuración.

**2. Variables de entorno:**

```bash
cd panel
cp .env.local.ejemplo .env.local   # y llena los valores
npm install
npm run dev                        # http://localhost:3000
```

**3. Crearte como operador.** Sin esto el panel te va a rechazar aunque
inicies sesión bien — es la puerta cerrada funcionando. Los pasos están en
[`../backend/README.md`](../backend/README.md), "PASO PENDIENTE MANUAL 2".

En resumen: consola → Authentication → Users → copia tu UID → Firestore →
colección `operadores` → documento con ese UID y un campo `nombre`.

> Si entras y ves "Esta cuenta no tiene acceso", la pantalla te muestra tu UID
> para que lo copies directo.

## Compilar

```bash
npm run build     # genera out/ (estático)
npm run servir    # sirve out/ para revisar antes de desplegar
```

## Estructura

```
src/
├── app/            layout, globals.css y la página raíz (el enrutador de estados)
├── componentes/    Login · SinAcceso · ListaComercios
└── lib/
    ├── firebase.ts   inicialización
    ├── sesion.ts     auth + verificación de operador (useSesion)
    ├── comercios.ts  lectura y estado de membresía
    └── formato.ts    soles y fechas en es-PE
```

## Reglas de esta carpeta

- **Export estático.** Nada de SSR ni API routes: eso despliega a Cloud Run y
  obliga al plan Blaze. `next.config.mjs` ya tiene `output: "export"`.
- **El panel jamás crea, edita ni borra pagos.** Las reglas de Firestore ya lo
  impiden; que quede claro también aquí.
- **Cuidado con la cuota**: Spark da ~50 000 lecturas al día. Consulta siempre
  con `limit`, usa `getDocs` en vez de `onSnapshot` salvo donde el tiempo real
  aporte de verdad (mapa y salud). La lista está topada en 200 a propósito.
- Mobile-first: el reseller lo va a usar parado en un mercado, con una mano.

## Antes de desplegar a Firebase Hosting

Agrega el dominio en consola → Authentication → Settings → **Dominios
autorizados**, o el login con Google va a fallar. `localhost` ya viene
autorizado.
