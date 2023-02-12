package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

data class InitialsAvatarData(
    val initials: String,
    val color: Color,
)

@Composable
fun InitialsAvatar(data: InitialsAvatarData, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(36.dp)
            .border(1.dp, data.color, CircleShape),
        color = MaterialTheme.colors.background,
        shape = CircleShape,
        elevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = data.initials,
                color = data.color,
                style = LocalTypography.current.bodySemiBold,
            )
        }
    }
}

@Preview
@Composable
fun InitialsAvatarPreview() {
    ChatBackupTheme {
        val data = InitialsAvatarData("RO", Color.Red)
        InitialsAvatar(data)
    }
}

@Preview
@Composable
fun InitialsAvatarPreviewUnknown() {
    ChatBackupTheme {
        val data = InitialsAvatarData("?", Color.Gray)
        InitialsAvatar(data)
    }
}