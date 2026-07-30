# PagoYa — Backend (Firebase)

Proyecto Firebase: `PagoYa` (consola: console.firebase.google.com)

## Modelo de datos (Firestore)

```
usuarios/{uid}                       → { comercioId }
codigos/{codigo6digitos}             → { comercioId }
comercios/{id}                       → { nombre, duenoUid, codigoVinculacion, creadoEn }
comercios/{id}/miembros/{uid}        → { rol: "dueno"|"trabajador", nombre }
comercios/{id}/pagos/{pagoId}        → { billeteraId, billeteraNombre, pagador,
                                         monto, timestamp, origenUid }
```

- `pagoId` es determinista (`uid-timestamp-centavos`) → subir dos veces la misma
  notificación no duplica el pago.
- Los pagos **no se editan ni borran** (regla anti-fake: el registro es inmutable).

## Cómo funciona el tiempo real (sin Cloud Functions, plan gratis)

- El teléfono del dueño captura la notificación → la sube a `pagos/`.
- Los teléfonos "escucha" (trabajadores) mantienen un listener de Firestore desde
  el servicio de primer plano → anuncian por voz cada pago nuevo que no capturaron
  ellos mismos. Latencia típica: ~1 segundo.
- Más adelante (plan Blaze): Cloud Function + FCM para despertar teléfonos aunque
  el sistema haya matado el proceso, y App Check para endurecer el anti-fake.

## PASO PENDIENTE MANUAL: publicar las reglas de seguridad

1. Consola Firebase → **Firestore Database → Reglas**.
2. Borrar lo que haya y pegar el contenido completo de `firestore.rules`.
3. **Publicar**.

Sin este paso, la base queda cerrada (modo producción rechaza todo) y la app
no podrá guardar nada.
