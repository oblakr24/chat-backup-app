package com.rokoblak.chatbackup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = PrimaryDark,
    primaryVariant = PrimaryVariantDark,
    secondary = SecondaryDark,
    onPrimary = Color.White,
)

private val LightColorPalette = lightColors(
    primary = PrimaryLight,
    primaryVariant = PrimaryVariantLight,
    secondary = SecondaryLight,
    onPrimary = Color.Black,
    background = Color(0xFFFAFDFF),
)

@Composable
fun ChatBackupTheme(
    overrideDarkMode: Boolean? = null,
    darkTheme: Boolean = overrideDarkMode ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = ChatBackupTypography,
        shapes = Shapes,
        content = content
    )
}