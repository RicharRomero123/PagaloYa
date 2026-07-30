# PagoYa — App Android

App nativa Kotlin + Compose. Captura la notificación real de la billetera,
la anuncia por voz y la reparte a los teléfonos del equipo.

📄 **Antes de tocar una pantalla, lee [`FLUJOS.md`](FLUJOS.md)** — ahí está el
mapa de cómo se mueve el usuario y qué pasa por dentro en cada paso.
🔊 **Antes de tocar el audio, lee [`VOZ.md`](VOZ.md)** — por qué suena a robot y
cómo llegar a calidad de soundbox.

## Probar en tu teléfono

1. Abrir `app-android/` en Android Studio (o `.\gradlew.bat assembleDebug`).
2. Conectar el teléfono con **depuración USB** activada y darle Run ▶.
3. La app te lleva sola por el alta: entrar → decir qué es este teléfono →
   carrusel de bienvenida → asistente de permisos.
4. Probar sin Yape: en **Más → Probar la voz**.
5. Probar con Yape real: yapearte desde otra cuenta 0.10 céntimos y escuchar.

## Afinar los patrones con notificaciones reales

Los textos exactos de las notificaciones de Yape varían por versión. Si un yapeo
real NO suena, la notificación quedó guardada en el **modo aprendizaje**
(`files/aprendizaje.jsonl` del dispositivo). Para leerla:

```
adb shell run-as pe.pagoya.app cat files/aprendizaje.jsonl
```

Con ese texto real se ajusta el regex en `app/src/main/assets/billeteras.json`
(grupo 1 = pagador, grupo 2 = monto) y, sobre todo, en **Firebase Remote Config**
(parámetro `billeteras_json`), que es lo que manda en producción y permite
corregir sin republicar el APK.

## Estructura

```
core/        lógica que no sabe de pantallas
  BilleteraParser · Anunciador (fachada de voz) · Pago/RegistroPagos
  Aprendizaje · Guardian (Yape detenido) · ProteccionMarca (autostart por marca)
  voz/       CatalogoVoces (elige la mejor voz del teléfono) · PreferenciasVoz
             SelloSonoro (el "¡PagoYa!" grabado) — ver VOZ.md

servicio/    lo que mantiene la app viva
  EscuchaNotificaciones (NotificationListenerService — el corazón)
  ServicioPrimerPlano (proceso vivo + oído en la nube + guardián)
  ReceptorArranque (re-arranca tras reiniciar el teléfono)

nube/        Firebase
  Sesion (auth) · ComercioRepo (comercio, miembros, pagos, modo escucha)
  AppCheckPagoYa (src/debug y src/release — anti-fake de raíz)

ui/          ver FLUJOS.md §9
  tema/      sistema de diseño: colores, tipografía, componentes
  acceso/ onboarding/ inicio/ caja/ equipo/ mas/
  Navegacion.kt — bottom nav de 4
```

## Sistema de diseño

Todo lo visual sale de `ui/tema/`. **Ninguna pantalla define colores, tamaños ni
botones propios**: si falta una pieza, se agrega en `Componentes.kt` y se usa
desde todas.

- Naranja `#FF6B1A` + azul noche `#1A2B4A` sobre crema. Nunca el morado de Yape
  ni el turquesa de Plin: copiamos los *patrones de uso* de las apps de billetera
  (botones enormes, una acción por pantalla, montos gigantes), jamás su marca.
- Tipografía Nunito por Google Fonts. Si el teléfono no puede descargarla, cae
  sola a la del sistema. Para fijarla en el APK: poner los `.ttf` en `res/font/`
  y cambiar `FuentePagoYa` en `Tipografia.kt`.
- Solo modo claro: se usa en mostradores con sol encima.

## Siguiente hito

FCM + Cloud Functions para despertar teléfonos aunque el sistema haya matado el
proceso, y panel web del dueño con el historial completo del comercio.
