package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TgBlueLight,
    onPrimary = Color.White,
    primaryContainer = TgBlueDark,
    onPrimaryContainer = Color.White,
    secondary = CoinGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF452200),
    onSecondaryContainer = CoinGoldLight,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = TgBgDark,
    onBackground = Color(0xFFE5E7EB),
    surface = TgSurfaceDark,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = TgSurfaceElevated,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = TgBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = TgBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBFA),
    onPrimaryContainer = TgBlueDark,
    secondary = CoinGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = TgBgLight,
    onBackground = Color(0xFF111827),
    surface = TgSurfaceLight,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = TgSurfaceLightElevated,
    onSurfaceVariant = Color(0xFF4B5563),
    outline = TgBorderLight
)

@Composable
fun INREarnBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
