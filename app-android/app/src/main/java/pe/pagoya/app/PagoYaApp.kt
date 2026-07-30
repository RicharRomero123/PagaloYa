package pe.pagoya.app

import android.app.Application
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.BilleteraParser
import pe.pagoya.app.core.RegistroPagos

class PagoYaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        BilleteraParser.cargar(this)
        RegistroPagos.cargar(this)
        Anunciador.inicializar(this)
    }
}
