package com.rokoblak.chatbackup.ui.theme

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.ui.graphics.Color

val PrimaryDark = Color.DarkGray
val PrimaryLight = Color(0xFFCCCCE0)
val PrimaryVariantLight = Color(0xFFC3C3D6)
val PrimaryVariantDark = Color(0xFF303030)

val SecondaryLight = Color.DarkGray
val SecondaryDark = Color.LightGray

fun Color.alpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Color = this.copy(alpha = alpha)
fun Color.alpha(@IntRange(from = 0, to = 100) alpha: Int): Color =
    this.copy(alpha = alpha.toFloat().div(100f))
