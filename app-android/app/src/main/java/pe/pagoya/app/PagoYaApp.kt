package pe.pagoya.app

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.BilleteraParser
import pe.pagoya.app.core.Enlaces
import pe.pagoya.app.core.PreferenciasApariencia
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.nube.AppCheckPagoYa
import pe.pagoya.app.servicio.VigilanteWorker

class PagoYaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 0. App Check antes de cualquier uso de Firestore: certifica que quien
        //    escribe pagos es el APK real de PagoYa y no un clon modificado.
        runCatching { AppCheckPagoYa.instalar() }
        // 1. Patrones locales (assets) — siempre disponibles aunque no haya internet
        BilleteraParser.cargar(this)
        RegistroPagos.cargar(this)
        PreferenciasApariencia.cargar(this)
        Anunciador.inicializar(this)
        // 2. Patrones desde la nube — si Yape cambia su texto, se corrige desde la
        //    consola (Remote Config, parámetro "billeteras_json") sin republicar APK
        actualizarPatronesDesdeNube()
        // 3. Enlaces configurables (WhatsApp de ventas, redes) desde Firestore:
        //    el operador los edita en su panel sin republicar la app.
        Enlaces.cargarDesdeNube()
        // 4. Vigilante externo: revive servicio y listener aunque maten el proceso
        VigilanteWorker.programar(this)
    }

    private fun actualizarPatronesDesdeNube() {
        runCatching {
            val config = FirebaseRemoteConfig.getInstance()
            config.setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600) // refresco máx. cada hora
                    .build()
            )
            config.fetchAndActivate().addOnCompleteListener {
                val json = config.getString(CLAVE_BILLETERAS)
                if (json.isNotBlank()) {
                    runCatching { BilleteraParser.cargarDesdeJson(json) }
                }
            }
        }
    }

    companion object {
        const val CLAVE_BILLETERAS = "billeteras_json"
    }
}
