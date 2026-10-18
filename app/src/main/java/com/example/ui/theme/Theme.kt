package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimaryDark,
    onPrimary = OnGoldPrimaryDark,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = OnGoldContainerDark,
    secondary = CoralSecondaryDark,
    onSecondary = OnCoralSecondaryDark,
    secondaryContainer = CoralContainerDark,
    onSecondaryContainer = OnCoralContainerDark,
    tertiary = LavenderTertiaryDark,
    onTertiary = OnLavenderTertiaryDark,
    tertiaryContainer = LavenderContainerDark,
    onTertiaryContainer = OnLavenderContainerDark,
    background = PlumBackgroundDark,
    onBackground = OnPlumBackgroundDark,
    surface = PlumSurfaceDark,
    onSurface = OnPlumSurfaceDark,
    surfaceVariant = PlumSurfaceVariantDark,
    onSurfaceVariant = OnPlumSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimaryLight,
    onPrimary = OnAmberPrimaryLight,
    primaryContainer = AmberContainerLight,
    onPrimaryContainer = OnAmberContainerLight,
    secondary = CoralSecondaryLight,
    onSecondary = OnCoralSecondaryLight,
    secondaryContainer = CoralContainerLight,
    onSecondaryContainer = OnCoralContainerLight,
    tertiary = PlumTertiaryLight,
    onTertiary = OnPlumTertiaryLight,
    tertiaryContainer = PlumContainerLight,
    onTertiaryContainer = OnPlumContainerLight,
    background = WarmCreamBackgroundLight,
    onBackground = OnWarmCreamBackgroundLight,
    surface = WarmCreamSurfaceLight,
    onSurface = OnWarmCreamSurfaceLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onSurfaceVariant = OnWarmSurfaceVariantLight
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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
        shapes = AppShapes,
        content = content
    )
}
