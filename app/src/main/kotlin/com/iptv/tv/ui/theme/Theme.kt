package com.iptv.tv.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta focada em TV — alto contraste, cores sóbrias
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),          // azul suave
    onPrimary = Color(0xFF003049),
    primaryContainer = Color(0xFF003F5C),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF1B3A1E),
    background = Color(0xFF0A0A0F),       // quase preto
    onBackground = Color(0xFFE8E8EC),
    surface = Color(0xFF12121A),
    onSurface = Color(0xFFE8E8EC),
    surfaceVariant = Color(0xFF1E1E2A),
    onSurfaceVariant = Color(0xFFB8B8C8),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF5C0A0A)
)

private val IPTVTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.5.sp)
)

@Composable
fun IPTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = IPTVTypography
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(DarkColorScheme.background)
        ) {
            content()
        }
    }
}
