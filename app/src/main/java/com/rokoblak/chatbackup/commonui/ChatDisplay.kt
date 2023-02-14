package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

data class ChatDisplayData(
    val id: String,
    val content: String,
    val date: String,
    val alignedLeft: Boolean,
    val avatarData: InitialsAvatarData?,
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
            if (data.avatarData != null) {
                InitialsAvatar(
                    data = data.avatarData,
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
                        .background(backgroundColor, RoundedCornerShape(8.dp))
                        .align(alignment)
                        .widthIn(min = 20.dp, max = 220.dp)
                        .padding(8.dp),
                    text = data.content,
                    style = LocalTypography.current.bodyRegular,
                    color = textColor,
                )
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
                avatarData = InitialsAvatarData("AB", Color.Blue),
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
                avatarData = null,
            )
        )
    }
}