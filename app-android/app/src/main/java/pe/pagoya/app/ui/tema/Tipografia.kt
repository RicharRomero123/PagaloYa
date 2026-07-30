package pe.pagoya.app.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import pe.pagoya.app.R

/**
 * Tipografía PagoYa: redondeada y gruesa (BRAND.md), pensada para leerse de
 * lejos en un puesto de mercado y en pantallas baratas.
 *
 * Nunito se descarga por Google Fonts; si el teléfono no puede (sin Play
 * Services, sin datos), Compose cae solo a la fuente del sistema y la app se ve
 * bien igual. Para dejarla fija en el APK: poner Nunito en res/font/ y cambiar
 * FuentePagoYa por FontFamily(Font(R.font.nunito_bold, FontWeight.Bold), ...).
 */
private val proveedorGoogleFonts = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val nunito = GoogleFont("Nunito")

val FuentePagoYa = FontFamily(
    Font(googleFont = nunito, fontProvider = proveedorGoogleFonts, weight = FontWeight.Normal),
    Font(googleFont = nunito, fontProvider = proveedorGoogleFonts, weight = FontWeight.Medium),
    Font(googleFont = nunito, fontProvider = proveedorGoogleFonts, weight = FontWeight.SemiBold),
    Font(googleFont = nunito, fontProvider = proveedorGoogleFonts, weight = FontWeight.Bold),
    Font(googleFont = nunito, fontProvider = proveedorGoogleFonts, weight = FontWeight.Black),
)

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
