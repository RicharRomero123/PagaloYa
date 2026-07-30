package pe.pagoya.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.LaunchedEffect
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.BilleteraParser
import pe.pagoya.app.core.RegistroPagos
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.Sesion
import pe.pagoya.app.servicio.ServicioPrimerPlano
import pe.pagoya.app.ui.PantallaComercio
import pe.pagoya.app.ui.PantallaLogin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Naranja = Color(0xFFFF6B1A)
private val AzulNoche = Color(0xFF1A2B4A)
private val Crema = Color(0xFFFFF6EF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppPagoYa(this) }
    }

    override fun onResume() {
        super.onResume()
        if (Sesion.conectado()) ServicioPrimerPlano.arrancar(this)
    }
}

private enum class Etapa { CARGANDO, LOGIN, COMERCIO, PRINCIPAL }

@Composable
fun AppPagoYa(activity: MainActivity) {
    var etapa by remember { mutableStateOf(Etapa.CARGANDO) }

    LaunchedEffect(Unit) {
        etapa = when {
            !Sesion.conectado() -> Etapa.LOGIN
            ComercioRepo.cargar() == null -> Etapa.COMERCIO
            else -> Etapa.PRINCIPAL
        }
    }

    when (etapa) {
        Etapa.CARGANDO -> {}
        Etapa.LOGIN -> PantallaLogin(activity) {
            etapa = Etapa.COMERCIO
        }
        Etapa.COMERCIO -> {
            // Si al entrar ya tenía comercio, saltar directo
            LaunchedEffect(Unit) {
                if (ComercioRepo.cargar() != null) etapa = Etapa.PRINCIPAL
            }
            PantallaComercio {
                ServicioPrimerPlano.arrancar(activity)
                etapa = Etapa.PRINCIPAL
            }
        }
        Etapa.PRINCIPAL -> PantallaPrincipal {
            Sesion.salir()
            etapa = Etapa.LOGIN
        }
    }
}

@Composable
fun PantallaPrincipal(alSalir: () -> Unit) {
    val contexto = LocalContext.current
    val pagos by RegistroPagos.pagos.collectAsState()

    var accesoNotificaciones by remember { mutableStateOf(tieneAccesoNotificaciones(contexto)) }
    var bateriaSinRestriccion by remember { mutableStateOf(bateriaLibre(contexto)) }
    var puedeNotificar by remember { mutableStateOf(permisoPostNotificaciones(contexto)) }
    var vozFuerte by remember { mutableStateOf(Anunciador.vozFuerte(contexto)) }

    // Reevaluar permisos cada vez que el usuario vuelve de Ajustes
    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                accesoNotificaciones = tieneAccesoNotificaciones(contexto)
                bateriaSinRestriccion = bateriaLibre(contexto)
                puedeNotificar = permisoPostNotificaciones(contexto)
            }
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    val pedirPermisoNotificar = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { puedeNotificar = it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema)
            .padding(16.dp)
    ) {
        val comercio by ComercioRepo.comercio.collectAsState()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PagoYa", color = Naranja, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text(
                "Salir",
                color = AzulNoche.copy(alpha = 0.5f),
                modifier = Modifier
                    .clickable { alSalir() }
                    .padding(4.dp),
                fontSize = 13.sp,
            )
        }
        comercio?.let { c ->
            Text(c.nombre, color = AzulNoche, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            if (c.rol == "dueno" && c.codigo.isNotBlank()) {
                Text(
                    "Código para tu gente: ${c.codigo}",
                    color = Naranja, fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
            } else if (c.rol == "trabajador") {
                Text("Modo escucha: anunciamos lo que capture el teléfono del dueño",
                    color = AzulNoche.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        } ?: Text("Tu caja habla. Tus pagos suenan.", color = AzulNoche, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))

        val todoListo = accesoNotificaciones && bateriaSinRestriccion &&
            (Build.VERSION.SDK_INT < 33 || puedeNotificar)

        if (!todoListo) {
            Text(
                "Primero activa estos permisos para que tu caja empiece a hablar:",
                color = AzulNoche, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            if (!accesoNotificaciones) {
                TarjetaPermiso(
                    titulo = "Escuchar tus yapeos",
                    detalle = "Dale acceso a las notificaciones. Solo leemos las de tus billeteras, nada más.",
                ) {
                    contexto.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
            if (Build.VERSION.SDK_INT >= 33 && !puedeNotificar) {
                TarjetaPermiso(
                    titulo = "Mostrar que estamos activos",
                    detalle = "Permite las notificaciones de PagoYa para verlo siempre encendido.",
                ) { pedirPermisoNotificar.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
            if (!bateriaSinRestriccion) {
                TarjetaPermiso(
                    titulo = "Que la batería no nos apague",
                    detalle = "Sin esto, el teléfono puede dormir a PagoYa y una venta no sonaría.",
                ) {
                    contexto.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${contexto.packageName}")
                        )
                    )
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "✅ ¡Listo, casero! PagoYa está escuchando. Si no suena, no te pagaron.",
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = vozFuerte, onCheckedChange = {
                vozFuerte = it
                Anunciador.definirVozFuerte(contexto, it)
            })
            Spacer(Modifier.width(8.dp))
            Text("Voz fuerte (volumen al máximo al anunciar)", color = AzulNoche)
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    Anunciador.anunciar(contexto, "¡Pago Ya! Así sonará cada venta, casero.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Naranja)
            ) { Text("🔊 Probar voz") }

            OutlinedButton(onClick = {
                // Prueba del flujo completo: mismo camino que una notificación real
                val pago = BilleteraParser.parsear(
                    "com.bcp.innovacxion.yapeapp",
                    "Yape · María Prueba te yapeó S/ 25.50",
                    System.currentTimeMillis()
                )
                if (pago != null) {
                    RegistroPagos.agregar(contexto, pago)
                    Anunciador.anunciar(contexto, BilleteraParser.fraseDeVoz(pago))
                }
            }) { Text("Simular yapeo") }
        }

        Spacer(Modifier.height(18.dp))
        val totalHoy = pagos.filter { esDeHoy(it.timestamp) }.sumOf { it.monto }
        Card(
            colors = CardDefaults.cardColors(containerColor = AzulNoche),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Hoy cayeron", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                Text(
                    "S/ ${"%,.2f".format(totalHoy)}",
                    color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(pagos) { pago ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(pago.pagador, fontWeight = FontWeight.Bold, color = AzulNoche)
                            Text(
                                "${pago.billeteraNombre} · ${hora(pago.timestamp)}",
                                fontSize = 12.sp,
                                color = AzulNoche.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            "S/ ${"%,.2f".format(pago.monto)}",
                            fontWeight = FontWeight.Black, color = Naranja, fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPermiso(titulo: String, detalle: String, alPulsar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, color = AzulNoche)
            Text(detalle, fontSize = 13.sp, color = AzulNoche.copy(alpha = 0.75f))
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = alPulsar,
                colors = ButtonDefaults.buttonColors(containerColor = Naranja)
            ) { Text("Activar") }
        }
    }
}

private fun tieneAccesoNotificaciones(contexto: android.content.Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(contexto)
        .contains(contexto.packageName)

private fun bateriaLibre(contexto: android.content.Context): Boolean =
    (contexto.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager)
        .isIgnoringBatteryOptimizations(contexto.packageName)

private fun permisoPostNotificaciones(contexto: android.content.Context): Boolean =
    Build.VERSION.SDK_INT < 33 ||
        contexto.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

private fun esDeHoy(timestamp: Long): Boolean {
    val hoy = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    return timestamp >= hoy
}

private fun hora(timestamp: Long): String =
    SimpleDateFormat("hh:mm a", Locale("es", "PE")).format(Date(timestamp))
