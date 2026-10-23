package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.White,
  primaryContainer = EmeraldLight,
  onPrimaryContainer = EmeraldDeep,
  secondary = AmberAccent,
  onSecondary = Color.White,
  secondaryContainer = AmberLight,
  onSecondaryContainer = AmberDark,
  tertiary = EmeraldDark,
  background = BackgroundLight,
  onBackground = TextPrimary,
  surface = SurfaceLight,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = TextSecondary,
  outline = BorderLight,
  error = StatusError,
  onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.Black,
  primaryContainer = EmeraldDeep,
  onPrimaryContainer = EmeraldLight,
  secondary = AmberAccent,
  onSecondary = Color.Black,
  secondaryContainer = AmberDark,
  onSecondaryContainer = AmberLight,
  tertiary = EmeraldDark,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  error = StatusError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep SaleSnap brand colors consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
