package pe.pagoya.app.ui.mas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pagoya.app.core.Anunciador
import pe.pagoya.app.core.voz.CatalogoVoces
import pe.pagoya.app.core.voz.PreferenciasVoz
import pe.pagoya.app.ui.tema.Aviso
import pe.pagoya.app.ui.tema.AzulNoche
import pe.pagoya.app.ui.tema.Blanco
import pe.pagoya.app.ui.tema.Borde
import pe.pagoya.app.ui.tema.BotonPagoYa
import pe.pagoya.app.ui.tema.BotonSecundario
import pe.pagoya.app.ui.tema.Crema
import pe.pagoya.app.ui.tema.Etiqueta
import pe.pagoya.app.ui.tema.Humo
import pe.pagoya.app.ui.tema.NaranjaHondo
import pe.pagoya.app.ui.tema.NaranjaPagoYa
import pe.pagoya.app.ui.tema.NaranjaSuave
import pe.pagoya.app.ui.tema.TarjetaPagoYa
import pe.pagoya.app.ui.tema.TextoMedio
import pe.pagoya.app.ui.tema.TextoTenue
import pe.pagoya.app.ui.tema.TipoAviso

// Lo que se oye al probar: una venta de verdad, no un "hola mundo".
private const val PRUEBA_SIN_NOMBRE = "¡Pago Ya! Te yapearon 25 soles con 50"
private const val PRUEBA_CON_NOMBRE = "¡Pago Ya! María te yapeó 25 soles con 50"

/**
 * La voz de tu caja. El comerciante escucha cada voz del teléfono y se queda
 * con la que le gusta — tocar una tarjeta la elige y la reproduce al toque.
 */
@Composable
fun PantallaVoz(alVolver: () -> Unit) {
    val contexto = LocalContext.current
    BackHandler(onBack = alVolver)

    val motorListo by Anunciador.listo.collectAsState()
    var voces by remember { mutableStateOf<List<CatalogoVoces.VozDisponible>>(emptyList()) }
    var elegida by remember { mutableStateOf(PreferenciasVoz.vozElegida(contexto)) }
    var velocidad by remember { mutableStateOf(PreferenciasVoz.velocidad(contexto)) }
    var tono by remember { mutableStateOf(PreferenciasVoz.tono(contexto)) }
    var vozFuerte by remember { mutableStateOf(PreferenciasVoz.vozFuerte(contexto)) }
    var decirNombre by remember { mutableStateOf(PreferenciasVoz.decirNombre(contexto)) }

    LaunchedEffect(motorListo) {
        if (motorListo) voces = Anunciador.catalogo(contexto)
    }

    fun probar() = Anunciador.anunciar(
        contexto,
        if (decirNombre) PRUEBA_CON_NOMBRE else PRUEBA_SIN_NOMBRE,
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Crema),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = alVolver) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = AzulNoche,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "La voz de tu caja",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AzulNoche,
                )
            }
        }

        // El motor manda más que la voz: si es el viejo, todo sonará a robot
        if (motorListo && !Anunciador.usaMotorGoogle()) {
            item {
                Aviso(
                    tipo = TipoAviso.AVISO,
                    titulo = "Tu teléfono usa una voz antigua",
                    texto = "Con el motor de voz de Google las voces suenan de " +
                        "persona, no de robot. Es gratis y se instala una sola vez.",
                    textoBoton = "Instalar voces de Google",
                    alPulsar = { CatalogoVoces.instalarMotorGoogle(contexto) },
                )
            }
        }

        item { Etiqueta("Elige tu voz") }

        item {
            TarjetaVoz(
                titulo = "Automática",
                descripcion = "PagoYa elige la mejor voz de tu teléfono",
                seleccionada = elegida == null,
                alElegir = {
                    PreferenciasVoz.elegirVoz(contexto, null)
                    elegida = null
                    Anunciador.aplicarPreferencias(contexto)
                    probar()
                },
            )
        }

        if (!motorListo) {
            item {
                Text(
                    "Buscando las voces de tu teléfono…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }

        items(voces) { voz ->
            TarjetaVoz(
                titulo = voz.etiqueta,
                descripcion = voz.descripcion,
                seleccionada = elegida == voz.id,
                alElegir = {
                    PreferenciasVoz.elegirVoz(contexto, voz.id)
                    elegida = voz.id
                    Anunciador.aplicarPreferencias(contexto)
                    probar()
                },
            )
        }

        item {
            BotonSecundario("Buscar más voces", alPulsar = {
                if (!CatalogoVoces.abrirAjustesDeVoz(contexto)) {
                    CatalogoVoces.instalarMotorGoogle(contexto)
                }
            })
        }
        item {
            Text(
                "En Ajustes → Texto a voz puedes descargar más voces. " +
                    "Al volver, dale a “Actualizar lista”.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoTenue,
            )
        }
        item {
            BotonSecundario("Actualizar lista", alPulsar = {
                Anunciador.reiniciar(contexto)
            })
        }

        // ── Cómo habla ──
        item { Etiqueta("Cómo habla", Modifier.padding(top = 8.dp)) }
        item {
            TarjetaPagoYa {
                Deslizador(
                    titulo = "Velocidad",
                    ayuda = "Más lenta se entiende mejor con bulla.",
                    valor = velocidad,
                    rango = PreferenciasVoz.VELOCIDAD_MIN..PreferenciasVoz.VELOCIDAD_MAX,
                    alCambiar = {
                        velocidad = it
                        PreferenciasVoz.definirVelocidad(contexto, it)
                    },
                    alSoltar = {
                        Anunciador.aplicarPreferencias(contexto)
                        probar()
                    },
                )
                Spacer(Modifier.height(18.dp))
                Deslizador(
                    titulo = "Tono",
                    ayuda = "Un poco más agudo suena más alegre.",
                    valor = tono,
                    rango = PreferenciasVoz.TONO_MIN..PreferenciasVoz.TONO_MAX,
                    alCambiar = {
                        tono = it
                        PreferenciasVoz.definirTono(contexto, it)
                    },
                    alSoltar = {
                        Anunciador.aplicarPreferencias(contexto)
                        probar()
                    },
                )
            }
        }

        item {
            TarjetaPagoYa {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Voz fuerte",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulNoche,
                        )
                        Text(
                            "Sube el volumen al máximo mientras anuncia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoMedio,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = vozFuerte,
                        onCheckedChange = {
                            vozFuerte = it
                            PreferenciasVoz.definirVozFuerte(contexto, it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = NaranjaPagoYa,
                        ),
                    )
                }
            }
        }

        // ── Qué dice ──
        item { Etiqueta("Qué dice", Modifier.padding(top = 8.dp)) }
        item {
            TarjetaPagoYa {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Decir el nombre de quien paga",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulNoche,
                        )
                        Text(
                            if (decirNombre) "“María te yapeó 25 soles con 50”"
                            else "“Te yapearon 25 soles con 50”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NaranjaHondo,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = decirNombre,
                        onCheckedChange = {
                            decirNombre = it
                            PreferenciasVoz.definirDecirNombre(contexto, it)
                            Anunciador.anunciar(
                                contexto,
                                if (it) PRUEBA_CON_NOMBRE else PRUEBA_SIN_NOMBRE,
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = NaranjaPagoYa,
                        ),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Viene apagado a propósito: decir el nombre de un cliente a " +
                        "todo volumen lo expone delante de desconocidos. El nombre " +
                        "siempre lo ves en pantalla, suene o no.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoMedio,
                )
            }
        }

        item {
            BotonPagoYa("🔊 Probar cómo suena una venta", alPulsar = { probar() })
        }
        item {
            TarjetaPagoYa(color = Humo) {
                Text(
                    "💡 Estas voces salen de tu teléfono, así que funcionan aunque " +
                        "se caiga el internet. Prefiere siempre una que diga " +
                        "“sin internet”.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMedio,
                )
            }
        }
    }
}

@Composable
private fun TarjetaVoz(
    titulo: String,
    descripcion: String,
    seleccionada: Boolean,
    alElegir: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alElegir),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) NaranjaSuave else Blanco
        ),
        border = BorderStroke(
            if (seleccionada) 2.dp else 1.dp,
            if (seleccionada) NaranjaPagoYa else Borde,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (seleccionada) NaranjaPagoYa else NaranjaSuave),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (seleccionada) "🔊" else "▶", fontSize = 16.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulNoche,
                )
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (seleccionada) NaranjaHondo else TextoMedio,
                )
            }
        }
    }
}

@Composable
private fun Deslizador(
    titulo: String,
    ayuda: String,
    valor: Float,
    rango: ClosedFloatingPointRange<Float>,
    alCambiar: (Float) -> Unit,
    alSoltar: () -> Unit,
) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = AzulNoche)
            Text(
                "%.2f×".format(valor),
                style = MaterialTheme.typography.labelSmall,
                color = TextoMedio,
            )
        }
        Text(ayuda, style = MaterialTheme.typography.bodySmall, color = TextoMedio)
        Slider(
            value = valor,
            onValueChange = alCambiar,
            onValueChangeFinished = alSoltar,
            valueRange = rango,
            colors = SliderDefaults.colors(
                thumbColor = NaranjaPagoYa,
                activeTrackColor = NaranjaPagoYa,
                inactiveTrackColor = Borde,
            ),
        )
    }
}
