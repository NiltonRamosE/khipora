package com.nramosds.khipora.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val KhiporaGreen = Color(0xFF236B55)
val KhiporaInk = Color(0xFF22322D)
val KhiporaBackground = Color(0xFFF7F9F8)
val KhiporaSoft = Color(0xFFDFF3E8)

private val lightPalette = lightColorScheme(
    primary = KhiporaGreen,
    onPrimary = Color.White,
    primaryContainer = KhiporaSoft,
    onPrimaryContainer = KhiporaInk,
    secondary = Color(0xFF57756B),
    background = KhiporaBackground,
    onBackground = KhiporaInk,
    surface = Color.White,
    onSurface = KhiporaInk,
    surfaceVariant = Color(0xFFEAF0EC),
    onSurfaceVariant = Color(0xFF506158),
    outline = Color(0xFFB5C7BB),
)

private val typography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    // La v1 usa un tema claro consistente en todas las pantallas.
    MaterialTheme(colorScheme = lightPalette, typography = typography, content = content)
}
