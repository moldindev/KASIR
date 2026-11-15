package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val BoutiqueLightColorScheme =
  lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnPrimaryContainer,
    secondary = CopperSecondary,
    onSecondary = CopperOnSecondary,
    secondaryContainer = CopperContainer,
    onSecondaryContainer = CopperOnContainer,
    tertiary = AmberTertiary,
    tertiaryContainer = AmberContainer,
    background = BoutiqueAlabasterBg,
    onBackground = CharcoalInkPrimary,
    surface = BoutiquePureSurface,
    onSurface = CharcoalInkPrimary,
    surfaceVariant = BoutiqueStoneSurfaceVariant,
    onSurfaceVariant = CharcoalInkSecondary,
    surfaceContainerLow = BoutiqueAlabasterBg,
    surfaceContainer = BoutiqueWarmContainer,
    surfaceContainerHigh = BoutiqueStoneSurfaceVariant,
    outline = BoutiqueHairlineOutline,
    outlineVariant = BoutiqueHairlineOutline,
    error = BrickError,
    onError = BrickOnError,
    errorContainer = BrickErrorContainer,
    onErrorContainer = BrickOnErrorContainer,
  )

private val BoutiqueDarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = SagePrimary,
    onPrimaryContainer = SagePrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = CopperOnContainer,
    secondaryContainer = CopperOnContainer,
    onSecondaryContainer = CopperContainer,
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
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
  )

@Composable
fun KasirSembakoTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) BoutiqueDarkColorScheme else BoutiqueLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
