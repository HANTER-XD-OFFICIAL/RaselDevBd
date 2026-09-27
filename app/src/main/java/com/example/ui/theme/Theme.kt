package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = CyberEmerald,
    onPrimary = Color(0xFF022C22),
    primaryContainer = CyberEmeraldContainer,
    onPrimaryContainer = OnCyberEmeraldContainer,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF083344),
    secondaryContainer = ElectricCyanContainer,
    onSecondaryContainer = OnElectricCyanContainer,
    tertiary = BengalCoral,
    onTertiary = Color.White,
    tertiaryContainer = BengalCoralContainer,
    onTertiaryContainer = OnBengalCoralContainer,
    background = MidnightNavy,
    onBackground = DarkTextPrimary,
    surface = DeepSlateSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = ElevatedSlateCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = SubtleSlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyberEmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = Color(0xFFE11D48),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFF881337),
    background = CrispLightBg,
    onBackground = CrispDarkText,
    surface = CrispLightSurface,
    onSurface = CrispDarkText,
    surfaceVariant = CrispLightCard,
    onSurfaceVariant = CrispMutedText,
    outline = Color(0xFFCBD5E1)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RaselDevTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    RaselDevTheme(darkTheme = darkTheme, content = content)
}
