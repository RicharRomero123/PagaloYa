# Continuación — Silencio, Horario y Mute remoto (2026-08-30)

Handoff para retomar luego. Feature completa y desplegada salvo el merge a `main` (Vercel) y la subida del AAB a Play.

## Qué se construyó

Tres capacidades nuevas, con la regla anti-fake intacta (silenciar SOLO calla la voz local; el historial se guarda igual y el reenvío por FCM sigue):

1. **Silencio individual** (app): el trabajador silencia su teléfono sin perder permisos ni historial. Config → sección "Silencio".
2. **Horario de anuncios** (app): franja inicio–fin en que la app habla; apagado = habla a toda hora; soporta cruce de medianoche. Config → sección "Negocio" → pantalla "El horario de tu caja".
3. **Mute remoto desde el panel**: el operador apaga/enciende la voz de un teléfono; el teléfono obedece al instante vía listener de Firestore. El trabajador puede revertir desde su app (última escritura gana).

## Contrato de datos (clave)

Doc `comercios/{comercioId}/miembros/{uid}`, campos planos:
- `silenciado` bool (default false)
- `horarioActivo` bool (default false)
- `horarioInicio` int = minutos del día 0..1439 (default 480 = 08:00)
- `horarioFin` int (default 1320 = 22:00)

Fuente de verdad del comportamiento = local (SharedPreferences `pagoya_config`). Firestore es el espejo que ve/manda el panel. El listener del teléfono NUNCA reescribe (evita bucle).

## Diseño técnico

- **Gate único**: `Anunciador.anunciarPago()` → `if (!PreferenciasVoz.deboHablarAhora(ctx)) return`. Pánico y "probar voz" NO pasan por el gate. El historial (`RegistroPagos.agregar`) ya se guardó antes en captura y en escucha remota; `subirPago` (FCM) es independiente.
- **Listener remoto**: `ComercioRepo.escucharPreferencias()` cuelga `addSnapshotListener` del doc de miembro (arranca en `ServicioPrimerPlano.prepararModoEscucha`, se suelta en `limpiar()`). Espeja solo los campos presentes a `PreferenciasVoz`.
- **Reglas**: `allow update` del doc de miembro tiene 3 caminos → (1) dueño cambia `puedeCapturar`; (2) el propio miembro cambia sus 4 prefs; (3) `esOperador()` cambia SOLO `silenciado`. Con validación tipo/rango. Tests en `backend/test-reglas.mjs`.

## Archivos tocados

**app-android/**
- `app/src/main/java/pe/pagoya/app/core/voz/PreferenciasVoz.kt` — prefs + `deboHablarAhora()`
- `app/src/main/java/pe/pagoya/app/core/Anunciador.kt` — gate
- `app/src/main/java/pe/pagoya/app/nube/ComercioRepo.kt` — `sincronizarEscucha()` + listener (`escucharPreferencias`/`dejarDeEscucharPreferencias`)
- `app/src/main/java/pe/pagoya/app/servicio/ServicioPrimerPlano.kt` — arranca listener
- `app/src/main/java/pe/pagoya/app/ui/mas/PantallaMas.kt` — secciones "Silencio" y "Negocio"
- `app/src/main/java/pe/pagoya/app/ui/mas/PantallaHorario.kt` — NUEVA pantalla de horario
- `app/build.gradle.kts` — versión 0.4.0 / versionCode 5

**backend/**
- `firestore.rules` — 3er camino operador + prefs del miembro
- `test-reglas.mjs` — casos nuevos

**panel/**
- `src/lib/membresia.ts` — tipo `Miembro` + `minutosAHora()` + `establecerSilenciado()`
- `src/componentes/FichaComercio.tsx` — badges silenciado/horario + toggle de mute

## Estado de despliegue

| Componente | Estado |
|---|---|
| Reglas Firestore | ✅ Desplegadas (`firebase deploy --only firestore:rules` desde backend/) |
| Cloud Functions (6) | ✅ Desplegadas (trialAlCrearComercio, fanoutPago, notificarCourier, canjearReferido, enviarCampana, vigilarCiego) |
| Panel en Firebase Hosting | ✅ https://pagoya-45018.web.app |
| Front en Vercel (main) | ⏳ PENDIENTE: mergear PR dev→main (auto-deploy) |
| AAB Android 0.4.0 | ✅ Firmado, listo para Play |

## Git

- Rama de trabajo: `dev` — todo commiteado y pusheado (HEAD `d7fd442b`).
- `main` está protegida (exige PR; no acepta push directo). `gh` NO está instalado.
- Commits de esta tanda en dev:
  - `dc13d779` feat(app-android): silencio individual y horario
  - `6c53b36d` feat(backend): reglas prefs de escucha del miembro
  - `cbad7847` feat(panel): badges silenciado/horario en la ficha
  - `fb0cee49` feat(app-android): obedecer mute remoto en tiempo real
  - `82e9a360` feat(backend): operador mutea cualquier miembro
  - `53753905` feat(panel): toggle mute
  - `d7fd442b` chore(app-android): versión 0.4.0 (versionCode 5)
- **Cambios sueltos en el árbol que NO son de esta feature** (no tocar sin revisar): `.gitignore`, `REDES.md` modificados; `landing/public/index.html` sin trackear.

## PENDIENTE al retomar

1. **Vercel**: abrir y mergear PR → https://github.com/RicharRomero123/PagaloYa/compare/main...dev?expand=1 (al mergear, Vercel publica el front). Alternativa: instalar `gh` (`winget install GitHub.cli`) para hacerlo por CLI.
2. **Play Console**: subir `app-android/app/build/outputs/bundle/release/app-release.aab` (8.6 MB, versionCode 5 / 0.4.0, firmado con upload key CN=PagoYa). Usa Play App Signing.
3. **Deuda técnica** (no urgente): migrar Cloud Functions a **Node 22** (Node 20 se decomisiona 2026-10-30) y actualizar `firebase-functions` a última.
4. **App vieja**: el mute remoto solo se aplica al instante en teléfonos con el APK nuevo (el del listener). Los del APK viejo no obedecen la orden remota hasta actualizar.

## Notas de versión para Play (es-419)

```
¡Ahora tú mandas cómo suena tu caja!

• Silenciar este teléfono: apágalo el día que no lo uses. No habla, pero igual guarda cada pago en tu historial y no pierde ningún permiso.
• Horario de anuncios: elige a qué hora empieza y deja de hablar. Fuera de tu horario descansa la voz, sin perderte ninguna venta.
• Tu equipo, más ordenado desde el panel.

Mejoras de estabilidad. ¡Gracias por yapear con PagoYa!
```

## Seguridad

Las claves del keystore se compartieron en el chat de la sesión. El `.jks` (`pagoya-upload.jks`) y `keystore.properties` siguen fuera del repo (`.gitignore`). Recomendado: guardar `.jks` + claves en gestor de contraseñas; si preocupa, Play permite restablecer la upload key por soporte.
