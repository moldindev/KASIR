package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WarmLightColorScheme =
  lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = TerracottaSecondary,
    onSecondary = TerracottaOnSecondary,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = TerracottaOnContainer,
    tertiary = AmberTertiary,
    tertiaryContainer = AmberContainer,
    background = WarmPaperBackground,
    onBackground = InkPrimary,
    surface = WarmCardSurface,
    onSurface = InkPrimary,
    surfaceVariant = WarmCreamSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLow = WarmPaperBackground,
    surfaceContainer = WarmSurfaceContainer,
    surfaceContainerHigh = WarmCreamSurfaceVariant,
    outline = WarmOutline,
    outlineVariant = WarmOutline,
    error = BrickError,
    onError = BrickOnError,
    errorContainer = BrickErrorContainer,
    onErrorContainer = BrickOnErrorContainer,
  )

private val WarmDarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = ForestPrimary,
    onPrimaryContainer = ForestPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = TerracottaOnContainer,
    secondaryContainer = TerracottaOnContainer,
    onSecondaryContainer = TerracottaContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    outline = DarkSurfaceVariant,
    error = BrickErrorContainer,
    onError = BrickOnErrorContainer,
  )

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
  )

@Composable
fun KasirSembakoTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) WarmDarkColorScheme else WarmLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
