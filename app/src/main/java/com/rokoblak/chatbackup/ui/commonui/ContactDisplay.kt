package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.rokoblak.chatbackup.ui.commonui.AvatarData
import com.rokoblak.chatbackup.ui.commonui.AvatarBadge
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

data class ContactDisplayData(
    val id: String,
    val number: String,
    val avatar: AvatarData,
    val title: String,
    val subtitle: String,
    val type: String,
)

@Composable
fun ContactDisplay(data: ContactDisplayData, modifier: Modifier = Modifier) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colors.background)
            .padding(8.dp),
    ) {
        val (avatar, title, subtitle, type) = createRefs()

        AvatarBadge(data = data.avatar, modifier = Modifier.constrainAs(avatar) {
            start.linkTo(parent.start)
            top.linkTo(parent.top)
        })

        Text(
            modifier = Modifier.constrainAs(title) {
                start.linkTo(avatar.end, 12.dp)
                top.linkTo(parent.top)
            }, text = data.title,
            style = LocalTypography.current.bodySemiBold
        )
        Text(
            modifier = Modifier.constrainAs(subtitle) {
                start.linkTo(avatar.end, 12.dp)
                top.linkTo(title.bottom)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
            text = data.subtitle,
            style = LocalTypography.current.captionRegular,
        )
        Text(
            modifier = Modifier.constrainAs(type) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            },
            text = data.type,
            style = LocalTypography.current.captionRegular,
        )
    }
}

@Preview
@Composable
fun ContactDisplayPreview() {
    ChatBackupTheme {
        val data = ContactDisplayData(
            id = "1",
            number = "num1",
            title = "Contact Name",
            subtitle = "+123 234 345",
            type = "Mobile",
            avatar = AvatarData.Initials("CN", Color.Blue)
        )

        ContactDisplay(data = data)
    }
}