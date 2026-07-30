# PagoYa — App Android

App nativa Kotlin. Hito 1: capturar notificación de billetera → parsear → anunciar por voz.

## Probar en tu teléfono

1. Abrir `app-android/` en Android Studio (o `.\gradlew.bat assembleDebug`).
2. Conectar el teléfono con **depuración USB** activada y darle Run ▶.
3. En la app, activar los 3 permisos que pide la pantalla:
   - Escuchar notificaciones (te lleva a Ajustes → acceso a notificaciones → PagoYa)
   - Notificaciones de PagoYa (Android 13+)
   - Ignorar optimización de batería
4. Probar sin Yape: botón **"Simular yapeo"** — recorre el flujo completo
   (parser → registro → voz) con una notificación de prueba.
5. Probar con Yape real: yapearte desde otra cuenta 0.10 céntimos y escuchar.

## Afinar los patrones con notificaciones reales

Los textos exactos de las notificaciones de Yape varían por versión. Si un yapeo
real NO suena, la notificación quedó guardada en el **modo aprendizaje**
(`files/aprendizaje.jsonl` del dispositivo). Para leerla:

```
adb shell run-as pe.pagoya.app cat files/aprendizaje.jsonl
```

Con ese texto real se ajusta el regex en `app/src/main/assets/billeteras.json`
(grupo 1 = pagador, grupo 2 = monto). Ese mismo esquema pasará a Firebase Remote
Config en el siguiente hito, para actualizar patrones sin republicar.

## Estructura

- `core/BilleteraParser.kt` — parser modular multi-billetera (patrones desde JSON)
- `core/Anunciador.kt` — TTS es-PE + "voz fuerte" (volumen máx al anunciar)
- `core/Pago.kt` — registro local de pagos (fuente de la UI)
- `core/Aprendizaje.kt` — log de notificaciones no reconocidas
- `servicio/EscuchaNotificaciones.kt` — NotificationListenerService (el corazón)
- `servicio/ServicioPrimerPlano.kt` — mantiene vivo el proceso
- `servicio/ReceptorArranque.kt` — re-arranca tras reinicio del teléfono

## Siguiente hito

Firebase: Auth, subir pagos, FCM a teléfonos "escucha" (trabajadores/dueño remoto),
Remote Config para patrones.
