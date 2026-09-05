package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ChambaBlueLight,
    onPrimary = Color.White,
    primaryContainer = ChambaNavyPrimary,
    onPrimaryContainer = Color.White,
    secondary = ChambaAmber,
    onSecondary = Color.Black,
    secondaryContainer = ChambaAmberDark,
    onSecondaryContainer = Color.White,
    tertiary = DominicanRedLight,
    onTertiary = DominicanRedDark,
    background = ChambaBgDark,
    onBackground = ChambaTextPrimaryDark,
    surface = ChambaSurfaceDark,
    onSurface = ChambaTextPrimaryDark,
    surfaceVariant = ChambaSurfaceVariantDark,
    onSurfaceVariant = ChambaTextSecondaryDark,
    outline = ChambaOutlineDark,
    error = DominicanRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ChambaNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = ChambaNavyDark,
    secondary = ChambaBlueAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = DominicanRed,
    onTertiary = Color.White,
    tertiaryContainer = DominicanRedLight,
    onTertiaryContainer = DominicanRedDark,
    background = ChambaBgLight,
    onBackground = ChambaTextPrimary,
    surface = ChambaSurfaceLight,
    onSurface = ChambaTextPrimary,
    surfaceVariant = ChambaSurfaceVariantLight,
    onSurfaceVariant = ChambaTextSecondary,
    outline = ChambaOutlineLight,
    error = DominicanRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
