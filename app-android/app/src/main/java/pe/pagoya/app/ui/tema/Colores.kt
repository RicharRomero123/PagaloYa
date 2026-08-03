package pe.pagoya.app.ui.tema

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Paleta PagoYa (BRAND.md).
 *
 * Regla de marca: naranja criollo + azul noche. Nunca el morado de Yape ni el
 * turquesa de Plin — somos compatibles con ellos, no somos ellos.
 */

// Marca
val NaranjaPagoYa = Color(0xFFFF6B1A)   // primario: energía, calle
val NaranjaHondo = Color(0xFFD95100)    // presionado / texto sobre naranja suave
val NaranjaBrasa = Color(0xFFB53E00)    // el extremo oscuro del degradado
val NaranjaClaro = Color(0xFFFF9248)    // el extremo claro del degradado
val NaranjaSuave = Color(0xFFFFE9DA)    // fondos de realce
val AzulNoche = Color(0xFF1A2B4A)       // secundario: confianza, la plata
val AzulNocheClaro = Color(0xFF2C4570)

/**
 * Degradado naranja de marca (oscuro → claro), en diagonal. Es el "sello tech"
 * de la parte superior de la app (barra + cabecera). Se define una sola vez
 * aquí para que toda la app use exactamente el mismo.
 */
val DegradadoMarca: Brush
    get() = Brush.linearGradient(listOf(NaranjaBrasa, NaranjaPagoYa, NaranjaClaro))

/** Degradado vertical (arriba oscuro → abajo claro) para cabeceras altas. */
val DegradadoMarcaVertical: Brush
    get() = Brush.verticalGradient(listOf(NaranjaBrasa, NaranjaPagoYa))

// Fondos
val Crema = Color(0xFFFFF6EF)
val Blanco = Color(0xFFFFFFFF)
val Humo = Color(0xFFF3EBE3)
val Borde = Color(0xFFE6DACD)

// Texto
val TextoFuerte = AzulNoche
val TextoMedio = Color(0xFF5C6B85)
val TextoTenue = Color(0xFF93A0B5)

// Semánticos: en el mostrador se leen de un vistazo, sin leer la letra
val VerdeOk = Color(0xFF14803C)
val VerdeSuave = Color(0xFFE4F6EA)
val RojoAlerta = Color(0xFFC62828)
val RojoSuave = Color(0xFFFDEAEA)
val AmbarAviso = Color(0xFF9A6100)
val AmbarSuave = Color(0xFFFFF2DC)
