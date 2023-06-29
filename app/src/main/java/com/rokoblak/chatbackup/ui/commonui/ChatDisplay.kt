package com.rokoblak.chatbackup.ui.commonui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

data class ChatDisplayData(
    val id: String,
    val content: String,
    val date: String,
    val alignedLeft: Boolean,
    val avatar: AvatarData?,
    val imageUri: String?,
)

@Composable
fun ChatDisplay(modifier: Modifier = Modifier, data: ChatDisplayData) {
    val alignment = if (data.alignedLeft) Alignment.Start else Alignment.End
    val backgroundColor = if (data.alignedLeft) Color.LightGray else Color.Blue
    val textColor = if (data.alignedLeft) Color.Black else Color.White
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(modifier = Modifier.wrapContentWidth()) {
            if (data.avatar != null) {
                AvatarBadge(
                    data = data.avatar,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(28.dp),
                    textStyle = LocalTypography.current.subheadSemiBold
                )
            }
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(4.dp)
            ) {
                Text(
                    modifier = Modifier
                        .background(backgroundColor, RoundedCornerShape(12.dp))
                        .align(alignment)
                        .widthIn(min = 20.dp, max = 220.dp)
                        .padding(8.dp),
                    text = data.content,
                    style = LocalTypography.current.bodyRegular,
                    color = textColor,
                )
                if (data.imageUri != null) {
                    Surface(
                        modifier = modifier
                            .size(220.dp)
                            .padding(vertical = 8.dp)
                            .border(1.dp, MaterialTheme.colors.background, CircleShape),
                        color = MaterialTheme.colors.background,
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AsyncImage(model = Uri.parse(data.imageUri), contentDescription = null)
                        }
                    }
                }
                Text(
                    modifier = Modifier.padding(2.dp),
                    text = data.date,
                    style = LocalTypography.current.subheadRegular
                )
            }
        }
    }
}

@Preview
@Composable
fun ChatDisplayOtherPreview() {
    ChatBackupTheme {
        ChatDisplay(
            modifier = Modifier,
            data = ChatDisplayData(
                id = "id1",
                content = "Content Looong Content Looooong Looong Looong",
                date = "Sun 14th Dec 2022, 13:44:55",
                alignedLeft = true,
                avatar = AvatarData.Initials("AB", Color.Blue),
                imageUri = null,
            )
        )
    }
}

@Preview
@Composable
fun ChatDisplayMinePreview() {
    ChatBackupTheme {
        ChatDisplay(
            modifier = Modifier,
            data = ChatDisplayData(
                id = "id1",
                content = "Content",
                date = "Sun 14th Dec 2022, 13:44:55",
                alignedLeft = false,
                avatar = null,
                imageUri = null,
            )
        )
    }
}