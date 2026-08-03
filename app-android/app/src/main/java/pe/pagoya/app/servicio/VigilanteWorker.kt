package pe.pagoya.app.servicio

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import pe.pagoya.app.core.Guardian
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.Sesion
import pe.pagoya.app.nube.TelemetriaRepo
import pe.pagoya.app.ui.onboarding.Permisos

/**
 * El vigilante de afuera. El servicio de primer plano puede morir (capas
 * agresivas de batería en gama media lo matan igual); este worker vive en
 * WorkManager, que el sistema respeta incluso tras matar el proceso, y cada
 * 15 minutos deja todo parado otra vez:
 *
 *  1. Revive el servicio de primer plano si lo mataron.
 *  2. Re-vincula el listener de notificaciones si Android lo soltó.
 *  3. Revive a Yape si está detenida (autoCurar); solo alerta si no pudo.
 *  4. Sube el latido de telemetría para el panel de operador.
 */
class VigilanteWorker(
    contexto: Context,
    parametros: WorkerParameters,
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val contexto = applicationContext
        if (!Sesion.conectado()) return Result.success()

        ServicioPrimerPlano.arrancar(contexto)
        if (Permisos.escuchaNotificaciones(contexto)) {
            EscuchaNotificaciones.reconectar(contexto)
        }
        // Si el proceso acaba de renacer, el comercio aún no está en memoria.
        // Se carga ANTES de autoCurar por si hace falta para el anuncio.
        runCatching { ComercioRepo.cargar() }
        Guardian.autoCurar(contexto)
        TelemetriaRepo.subirLatido(contexto)
        return Result.success()
    }

    companion object {
        private const val NOMBRE = "vigilante_pagoya"

        /**
         * Idempotente: si ya está programado, lo deja como está. 15 min es el
         * mínimo que WorkManager permite para trabajo periódico; la ventana
         * flex de 5 min deja que el sistema lo corra lo antes posible dentro de
         * cada ciclo. No pide red: revivir a Yape es un chequeo local.
         */
        fun programar(contexto: Context) {
            val pedido = PeriodicWorkRequestBuilder<VigilanteWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
                NOMBRE,
                ExistingPeriodicWorkPolicy.KEEP,
                pedido,
            )
        }
    }
}
