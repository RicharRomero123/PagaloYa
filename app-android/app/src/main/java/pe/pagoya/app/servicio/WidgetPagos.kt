package pe.pagoya.app.servicio

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import pe.pagoya.app.MainActivity
import pe.pagoya.app.R
import pe.pagoya.app.core.Pago
import pe.pagoya.app.core.RegistroPagos
import java.util.Locale

/**
 * Widget de inicio "la bolsa de PagoYa": el comerciante ve su caja sin abrir la
 * app — total de hoy y los últimos 3 pagos.
 *
 * Corre en OTRO contexto/proceso (el launcher), así que NO usa el StateFlow en
 * memoria de RegistroPagos: lee y parsea el JSON directo del SharedPreferences
 * (RegistroPagos.leerDesdePrefs). Cuando cae un pago, RegistroPagos.persistir
 * llama a WidgetPagos.refrescar() para pintar el pago al toque.
 */
class WidgetPagos : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        gestor: AppWidgetManager,
        ids: IntArray,
    ) {
        ids.forEach { id -> pintar(context, gestor, id) }
    }

    companion object {

        private const val CANT_FILAS = 3
        private val FILAS = intArrayOf(R.id.fila_1, R.id.fila_2, R.id.fila_3)
        private val FILAS_BILLETERA =
            intArrayOf(R.id.fila_1_billetera, R.id.fila_2_billetera, R.id.fila_3_billetera)
        private val FILAS_MONTO =
            intArrayOf(R.id.fila_1_monto, R.id.fila_2_monto, R.id.fila_3_monto)

        /**
         * Refresca todas las instancias del widget. Lo llama RegistroPagos al
         * persistir un pago nuevo (y sirve de helper estático para no acoplar el
         * registro al AppWidgetManager). Seguro si no hay ningún widget puesto.
         */
        fun refrescar(context: Context) {
            runCatching {
                val gestor = AppWidgetManager.getInstance(context)
                val ids = gestor.getAppWidgetIds(
                    ComponentName(context, WidgetPagos::class.java)
                )
                ids.forEach { id -> pintar(context, gestor, id) }
            }
        }

        private fun pintar(context: Context, gestor: AppWidgetManager, id: Int) {
            val vistas = RemoteViews(context.packageName, R.layout.widget_pagos)
            val pagos = RegistroPagos.leerDesdePrefs(context)

            val total = RegistroPagos.totalDeHoy(pagos)
            vistas.setTextViewText(
                R.id.widget_total,
                "Hoy S/ ${formatearMonto(total)}",
            )

            val recientes = pagos.take(CANT_FILAS)
            if (recientes.isEmpty()) {
                vistas.setViewVisibility(R.id.widget_vacio, View.VISIBLE)
                FILAS.forEach { vistas.setViewVisibility(it, View.GONE) }
            } else {
                vistas.setViewVisibility(R.id.widget_vacio, View.GONE)
                for (i in 0 until CANT_FILAS) {
                    val pago = recientes.getOrNull(i)
                    if (pago == null) {
                        vistas.setViewVisibility(FILAS[i], View.GONE)
                    } else {
                        vistas.setViewVisibility(FILAS[i], View.VISIBLE)
                        vistas.setTextViewText(FILAS_BILLETERA[i], etiquetaPago(pago))
                        vistas.setTextViewText(
                            FILAS_MONTO[i],
                            "S/ ${formatearMonto(pago.monto)}",
                        )
                    }
                }
            }

            // Tocar el widget abre la app.
            val abrir = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            vistas.setOnClickPendingIntent(R.id.widget_raiz, abrir)

            gestor.updateAppWidget(id, vistas)
        }

        /**
         * Etiqueta de la fila: la billetera y, discreto, el nombre del pagador si
         * lo hay (dato personal: se mantiene breve y sin exponer de más).
         */
        private fun etiquetaPago(pago: Pago): String {
            val pagador = pago.pagador.trim()
            return if (pagador.isEmpty() || pagador == "Alguien") {
                pago.billeteraNombre
            } else {
                "${pago.billeteraNombre} · $pagador"
            }
        }

        private fun formatearMonto(monto: Double): String =
            String.format(Locale.US, "%.2f", monto)
    }
}
