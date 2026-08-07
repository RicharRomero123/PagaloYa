import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

/**
 * Datos de firma del APK de release. Viven en `app-android/keystore.properties`,
 * que NO va al repositorio (ver .gitignore). Si el archivo no existe, el proyecto
 * compila igual y `assembleRelease` genera un APK sin firmar.
 */
val propsFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val hayFirma = propsFirma.getProperty("storeFile") != null

android {
    namespace = "pe.pagoya.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "pe.pagoya.app"
        // Android 8.0+: cubre la casi totalidad de teléfonos de comercios en Perú
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "0.2.0"
    }

    signingConfigs {
        if (hayFirma) {
            create("release") {
                storeFile = rootProject.file(propsFirma.getProperty("storeFile"))
                storePassword = propsFirma.getProperty("storePassword")
                keyAlias = propsFirma.getProperty("keyAlias")
                keyPassword = propsFirma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Sin keystore.properties se firma con la clave de depuración, para
            // poder instalar el release en un teléfono de prueba sin trámite.
            // No es un riesgo para producción: Play rechaza cualquier subida
            // firmada con la clave de debug.
            signingConfig = if (hayFirma) signingConfigs.getByName("release")
            else signingConfigs.getByName("debug")
            // R8 activado: poda la librería de íconos (entraba completa) y baja
            // bastante el bundle. Es seguro aquí porque el código NO deserializa
            // por reflexión (Firestore se lee por acceso manual a mapas) y las
            // reglas están en proguard-rules.pro. Shrink de RECURSOS lo dejamos
            // apagado por ahora (menos riesgo con widget/notificación); se puede
            // activar luego probándolo.
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    // Vigilante que revive servicio y listener aunque el sistema mate el proceso
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-core")
    // Íconos de trazo fino (look SF Symbols / iOS): Tabler vía compose-icons.
    // Se eligió Tabler porque es el único set outline del grupo con cobertura
    // completa de nuestros glifos (volumen, megáfono, escudo, recibo, tienda…);
    // Lucide y Phosphor no existen publicados en br.com.devsrsouza.compose.icons.
    // OJO: con R8/minify apagado (ver buildTypes.release) la librería entra
    // completa al APK y pesa varios MB.
    // TODO: habilitar isMinifyEnabled (probando a fondo la reflexión de
    // Firebase/parser) antes del release final para que solo queden los
    // íconos usados.
    implementation("br.com.devsrsouza.compose.icons:tabler-icons:1.1.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-config")
    // Push de campañas del operador (ofertas, recordatorio de cierre): la app se
    // suscribe a topics y recibe los mensajes que se envían desde la consola.
    implementation("com.google.firebase:firebase-messaging")
    // Cloud Functions callable (ej. canjearReferido "Casero trae Casero").
    implementation("com.google.firebase:firebase-functions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Anti-fake de raíz: App Check certifica que quien escribe en Firestore es
    // el APK real de PagoYa. En debug se usa el proveedor de depuración
    // (ver src/debug y src/release de nube/AppCheckPagoYa.kt).
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    debugImplementation("com.google.firebase:firebase-appcheck-debug")

    // Login con Google (Credential Manager)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}
