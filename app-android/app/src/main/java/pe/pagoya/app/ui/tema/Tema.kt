package pe.pagoya.app.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Tema PagoYa.
 *
 * A propósito solo hay modo claro: la app se usa en mostradores con sol
 * encima y en teléfonos de gama baja, donde el contraste alto sobre crema se
 * lee mejor que cualquier tema oscuro.
 */
private val EsquemaPagoYa = lightColorScheme(
    primary = NaranjaPagoYa,
    onPrimary = Blanco,
    primaryContainer = NaranjaSuave,
    onPrimaryContainer = NaranjaHondo,
    secondary = AzulNoche,
    onSecondary = Blanco,
    secondaryContainer = AzulNocheClaro,
    onSecondaryContainer = Blanco,
    background = Crema,
    onBackground = TextoFuerte,
    surface = Blanco,
    onSurface = TextoFuerte,
    surfaceVariant = Humo,
    onSurfaceVariant = TextoMedio,
    outline = Borde,
    outlineVariant = Borde,
    error = RojoAlerta,
    onError = Blanco,
    errorContainer = RojoSuave,
    onErrorContainer = RojoAlerta,
)

/**
 * Esquinas MUY generosas: se ve amable y moderno, no bancario. Se subieron los
 * radios (botones y tarjetas más redondeados) para el look "tech" que pide la
 * marca — botones tipo píldora, tarjetas con esquinas suaves.
 */
private val FormasPagoYa = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun TemaPagoYa(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaPagoYa,
        typography = TipografiaPagoYa,
        shapes = FormasPagoYa,
        content = contenido,
    )
}
