package com.rokoblak.chatbackup.ui.theme

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.ui.graphics.Color

val PrimaryDark = Color.DarkGray
val PrimaryLight = Color(0xFFCCCCE6)
val PrimaryVariantLight = Color(0xFFB8B8CE)
val PrimaryVariantDark = Color(0xFF303030)

val SecondaryLight = Color.DarkGray
val SecondaryDark = Color.LightGray

val DarkRed = Color(0xFFD50F00)
val DarkYellow = Color(0xFFDBA400)
val DarkOrange = Color(0xFFCE3100)
val DarkBrown = Color(0xFF882D10)
val DarkGreen = Color(0xFF008805)

fun Color.alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Color = this.copy(alpha = alpha)
fun Color.alpha(@IntRange(from = 0, to = 100) alpha: Int): Color =
    this.copy(alpha = alpha.toFloat().div(100f))
