package pe.pagoya.app.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía PagoYa: pensada para leerse de lejos en un puesto de mercado y en
 * pantallas baratas. Lo que hace el trabajo pesado es la ESCALA (montos
 * enormes, nada por debajo de 12 sp) y los pesos gruesos.
 *
 * Hoy usa la fuente del sistema. BRAND.md pide una redondeada y gruesa (Nunito
 * o Baloo); para ponerla, la ruta segura es empaquetarla en el APK:
 *
 *   1. Descargar Nunito de fonts.google.com
 *   2. Copiar a app/src/main/res/font/ con nombres válidos:
 *        nunito_regular.ttf · nunito_bold.ttf · nunito_black.ttf
 *   3. Reemplazar la línea de abajo por:
 *
 *        val FuentePagoYa = FontFamily(
 *            Font(R.font.nunito_regular, FontWeight.Normal),
 *            Font(R.font.nunito_bold, FontWeight.Bold),
 *            Font(R.font.nunito_black, FontWeight.Black),
 *        )
 *
 * (Se descartó la descarga por Google Fonts: con android.nonTransitiveRClass=true
 * el array de certificados de la librería no resuelve desde el R de la app, y
 * además dependería de Play Services y de tener datos la primera vez.)
 */
val FuentePagoYa = FontFamily.Default

val TipografiaPagoYa = Typography(
    // El monto del día: lo más grande de la app, se lee desde la vereda
    displayLarge = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Black,
        fontSize = 44.sp, lineHeight = 48.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Black,
        fontSize = 34.sp, lineHeight = 40.sp,
    ),
    // Títulos de pantalla y de paso del asistente
    headlineLarge = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Black,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Black,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Bold,
        fontSize = 19.sp, lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp,
    ),
    // Botones: gruesos, nunca chiquitos
    labelLarge = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 20.sp,
    ),
    // Etiquetas de sección: "ÚLTIMOS PAGOS"
    labelMedium = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FuentePagoYa, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, lineHeight = 14.sp,
    ),
)
