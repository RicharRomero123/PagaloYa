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

/** Esquinas generosas: se ve amable, no bancario. */
private val FormasPagoYa = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
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
