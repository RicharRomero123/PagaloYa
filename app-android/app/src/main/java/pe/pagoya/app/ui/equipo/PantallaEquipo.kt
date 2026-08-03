package pe.pagoya.app.ui.equipo

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.Bulb
import compose.icons.tablericons.Gift
import compose.icons.tablericons.Headphones
import pe.pagoya.app.nube.ComercioRepo
import pe.pagoya.app.nube.ReferidosRepo
import pe.pagoya.app.ui.tema.AvatarInicial
import kotlinx.coroutines.launch
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaHondo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.RojoAlerta
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.VerdeOk
import pe.pagoya.app.ui.tema.VerdeSuave

/**
 * Equipo: quién escucha los pagos de este negocio.
 *
 * Para el dueño, el corazón es el código de 6 dígitos — se comparte por
 * WhatsApp, que es como se coordina de verdad en un negocio peruano.
 */
@Composable
fun PantallaEquipo() {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val comercio by ComercioRepo.comercio.collectAsState()
    var miembros by remember { mutableStateOf<List<ComercioRepo.Miembro>>(emptyList()) }

    LaunchedEffect(comercio?.id) {
        miembros = runCatching { ComercioRepo.miembros() }.getOrDefault(emptyList())
    }

    val esDueno = comercio?.rol == "dueno"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    "Tu equipo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                )
                Text(
                    "Los teléfonos que anuncian los pagos de ${comercio?.nombre ?: "tu negocio"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }

        if (esDueno && !comercio?.codigo.isNullOrBlank()) {
            item { TarjetaCodigo(comercio!!.nombre, comercio!!.codigo, contexto) }
        } else {
            item {
                TarjetaPagoYa(color = VerdeSuave) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            TablerIcons.Headphones,
                            contentDescription = null,
                            tint = VerdeOk,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Estás en modo escucha",
                            style = MaterialTheme.typography.titleMedium,
                            color = VerdeOk,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Este teléfono anuncia los pagos que caen al Yape del dueño. " +
                            "No necesita capturar nada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzulNoche,
                    )
                }
            }
        }

        // "Casero trae Casero": solo el dueño y solo si aún no canjeó un código.
        if (esDueno && comercio?.referidoCanjeado != true) {
            item {
                TarjetaReferido(
                    alCanjear = { codigo -> ReferidosRepo.canjearReferido(codigo) },
                    alExito = {
                        // La Function cambió la vigencia y marcó el canje: recargar
                        // el comercio para reflejarlo (y ocultar esta tarjeta).
                        alcance.launch { ComercioRepo.recargar() }
                    },
                )
            }
        }

        // Privacidad de caja: permiso criollo que solo el dueño maneja. Por
        // defecto su gente ve la caja; aquí puede reservarse los totales.
        if (esDueno) {
            item {
                TarjetaPrivacidadCaja(
                    verCaja = comercio?.trabajadorVeCaja ?: true,
                    alCambiar = { nuevo ->
                        alcance.launch { ComercioRepo.fijarVerCajaTrabajador(nuevo) }
                    },
                )
            }
        }

        if (miembros.isNotEmpty()) {
            item { Etiqueta("Quiénes escuchan", Modifier.padding(top = 8.dp)) }
            item {
                TarjetaPagoYa(relleno = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    miembros.forEachIndexed { i, m ->
                        if (i > 0) HorizontalDivider(color = Borde)
                        FilaMiembro(m)
                    }
                }
            }
        }

        item {
            TarjetaPagoYa(color = Humo) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        TablerIcons.Bulb,
                        contentDescription = null,
                        tint = NaranjaPagoYa,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Cómo funciona",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulNoche,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "El teléfono con el Yape del negocio es el único que captura. " +
                        "Los demás solo escuchan y anuncian, aunque estés en tu casa. " +
                        "Nadie más que ese teléfono puede registrar un pago.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }
    }
}

@Composable
private fun TarjetaCodigo(negocio: String, codigo: String, contexto: Context) {
    TarjetaPagoYa {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Etiqueta("Código de tu negocio")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                codigo.forEach { digito ->
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaranjaSuave),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            digito.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = NaranjaHondo,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Pásaselo a tu gente. Con este código su teléfono empieza a " +
                    "cantar tus pagos al toque.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMedio,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            BotonPagoYa("Compartir por WhatsApp", alPulsar = {
                compartirCodigo(contexto, negocio, codigo)
            })
        }
    }
}

/**
 * "Casero trae Casero": el dueño mete el código de 6 dígitos del comercio que
 * lo invitó y gana días de plan. El canje lo hace la Cloud Function; aquí solo
 * la llamamos y mostramos su mensaje (ya viene en criollo).
 *
 * @param alCanjear invoca la Function y devuelve el mensaje de éxito.
 * @param alExito se llama tras un canje bueno (para recargar el comercio).
 */
@Composable
private fun TarjetaReferido(
    alCanjear: suspend (String) -> Result<String>,
    alExito: () -> Unit,
) {
    val alcance = rememberCoroutineScope()
    var codigo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf<String?>(null) }

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NaranjaPagoYa,
        unfocusedBorderColor = Borde,
        focusedLabelColor = NaranjaPagoYa,
    )

    TarjetaPagoYa {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                TablerIcons.Gift,
                contentDescription = null,
                tint = NaranjaPagoYa,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "¿Te invitó un casero?",
                style = MaterialTheme.typography.titleMedium,
                color = AzulNoche,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Mete el código que te pasó y ganas días de regalo. Solo se canjea una vez.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoMedio,
        )

        if (exito != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VerdeSuave)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    exito!!,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerdeOk,
                )
            }
        } else {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it.filter(Char::isDigit).take(6); error = null },
                label = { Text("Código de tu casero") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = coloresCampo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = RojoAlerta,
                )
            }
            Spacer(Modifier.height(12.dp))
            BotonPagoYa(
                texto = "Canjear",
                cargando = cargando,
                alPulsar = {
                    if (codigo.length != 6) {
                        error = "El código tiene 6 dígitos, casero."
                        return@BotonPagoYa
                    }
                    cargando = true; error = null
                    alcance.launch {
                        alCanjear(codigo)
                            .onSuccess { mensaje ->
                                exito = mensaje
                                alExito()
                            }
                            .onFailure { error = it.message ?: "No se pudo canjear el código." }
                        cargando = false
                    }
                },
            )
        }
    }
}

/**
 * Toggle del dueño: ¿su gente ve la caja (total del día y métricas) o solo
 * escucha los pagos? Viene ENCENDIDO por defecto — la privacidad es opt-in.
 */
@Composable
private fun TarjetaPrivacidadCaja(verCaja: Boolean, alCambiar: (Boolean) -> Unit) {
    TarjetaPagoYa {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Mi gente ve la caja",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulNoche,
                )
                Text(
                    if (verCaja)
                        "Tus trabajadores ven el total del día y las cuentas."
                    else
                        "Tus trabajadores solo escuchan los pagos. Los totales los ves solo tú.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoMedio,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = verCaja,
                onCheckedChange = alCambiar,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Blanco,
                    checkedTrackColor = NaranjaPagoYa,
                ),
            )
        }
    }
}

@Composable
private fun FilaMiembro(miembro: ComercioRepo.Miembro) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarInicial(miembro.nombre)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                miembro.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = AzulNoche,
            )
            Text(
                if (miembro.rol == "dueno") "Dueño" else "Trabajador",
                style = MaterialTheme.typography.bodySmall,
                color = TextoMedio,
            )
        }
        val (fondo, tinte, etiqueta) = if (miembro.puedeCapturar) {
            Triple(NaranjaSuave, NaranjaHondo, "Captura")
        } else {
            Triple(VerdeSuave, VerdeOk, "Escucha")
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(fondo)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = tinte)
        }
    }
}

private fun compartirCodigo(contexto: Context, negocio: String, codigo: String) {
    val texto = "¡Hola! Instala PagoYa y entra con el código $codigo para escuchar " +
        "los pagos de $negocio en tu teléfono. 🔊"
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(
        Intent.createChooser(envio, "Compartir código")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
