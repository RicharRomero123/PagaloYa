package pe.pagoya.app.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
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
                    "1. En la pantalla que se abre, busca Yape y activa \"Inicio automático\". Haz lo mismo con PagoYa.",
                    "2. Luego abre tus apps recientes (botón cuadrado), busca Yape y arrástralo hacia abajo para ponerle el candado 🔒. Haz lo mismo con PagoYa.",
                    "3. Por último: Ajustes → Apps → PagoYa → Otros permisos → activa \"Mostrar ventanas emergentes en segundo plano\" (así podemos revivir tu Yape).",
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

    // ── Blindaje total: cada escudo abre una pantalla REAL de ajustes ───────
    // "Full protección" = todos los candados que evitan que el sistema mate a
    // Yape o a PagoYa. Cada uno se levanta desde su pantalla del teléfono. Los
    // que podemos comprobar por código (batería y "aparecer encima" de PagoYa)
    // llevan check verde; los del fabricante y los de la app de Yape se ponen a
    // mano (Android no deja leer esos ajustes de otra app).

    const val ESCUDO_BATERIA = "bateria_pagoya"
    const val ESCUDO_OVERLAY = "overlay_pagoya"
    const val ESCUDO_FABRICANTE = "fabricante"
    const val ESCUDO_APP_YAPE = "app_yape"
    const val ESCUDO_APP_PAGOYA = "app_pagoya"

    data class Escudo(
        val id: String,
        val titulo: String,
        val descripcion: String,
        val textoBoton: String,
        /** Pasos exactos dentro de la pantalla que se abre (los del fabricante). */
        val pasos: List<String> = emptyList(),
        /** true si podemos confirmar por código que quedó puesto. */
        val verificable: Boolean = false,
    )

    /** La lista de escudos para ESTE teléfono, en el orden en que conviene ponerlos. */
    fun escudos(context: Context): List<Escudo> {
        val guia = guiaParaEsteTelefono()
        val yapeInstalado =
            Guardian.estadoDe(context, Guardian.PAQUETE_YAPE) != Guardian.EstadoBilletera.NO_INSTALADA
        return buildList {
            add(
                Escudo(
                    ESCUDO_BATERIA,
                    "Batería sin restricción para PagoYa",
                    "Que el teléfono nunca duerma a PagoYa para ahorrar batería.",
                    "Quitar la restricción",
                    verificable = true,
                )
            )
            add(
                Escudo(
                    ESCUDO_OVERLAY,
                    "Dejar que PagoYa aparezca encima",
                    "Es lo que nos deja revivir tu Yape solos cuando el teléfono lo apaga.",
                    "Permitir",
                    verificable = true,
                )
            )
            add(
                Escudo(
                    ESCUDO_FABRICANTE,
                    guia.titulo,
                    "El ajuste que tu ${guia.marca} esconde para no matar a Yape ni a PagoYa. " +
                        "El más importante en gama media.",
                    "Abrir ajustes de ${guia.marca}",
                    pasos = guia.pasos,
                )
            )
            if (yapeInstalado) add(
                Escudo(
                    ESCUDO_APP_YAPE,
                    "Blindar la app de Yape",
                    "Dentro: Batería → \"Sin restricciones\", y activa el inicio automático de Yape.",
                    "Abrir ajustes de Yape",
                )
            )
            add(
                Escudo(
                    ESCUDO_APP_PAGOYA,
                    "Blindar la app de PagoYa",
                    "Dentro: Batería → \"Sin restricciones\" para PagoYa.",
                    "Abrir ajustes de PagoYa",
                )
            )
        }
    }

    /** ¿Este escudo ya quedó puesto? Solo los verificables dan una respuesta real. */
    fun estaPuesto(context: Context, id: String): Boolean = when (id) {
        ESCUDO_BATERIA -> (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
        ESCUDO_OVERLAY -> Settings.canDrawOverlays(context)
        else -> false
    }

    /** Abre la pantalla exacta del teléfono para levantar este escudo. */
    fun abrirEscudo(context: Context, id: String): Boolean = when (id) {
        ESCUDO_BATERIA -> lanzar(
            context,
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}"),
            ),
        )
        ESCUDO_OVERLAY -> lanzar(
            context,
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}"),
            ),
        )
        // Reutiliza los intents del fabricante (con su fallback a los ajustes de Yape).
        ESCUDO_FABRICANTE -> abrirAjustes(context)
        ESCUDO_APP_YAPE -> lanzar(context, detallesApp(Guardian.PAQUETE_YAPE))
        ESCUDO_APP_PAGOYA -> lanzar(context, detallesApp(context.packageName))
        else -> false
    }

    /** ¿Están puestos TODOS los escudos que podemos comprobar? (batería + overlay) */
    fun blindajeVerificableCompleto(context: Context): Boolean =
        escudos(context).filter { it.verificable }.all { estaPuesto(context, it.id) }

    private fun detallesApp(paquete: String) = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:$paquete"),
    )

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
