package com.iptv.tv.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    onPrimary = Color(0xFF003049),
    primaryContainer = Color(0xFF003F5C),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF1B3A1E),
    background = Color(0xFF0A0A0F),
    onBackground = Color(0xFFE8E8EC),
    surface = Color(0xFF12121A),
    onSurface = Color(0xFFE8E8EC),
    surfaceVariant = Color(0xFF1E1E2A),
    onSurfaceVariant = Color(0xFFB8B8C8),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF5C0A0A)
)

private fun createLightColorScheme(): ColorScheme {
    val lightPrimary = Color(0xFF0077B6)
    val lightOnPrimary = Color(0xFFFFFFFF)
    val lightPrimaryContainer = Color(0xFFCAE9FF)
    val lightOnPrimaryContainer = Color(0xFF001E2E)
    val lightSecondary = Color(0xFF2E7D32)
    val lightOnSecondary = Color(0xFFFFFFFF)
    val lightBackground = Color(0xFFF5F5F5)
    val lightOnBackground = Color(0xFF1A1A1A)
    val lightSurface = Color(0xFFFFFFFF)
    val lightOnSurface = Color(0xFF1A1A1A)
    val lightSurfaceVariant = Color(0xFFE0E0E0)
    val lightOnSurfaceVariant = Color(0xFF444444)
    val lightError = Color(0xFFB00020)
    val lightOnError = Color(0xFFFFFFFF)

    return ColorScheme(
        primary = lightPrimary,
        onPrimary = lightOnPrimary,
        primaryContainer = lightPrimaryContainer,
        onPrimaryContainer = lightOnPrimaryContainer,
        secondary = lightSecondary,
        onSecondary = lightOnSecondary,
        background = lightBackground,
        onBackground = lightOnBackground,
        surface = lightSurface,
        onSurface = lightOnSurface,
        surfaceVariant = lightSurfaceVariant,
        onSurfaceVariant = lightOnSurfaceVariant,
        error = lightError,
        onError = lightOnError,
        inversePrimary = Color(0xFF4FC3F7),
        secondaryContainer = Color(0xFFB8E6B8),
        onSecondaryContainer = Color(0xFF002200),
        tertiary = Color(0xFF006874),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF97F0FF),
        onTertiaryContainer = Color(0xFF001F24),
        surfaceTint = lightPrimary,
        inverseSurface = Color(0xFFE8E8EC),
        inverseOnSurface = Color(0xFF1A1A1A),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        border = Color(0xFFCAC4D0),
        borderVariant = Color(0xFFCAC4D0),
        scrim = Color(0xFF000000)
    )
}

enum class AppTheme { LIGHT, DARK }

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
fun IPTVTheme(theme: AppTheme = AppTheme.LIGHT, content: @Composable () -> Unit) {
    val colorScheme = if (theme == AppTheme.DARK) DarkColorScheme else createLightColorScheme()
    MaterialTheme(colorScheme = colorScheme, typography = IPTVTypography) {
        Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
            content()
        }
    }
}
