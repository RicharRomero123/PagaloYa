package pe.pagoya.app.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Onboarding por marca: cada fabricante chino esconde en un lugar distinto el
 * ajuste que impide que el sistema mate a Yape (inicio automático / batería).
 * Detectamos la marca, damos los pasos exactos y abrimos la pantalla correcta.
 *
 * Basado en el conocimiento público de dontkillmyapp.com — los componentes
 * pueden variar por versión de capa; siempre hay fallback a los ajustes de Yape.
 */
object ProteccionMarca {

    data class Guia(
        val marca: String,
        val titulo: String,
        val pasos: List<String>,
        val intentos: List<Intent>,
    )

    fun guiaParaEsteTelefono(): Guia {
        val fabricante = Build.MANUFACTURER.lowercase()
        return when {
            fabricante.contains("xiaomi") || fabricante.contains("redmi") ||
                fabricante.contains("poco") -> Guia(
                marca = "Xiaomi/Redmi/POCO",
                titulo = "Blinda tu Yape en tu Xiaomi",
                pasos = listOf(
                    "1. En la pantalla que se abre, busca Yape y activa \"Inicio automático\".",
                    "2. Luego abre tus apps recientes (botón cuadrado), busca Yape y arrástralo hacia abajo para ponerle el candado 🔒.",
                    "3. Haz lo mismo con PagoYa.",
                ),
                intentos = listOf(
                    intento("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"),
                ),
            )
            fabricante.contains("huawei") || fabricante.contains("honor") -> Guia(
                marca = "Huawei/Honor",
                titulo = "Blinda tu Yape en tu Huawei",
                pasos = listOf(
                    "1. En la pantalla que se abre, busca Yape.",
                    "2. Desactiva \"Gestión automática\" y activa las 3 opciones manuales (inicio automático, inicio secundario, ejecutar en segundo plano).",
                    "3. Haz lo mismo con PagoYa.",
                ),
                intentos = listOf(
                    intento("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                    intento("com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"),
                ),
            )
            fabricante.contains("oppo") || fabricante.contains("realme") ||
                fabricante.contains("oneplus") -> Guia(
                marca = "Oppo/Realme/OnePlus",
                titulo = "Blinda tu Yape en tu equipo",
                pasos = listOf(
                    "1. En la pantalla que se abre, busca Yape y permite \"Inicio automático\".",
                    "2. En Batería, elige \"No optimizar\" para Yape y PagoYa.",
                ),
                intentos = listOf(
                    intento("com.coloros.safecenter",
                        "com.coloros.safecenter.startupapp.StartupAppListActivity"),
                    intento("com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                    intento("com.oneplus.security",
                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
                ),
            )
            fabricante.contains("vivo") -> Guia(
                marca = "Vivo",
                titulo = "Blinda tu Yape en tu Vivo",
                pasos = listOf(
                    "1. En la pantalla que se abre, busca Yape y permite el arranque en segundo plano.",
                    "2. Haz lo mismo con PagoYa.",
                ),
                intentos = listOf(
                    intento("com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                    intento("com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
                ),
            )
            fabricante.contains("samsung") -> Guia(
                marca = "Samsung",
                titulo = "Blinda tu Yape en tu Samsung",
                pasos = listOf(
                    "1. En la pantalla que se abre, entra a \"Batería\".",
                    "2. En \"Límites de uso en segundo plano\", saca a Yape de las apps en suspensión y agrégalo a \"Apps que nunca se suspenden\".",
                    "3. Haz lo mismo con PagoYa.",
                ),
                intentos = listOf(
                    intento("com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"),
                    intento("com.samsung.android.lool",
                        "com.samsung.android.sm_cn.ui.battery.BatteryActivity"),
                ),
            )
            else -> Guia(
                marca = Build.MANUFACTURER,
                titulo = "Blinda tu Yape",
                pasos = listOf(
                    "1. En la pantalla que se abre, entra a Batería.",
                    "2. Elige \"Sin restricciones\" para que el sistema nunca duerma a Yape.",
                    "3. Haz lo mismo con PagoYa (Ajustes → Apps → PagoYa → Batería).",
                ),
                intentos = emptyList(),
            )
        }
    }

    /**
     * Abre la pantalla de la marca; si ninguna existe (varía por versión),
     * cae a los ajustes de la app de Yape (Batería está a un toque ahí).
     */
    fun abrirAjustes(context: Context): Boolean {
        for (intent in guiaParaEsteTelefono().intentos) {
            if (lanzar(context, intent)) return true
        }
        return lanzar(
            context,
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${Guardian.PAQUETE_YAPE}")
            )
        )
    }

    private fun intento(paquete: String, clase: String) = Intent().apply {
        component = ComponentName(paquete, clase)
    }

    private fun lanzar(context: Context, intent: Intent): Boolean = runCatching {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    }.getOrDefault(false)

    // ¿Ya completó este onboarding?
    private const val PREFS = "pagoya_config"
    private const val CLAVE = "proteccion_marca_hecha"

    fun yaProtegido(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(CLAVE, false)

    fun marcarProtegido(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(CLAVE, true).apply()
    }
}
