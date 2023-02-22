package com.rokoblak.chatbackup.commonui

import android.net.Uri
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

sealed interface AvatarData {
    data class Initials(
        val initials: String,
        val color: Color,
    ): AvatarData

    data class LocalPhoto(val uri: String): AvatarData
}



@Composable
fun AvatarBadge(
    data: AvatarData,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTypography.current.bodySemiBold,
) {
    when (data) {
        is AvatarData.Initials -> {
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
                        style = textStyle,
                    )
                }
            }
        }
        is AvatarData.LocalPhoto -> {
            Surface(
                modifier = modifier
                    .size(36.dp)
                    .border(1.dp, MaterialTheme.colors.background, CircleShape),
                color = MaterialTheme.colors.background,
                shape = CircleShape,
                elevation = 2.dp,
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(model = Uri.parse(data.uri), contentDescription = null)
                }
            }
        }
    }

}

@Preview
@Composable
fun InitialsAvatarPreview() {
    ChatBackupTheme {
        val data = AvatarData.Initials("RO", Color.Red)
        AvatarBadge(data)
    }
}

@Preview
@Composable
fun InitialsAvatarPreviewUnknown() {
    ChatBackupTheme {
        val data = AvatarData.Initials("?", Color.Gray)
        AvatarBadge(data)
    }
}