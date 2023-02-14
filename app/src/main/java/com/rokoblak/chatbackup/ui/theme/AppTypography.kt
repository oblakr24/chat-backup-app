package com.rokoblak.chatbackup.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class AppTypography(
    val title: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        letterSpacing = (-0.45).sp
    ),
    val titleSemiBold: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.45).sp
    ),
    val headline: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = (-0.45).sp
    ),
    val bodyRegular: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = (-0.23).sp
    ),
    val bodySemiBold: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = (-0.23).sp
    ),
    val subheadRegular: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = (-0.08).sp
    ),
    val subheadSemiBold: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = (-0.08).sp
    ),
    val captionRegular: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.06.sp
    ),
)

val LocalTypography = staticCompositionLocalOf { AppTypography() }

// TODO: This is not currently used in the composables, since it has a limited set of styles - determine what to do with it
val ChatBackupTypography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)