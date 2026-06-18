package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = PolishedPrimary,
    secondary = PolishedSecondary,
    tertiary = CharcoalTertiary,
    primaryContainer = PolishedPrimaryContainer,
    onPrimaryContainer = PolishedOnPrimaryContainer,
    surfaceVariant = CharcoalTertiary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PolishedPrimary,
    onPrimary = PolishedOnPrimary,
    primaryContainer = PolishedPrimaryContainer,
    onPrimaryContainer = PolishedOnPrimaryContainer,
    secondary = PolishedSecondary,
    onSecondary = Color.White,
    secondaryContainer = PolishedSecondaryContainer,
    onSecondaryContainer = PolishedOnSecondaryContainer,
    tertiary = CharcoalTertiary,
    background = BackgroundLight,
    surface = Color.White,
    surfaceVariant = BottomNavContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Brand color is available across all versions with custom branding design constraints focus
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
