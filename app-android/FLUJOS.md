# PagoYa — Mapa de flujos de la app

> Cómo se mueve el usuario por la app y qué pasa por dentro en cada paso.
> Si vas a tocar una pantalla, lee primero el flujo al que pertenece.

## 1. Etapas de nivel superior

Las controla `MainActivity.kt` (`RaizPagoYa`). Fuera del armazón de pestañas,
porque son pasos de una sola vez.

```
  CARGANDO
     │  ¿hay sesión?
     ├── no ──► LOGIN ──────────────┐
     │                              │
     └── sí ─► ¿tiene comercio?     │
                 ├── no ──► COMERCIO ◄┘
                 │             │
                 └── sí ───────┤
                               ▼
                    ¿ya vio la bienvenida?
                      ├── no ──► BIENVENIDA (carrusel de 3)
                      │              │
                      └── sí ────────┤
                                     ▼
                        ¿faltan permisos para su rol?
                          ├── sí ──► PERMISOS (asistente)
                          │              │
                          └── no ────────┤
                                         ▼
                                    PRINCIPAL
                              (bottom nav de 4 pestañas)
```

Vuelta atrás: desde **Más → Revisar ahora** se puede volver a `PERMISOS` en
cualquier momento; al terminar, regresa a `PRINCIPAL`.

## 2. Alta del dueño

```
LOGIN                    PantallaLogin
  │  Google o correo      Sesion.entrarConGoogle / entrarConCorreo
  ▼
COMERCIO                 PantallaComercio → elige "Es mi negocio"
  │  escribe el nombre
  ▼
ComercioRepo.crearComercio(nombre)
  ├─ comercios/{id}                 { nombre, duenoUid, codigoVinculacion }
  ├─ .../miembros/{uid}             { rol: dueno, puedeCapturar: true }
  ├─ codigos/{6 dígitos}            { comercioId }
  └─ usuarios/{uid}                 { comercioId }
  ▼
BIENVENIDA → PERMISOS (los 4) → PRINCIPAL
```

Los cuatro pasos van etiquetados: si una regla de Firestore rechaza uno, el
error dice cuál (`[codigo] ...`). Es lo que hace diagnosticable el alta.

## 3. Alta del trabajador

```
LOGIN
  ▼
COMERCIO → elige "Trabajo aquí" → escribe el código de 6 dígitos
  ▼
ComercioRepo.unirseConCodigo(codigo)
  ├─ lee codigos/{codigo} → comercioId
  ├─ .../miembros/{uid}   { rol: trabajador, puedeCapturar: false }
  └─ usuarios/{uid}       { comercioId }
  ▼
BIENVENIDA → PERMISOS (solo 2: avisos + batería) → PRINCIPAL
```

**Su teléfono nunca captura.** No se le pide acceso a notificaciones ni blindaje
de Yape porque no le sirven: sus pagos llegan de la nube. Y el servidor le
rechaza cualquier intento de escribir un pago (ver `backend/firestore.rules`).

## 4. Onboarding de permisos

`ui/onboarding/` — un permiso por pantalla, no avanza hasta tenerlo.

| Permiso | ¿Quién lo necesita? | Se verifica solo |
|---|---|---|
| Escuchar notificaciones | solo captura | sí |
| Avisos de PagoYa (Android 13+) | todos | sí |
| Batería sin restricción | todos | sí |
| Blindar Yape (por marca) | solo captura, si Yape está instalado | **no** |

```
recordarPermisos(captura)
   │  calcula requeridos y faltantes
   │  se recalcula en cada ON_RESUME  ◄── clave: el usuario vuelve de Ajustes
   ▼
AsistentePermisos muestra faltantes.first()
   │
   ├── "Activar ahora"  → Permisos.abrirAjuste(...) → Ajustes del sistema
   │                       (al volver, ON_RESUME → si ya está, avanza solo)
   │
   └── "Ya lo activé"   → refrescar()
                           ├── concedido → avanza
                           └── no        → reclamo en rojo
```

El blindaje por marca (`ProteccionMarca`) es el único que se confía al usuario,
porque el sistema no expone si está activo. Se marca con "Ya lo hice".

## 5. Camino crítico: de la notificación a la voz

Este es el flujo que sostiene la promesa del producto. Nada más crea pagos.

```
Yape muestra su notificación
   ▼
EscuchaNotificaciones.onNotificationPosted
   ├─ ¿el paquete está en la allowlist?          no → se ignora
   ├─ arma título + texto + bigText
   ├─ dedup por huella (ventana de 30 s)
   ▼
BilleteraParser.parsear(paquete, texto, ts)
   ├─ null → Aprendizaje.registrar(...)  (para afinar patrones después)
   ▼
   Pago(billetera, pagador, monto, timestamp)
   ├─► RegistroPagos.agregar    → alimenta Inicio y Caja
   ├─► Anunciador.anunciarPago  → sello grabado + voz elegida
   │                              sin nombre por defecto (ver VOZ.md)
   └─► ComercioRepo.subirPago  → Firestore (solo si puedeCapturar)
                                  id = uid-timestamp-centavos
                                  recibidoEn = hora del SERVIDOR
```

Los patrones vienen de Remote Config (`billeteras_json`) con los de
`assets/billeteras.json` como respaldo sin internet.

## 6. Modo escucha (el dueño remoto)

```
ServicioPrimerPlano.onStartCommand
   ▼
ComercioRepo.escucharPagos { pago -> registrar + anunciar }
   │
   │  query: pagos ordenados por recibidoEn (hora del servidor), limit 30
   │
   ├─ primera foto  → se marca todo como visto, NO se anuncia
   │                  (esos pagos ya sonaron cuando cayeron)
   └─ de ahí en adelante → cada ADDED nuevo que no sea propio → suena
```

**Sin relojes.** No se filtra por hora del teléfono: da igual que el equipo del
trabajador esté adelantado o atrasado respecto al del dueño.

## 7. Guardián de Yape

```
ServicioPrimerPlano.vigilarYape   (cada 30 min)
   ▼
Guardian.estadoYape → FLAG_STOPPED?
   ├─ DETENIDA → anuncio hablado: "Tu Yape está apagado..."
   │             + tarjeta roja en Inicio con "Abrir Yape ahora"
   ├─ OK / NO_INSTALADA → nada
```

Es la contramedida al fallo más común: el sistema (o el usuario) mata Yape y las
notificaciones dejan de llegar sin que nadie se entere hasta que falta plata.

## 8. Cierre de caja

```
Pestaña Caja
   ├─ total de hoy (grande, azul noche)
   ├─ desglose por billetera → para cuadrar contra cada app
   ├─ historial agrupado por día ("Hoy", "Ayer", fecha)
   └─ "Compartir cierre del día" → texto plano → WhatsApp
```

Se alimenta de `RegistroPagos` (local, últimos 100). El historial completo del
comercio vive en Firestore y sale en el panel web.

## 9. Módulos de la UI

```
ui/
├── tema/                 sistema de diseño — TOCAR ESTO ANTES QUE UNA PANTALLA
│   ├── Colores.kt        paleta de marca
│   ├── Tipografia.kt     Nunito + escala (montos gigantes, nada bajo 12 sp)
│   ├── Tema.kt           TemaPagoYa: esquema, formas, solo modo claro
│   └── Componentes.kt    BotonPagoYa, TarjetaPagoYa, Aviso, FilaPago, formato…
├── Navegacion.kt         armazón: bottom nav de 4 + pantalla de carga
├── acceso/               PantallaLogin, PantallaComercio
├── onboarding/           Permisos (lógica), Bienvenida (carrusel), Asistente
├── inicio/               ¿estoy escuchando? · ¿cuánto llevo hoy? · ¿qué cayó?
├── caja/                 cierre del día e historial
├── equipo/               código de vinculación y quién escucha
└── mas/                  voz, estado de permisos, plan, sesión
```

Regla: **ninguna pantalla define colores, tamaños ni botones propios.** Todo sale
de `ui/tema/`. Si algo falta, se agrega ahí y se usa desde todas.

## 10. Criterios de diseño (por qué se ve así)

- **Nada de parecerse a Yape.** Naranja #FF6B1A y azul noche #1A2B4A. Copiamos
  los *patrones de uso* de las apps de billetera (botones enormes, una acción
  por pantalla, montos gigantes), nunca su identidad visual.
- **Solo modo claro.** Se usa en mostradores con sol encima y teléfonos baratos.
- **Botones de 56 dp.** Se tocan con las manos ocupadas y apuradas.
- **Emoji en círculo de color** como ilustración: cálido, criollo, cero peso en
  el APK y no depende de un diseñador para cada pantalla nueva.
- **El estado antes que el dato.** Lo primero en Inicio no es el monto: es si la
  caja está escuchando. Si está muda, todo lo demás no sirve.

## 11. Pendientes de estos flujos

- El dueño no puede aún habilitar la captura de un segundo teléfono desde la
  app (las reglas ya lo permiten; falta el botón en Equipo).
- Sacar a un trabajador del comercio: la regla existe, la UI no.
- El historial de Caja es local (últimos 100 pagos de este teléfono), no el del
  comercio completo.
- Plan y suscripción son informativos: no hay cobro todavía.
