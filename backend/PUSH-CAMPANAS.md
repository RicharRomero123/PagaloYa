# Push de campañas desde la consola de Firebase (arranque gratis, sin Blaze)

Cómo enviar avisos, ofertas y recordatorios a los teléfonos de PagoYa **sin
Cloud Functions y sin plan Blaze**. En plan Spark todo se resuelve con la
consola de Firebase Cloud Messaging (FCM) apuntando a **topics**.

> Regla de oro (CLAUDE.md): esto NO son pagos. Las campañas son mensajes de
> marketing/soporte del equipo PagoYa. Los pagos siguen naciendo SOLO de
> notificaciones reales capturadas por la app. Un push de campaña nunca crea un
> pago ni suena como uno.

---

## 1. Cómo funciona: topics FCM

La app Android se suscribe (lo implementa el cliente aparte, no el backend) a
estos **topics** al iniciar sesión, según el plan del comercio:

| Topic            | Quiénes lo reciben                                  |
|------------------|----------------------------------------------------|
| `todos`          | TODOS los teléfonos con la app instalada           |
| `plan_gratis`    | Comercios en plan Gratis                            |
| `plan_caserito`  | Comercios en plan Caserito                          |
| `plan_patron`    | Comercios en plan Patrón                            |

Con topics no necesitamos guardar ni manejar tokens FCM para campañas: la
consola envía a un topic y FCM lo reparte. Es gratis y escala solo.

**Segmentar = elegir el topic correcto:**

- Aviso general (mantenimiento, novedad) → `todos`.
- Empujar upgrade a quien está en Gratis → `plan_gratis`.
- Recordatorio de cierre del día o tip de uso a pagantes → `plan_caserito` y/o
  `plan_patron`.
- Nunca ofrezcas "sube a Caserito" a `plan_patron` (ya están arriba): elige bien
  el topic para no quedar mal.

> Nota técnica sobre el cambio de plan: cuando un operador cambia el plan de un
> comercio, la app debe re-suscribir el teléfono al topic nuevo y desuscribir
> del viejo. Eso es trabajo del cliente Android. Si un teléfono quedó en el topic
> viejo por unos minutos, no pasa nada grave para una campaña; solo evita mandar
> ofertas contradictorias el mismo día de una migración masiva.

---

## 2. Enviar una campaña (paso a paso en la consola)

1. Entra a **Firebase Console** → proyecto de PagoYa.
2. Menú lateral: **Messaging** (Cloud Messaging / "Interacción").
3. Botón **Crear campaña** → **Notificaciones de Firebase** (o **Enviar mensaje
   de prueba** si primero quieres probarlo en tu propio teléfono).
4. **Título** y **Texto** del mensaje (ver textos criollos abajo).
5. **Siguiente** → **Destino**: elige **Tema (topic)** e ingresa el topic exacto,
   por ejemplo `todos` o `plan_gratis`.
   - (También se puede segmentar por app/idioma/país, pero para PagoYa el topic
     es lo que usamos.)
6. **Programación**: "Ahora" o programa fecha/hora (ver horario recomendado).
7. Revisa y **Publicar**.

### Probar antes de disparar a todos
Usa **"Enviar mensaje de prueba"** con el token FCM de tu propio teléfono de
prueba (lo imprime la app en modo debug). Así ves el título, el cuerpo y que no
se vea cortado, antes de mandarlo a miles.

---

## 3. Buenas prácticas (no quemar la lista)

- **No spamear.** Máximo 1 campaña útil por semana en `todos`. Un aviso de más y
  la gente desactiva las notificaciones (y ahí perdemos también el canal).
- **Horario.** Comercios peruanos: manda entre **9:00 y 20:00**. Nada de
  madrugadas. Para "recordatorio de cierre del día", cae bien entre **19:00 y
  20:00**, cuando están cuadrando la caja.
- **Un solo mensaje, una sola idea.** Título corto, cuerpo de una línea.
- **Tono criollo** (ver BRAND.md): hablamos como el comerciante, no como un banco.
- **Nada de datos personales** en el texto (Ley 29733). Las campañas son masivas:
  jamás pongas nombres de pagadores ni montos de nadie.
- **No imites a Yape** ni su morado/logo en imágenes de la campaña.

### Ejemplos de texto (criollos, BRAND.md)

**Aviso general (`todos`)**
- Título: `¡PagoYa está fino!`
- Texto: `Actualizamos la app pa' que tus pagos suenen más rápido, casero.`

**Recordatorio de cierre del día (`plan_caserito` / `plan_patron`)**
- Título: `¿Ya cuadraste tu caja?`
- Texto: `Mira el total del día en PagoYa y cierra tranquilo.`

**Empujar upgrade (`plan_gratis`)**
- Título: `Tu gente también puede escuchar los pagos`
- Texto: `Con el Plan Caserito, tú en casa y tu chamba en la tienda oye cada Yape. Pregúntanos.`

**Oferta / promo (`plan_gratis`)**
- Título: `Chócala, casero`
- Texto: `Este mes el Plan Caserito con descuento. Escríbenos y te contamos.`

---

## 4. Fase 2: botón "Enviar desde el panel"

Enviar campañas **desde el panel web del operador** con un botón queda para
**FASE 2**. NO se hace en Spark. Requiere:

- **Plan Blaze** (pago por uso).
- Una **Cloud Function** que reciba la solicitud del panel y llame al **Admin
  SDK** (`messaging().send(...)` a un topic) desde el servidor.

**Por qué no se puede hoy en el navegador del panel:** enviar a un topic requiere
credenciales de servidor (la *server key* / Admin SDK). Esa clave **no puede
vivir en el navegador**: cualquiera abriría las DevTools, la copiaría y mandaría
push a nombre de PagoYa a toda la base. Por eso el envío autenticado va SIEMPRE
detrás de una Cloud Function, nunca en el cliente del panel.

Mientras tanto, la consola de Firebase (sección 2) cubre el 100% de la necesidad
de campañas sin costo.
