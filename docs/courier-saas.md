# Software de Courier — Documento de diseño

> **Producto separado de PagoYa.** PagoYa es un producto masivo (bodegas, taxis,
> delivery, transporte) que vive en Firebase y se vende por licencia. Este
> software es un **SaaS operativo a medida SOLO para couriers** (ej. KCR Courier),
> vive en un **VPS propio** y se cobra como suscripción multi-cliente. Solo se
> tocan en un punto: el **check de pago Yape** (ver §11).

---

## 1. Objetivo

Reemplazar el Excel operativo del courier por un SaaS multi-cliente que:

- Registra pedidos de varias tiendas/marcas.
- **Asigna automáticamente** cada pedido a un motorizado según su distrito/zona.
- Da a cada rol su vista (dueño, seguimiento, contaduría, sistemas, motorizado).
- Controla la máquina de estados del pedido (entregado, ausente, rechazado…).
- Cuadra la caja con 3 flujos de plata (cobro cliente, tarifa tienda, tarifa moto).
- Confirma el pago Yape real vía PagoYa (anti "Yape falso").

## 2. Alcance

**Dentro:** registro de pedidos, catálogos, asignación automática, app del
motorizado, tablero de seguimiento, contaduría/liquidación, RBAC modular,
integración PagoYa, multi-tenant.

**Fuera (por ahora):** pasarela de pago propia, facturación electrónica SUNAT,
tracking GPS en vivo, app del cliente final. Se evalúan en fases posteriores.

## 3. Stack

```
Front (panel):  Next.js + TypeScript + TailwindCSS + shadcn/ui
Backend:        NestJS + TypeScript
ORM:            Prisma
Base de datos:  PostgreSQL (con Row-Level Security por tenant)
Tiempo real:    Redis + WebSocket (Socket.IO / NestJS Gateway)
App motorizado: Android nativo (Kotlin + Jetpack Compose)
Infra (VPS):    Docker Compose + Nginx (reverse proxy) + Certbot/Caddy (SSL)
Auth:           JWT (access + refresh), roles y org_id en el token
```

**Por qué NestJS y no Spring Boot:** un solo lenguaje (TypeScript) en front y
back → menos fricción para equipo chico, más liviano en el VPS, encaja con
Prisma/Postgres. Spring Boot solo se justificaría con equipo Java o exigencias
enterprise, que no es el caso hoy.

## 4. Arquitectura

```
┌─────────────────── VPS ───────────────────┐
│  Nginx (SSL)                               │
│    ├── Next.js (panel web)                 │
│    └── NestJS API  ── multi-tenant         │
│           ├── PostgreSQL (org_id en todo)  │
│           └── Redis (colas + realtime)     │
└────────────────────────────────────────────┘
        ▲                        ▲
        │ REST / WebSocket       │ webhook firmado (pago real)
        │                        │
┌───────┴────────┐      ┌────────┴───────────┐
│ App Android    │      │ PagoYa (Firebase)  │
│ del motorizado │      │  capta Yape/Plin   │
└────────────────┘      └────────────────────┘
```

## 5. Multi-tenancy

- Cada courier = una **Organización** (`org_id`).
- **Toda** tabla lleva `org_id`; **toda** query lo filtra. Se refuerza con
  **Row-Level Security (RLS)** en Postgres para que sea imposible cruzar datos
  entre couriers aunque haya un bug en el código.
- El `org_id` y el rol viven en el JWT; el backend nunca confía en el `org_id`
  que mande el cliente.
- Alta de un courier nuevo = crear una fila + su admin, sin nuevo servidor.

## 6. Roles y permisos (RBAC modular)

El **Administrador** (dueño) puede todo y **delega acceso a módulos y
submódulos**. Los 5 roles son plantillas por defecto, editables.

| Módulo / acción | Admin | Seguimiento | Contaduría | Sistemas | Motorizado |
|---|:--:|:--:|:--:|:--:|:--:|
| Registrar pedidos | ✅ | ✅ | — | ✅ | — |
| Asignar / reasignar | ✅ | ✅ | — | ✅ | — |
| Ver/gestionar ruta propia | ✅ | ✅ | — | — | ✅ (solo suya) |
| Cambiar estado + motivo | ✅ | ✅ | — | — | ✅ (solo suya) |
| Confirmar pagos / caja | ✅ | 👁️ ver | ✅ | — | — |
| Liquidación / reportes | ✅ | — | ✅ | — | — |
| Catálogos (tiendas, zonas, tarifas) | ✅ | — | — | ✅ | — |
| Usuarios y permisos | ✅ | — | — | 🔧 técnico | — |

- Permisos a nivel de **módulo Y submódulo** (ej. Contaduría *ve* el pedido
  pero no edita la dirección).
- Modelo: `Rol (org_id) → Permisos[]`, donde permiso = `modulo.accion`
  (`pedidos.crear`, `pedidos.asignar`, `caja.confirmar`, `reportes.ver`…).

## 7. Modelo de datos

### 7.1 Catálogos (dropdowns, adiós texto libre / typos)

- **Empresa/Tienda** — LOQUIEROYO, MAPLE STORE, NEXORA, VELORA, FERRANO, HOPE,
  BELLISIMUA, QUEHAYDENUEVO, YUME ONLINE, COMPRAPRIME…
- **Distrito** y **Provincia** — normalizados (evita "MOLINA" vs "LA MOLINA",
  "OLIVOS" vs "LOS OLIVOS").
- **Procedimiento** — REGULAR, STAR, EXPRESS, POS-REGULAR, POS-STAR, POS-EXPRESS,
  DEJAR, DEJAR-STAR, DEJAR-EXPRESS, RECOJO, TRASLADO, DEVOLUCION DE DINERO,
  SUBSANACIÓN.
- **Dimensiones** — SOBRE, PAQ.PEQUEÑO, PAQ.MEDIO, PAQ.GRANDE.
- **TipoEnvio** — PRODUCTO, DOCUMENTO.
- **Estado** — EN PROCESO, ENTREGADO, REPROGRAMADO, AUSENTE VISITADO, AUSENTE VIA
  LLAMADA, RECHAZADO VISITADO, RECHAZADO VIA LLAMADA, OTROS, SE MUDO,
  DESCONOCIDO, DIREC. NO EXISTE, PREDIO DESHABITADO, TERRENO BALDIO, FUERA DE ZONA.
- **Motivo** — lista larga ligada al estado (EXITOSO, NO SE ENCONTRO AL
  DESTINATARIO, POR CARACTERISTICA Y/O PRECIO, NO RESPONDE, CLIENTE SIN FONDOS,
  ENTREGADO ANTERIORMENTE, NO CORRESPONDE AL DISTRITO, ANULADO EN RUTA/BASE, etc.).
- **FormaPago** — EFECTIVO, MIXTO, YAPE, PLIN, PAGO A TIENDA, POS, BCP,
  CONTINENTAL, SCOTIABANK, INTERBANK.

### 7.2 Entidades principales

```
Organizacion   (courier)  → id, nombre, plan, yape_negocio, activo
Usuario        → id, org_id, nombre, email, rol_id, activo
Rol            → id, org_id, nombre, permisos[]      // RBAC modular
Motorizado     → id, org_id, usuario_id, nombre, estado (disponible/ruta/off)
Zona           → id, org_id, motorizado_id, distritos[]   // cobertura
Tienda         → id, org_id, nombre
Pedido         → id, org_id, n_envio, tienda_id, horario, procedimiento,
                 dimensiones, tipo_envio, contenido, caracteristica, unidades,
                 monto_cobrar, destinatario, direccion, referencia, celular,
                 distrito_id, provincia_id, indicacion_cliente, fecha,
                 estado, motivo, observacion, forma_pago,
                 tarifa_tienda, tarifa_moto, motorizado_id, n_seguimiento,
                 // derivados / automáticos:
                 estado_pedido (VENDIDO|DEVUELTO|NO_LLEVO),
                 pago_confirmado (bool), pago_ref_pagoya
Pago           → id, org_id, pedido_id, monto, origen (PAGOYA|EFECTIVO|OTRO),
                 ref_pagoya, confirmado_at
Liquidacion    → id, org_id, motorizado_id, fecha, total_cobrado,
                 total_tarifa_moto, total_a_rendir
```

### 7.3 Los 3 flujos de plata (clave para contaduría)

| Campo | Qué es | Dirección |
|---|---|---|
| `monto_cobrar` | lo que paga el cliente por el producto | cliente → tienda/marca |
| `tarifa_tienda` | lo que el courier cobra a la tienda por el envío | **ingreso courier** |
| `tarifa_moto` | lo que el courier paga al motorizado | **egreso courier** |

## 8. Máquina de estados del pedido

```
        ┌──────────────┐
        │  EN PROCESO  │ ← se crea y se asigna
        └──────┬───────┘
               │ el motorizado actúa
   ┌───────────┼───────────────┬───────────────┐
   ▼           ▼               ▼               ▼
ENTREGADO   AUSENTE/RECHAZADO  REPROGRAMADO    OTROS
   │           │               │               │
   ▼           ▼               ▼               ▼
 (pago?)     DEVUELTO       vuelve a          DEVUELTO
   │                        EN PROCESO
   ├─ pagado  → ✅ VENDIDO      (otra fecha)
   └─ no pagó → 🔄 DEVUELTO

Si nunca salió a ruta → ❌ NO_LLEVO
```

**`estado_pedido` es derivado, no manual** (a diferencia del Excel):

| Estado real | Pago | → estado_pedido |
|---|---|---|
| ENTREGADO | confirmado | ✅ VENDIDO |
| RECHAZADO / AUSENTE / OTROS | — | 🔄 DEVUELTO |
| no salió a ruta | — | ❌ NO_LLEVO |

## 9. Motor de asignación automática

Reemplaza a la persona que hoy reparte manualmente en el Excel.

1. **Regla base:** `pedido.distrito → Zona → Motorizado`. Al registrar, se
   asigna solo.
2. **Balanceo:** si un distrito tiene 2+ motorizados, reparte por carga (menos
   pedidos activos) u horario.
3. **Fallback:** distrito sin zona → "Sin asignar" para que Seguimiento resuelva
   (nunca se pierde un pedido).
4. **Fase 2 (fino):** geocodificar la dirección (muchas ya traen link de Google
   Maps) para ordenar ruta por cercanía, no solo por distrito.

**Config:** pantalla **Zonas** donde el Admin arrastra distritos a cada
motorizado. Cambió un rider → cambia la zona, no el código.

Ejemplo de zonas (de la data real de KCR):

```
WILMER    → ATE, CHOSICA, LURIGANCHO, LA MOLINA
ALONSO    → CARABAYLLO, COMAS, LOS OLIVOS, PUENTE PIEDRA, ANCON
LUIS      → JESÚS MARÍA, LINCE, MIRAFLORES, SAN ISIDRO
MIGUEL    → BARRANCO
YERALD    → RÍMAC, EL AGUSTINO
provincia → CHIMBOTE (fuera de Lima)
```

## 10. Módulos funcionales

1. **Registro de pedidos** — alta con catálogos; carga masiva (importar Excel) en
   la migración inicial.
2. **Asignación** — automática + tablero para reasignar manualmente.
3. **App del motorizado (Android)** — login, "mis pedidos del día", ver monto a
   cobrar, marcar estado + motivo, registrar forma de pago.
4. **Seguimiento** — tablero del día por motorizado/distrito/estado.
5. **Contaduría / liquidación** — cierre de caja, cuánto rinde cada motorizado,
   ingresos por tarifa tienda, egresos por tarifa moto.
6. **Catálogos** — tiendas, distritos, zonas, tarifas, procedimientos.
7. **Usuarios y permisos** — roles y accesos por módulo/submódulo.

## 11. Integración PagoYa (único punto de contacto)

Confirma que el Yape del cliente **cayó de verdad** al Yape del negocio.

```
Cliente paga Yape → PagoYa capta la notif real (Firebase)
  → Cloud Function hace POST firmado:
    POST /webhooks/pagoya
    { org_id, monto, billetera, ts, hmac }
  → NestJS busca pedido EN_PROCESO de ese org con ese monto (ventana de tiempo)
  → marca pago_confirmado ✅ y estado_pedido VENDIDO
  → emite evento realtime (Redis → WebSocket) al panel y a la app del motorizado
```

- **Seguridad:** webhook firmado con **HMAC** (secreto por org). El ERP **nunca
  crea pagos**, solo los recibe → respeta la regla anti-fake de PagoYa.
- **Matching:** por monto + ventana de tiempo + motorizado en ruta. Ambigüedad
  (dos pedidos del mismo monto) → queda "por confirmar" y lo resuelve la dueña.
- PagoYa se vende como licencia aparte; aquí es opcional pero es el gancho.

## 12. Deploy en el VPS

```
docker-compose:
  nginx        (reverse proxy + SSL)
  nestjs-api
  next-panel
  postgres     (volumen persistente + backups diarios)
  redis
Certbot/Caddy para SSL · .env para secretos (JWT, HMAC PagoYa, DB) · backups
automáticos de Postgres.
```

## 13. Roadmap por fases

- **Fase 1 — MVP SaaS (3–5 sem):** multi-tenant + registro + catálogos +
  asignación automática por distrito + app motorizado (login, mis pedidos,
  estado/motivo, forma de pago) + panel seguimiento + caja manual +
  importación del Excel actual.
- **Fase 2:** webhook PagoYa (pago automático) + tiempo real + contaduría /
  liquidación + reportes.
- **Fase 3:** geocodificación y orden de ruta, evaluación de fusión de apps
  (una sola app rider PagoYa+courier), onboarding self-service para vender a
  más couriers, tracking.

## 14. Riesgos y decisiones abiertas

- **Dos apps en el celular del motorizado** (PagoYa + courier). Decisión actual:
  arrancar con dos apps separadas; evaluar fusión en Fase 3.
- **Calidad de datos del Excel** (typos, distritos inconsistentes, direcciones
  sin número). Mitigación: catálogos obligatorios y validación en el registro.
- **Matching de pagos ambiguos** (mismo monto, mismo momento). Mitigación:
  estado "por confirmar" + resolución manual.
- **Migración inicial** desde el Excel: definir importador y mapeo de columnas.

---

_Última actualización: 2026-08-06._
