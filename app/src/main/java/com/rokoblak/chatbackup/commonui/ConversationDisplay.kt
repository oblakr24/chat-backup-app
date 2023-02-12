package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

data class ConversationDisplayData(
    val contactId: String,
    val id: String,
    val title: String,
    val subtitle: String,
    val date: String,
    val checked: Boolean?,
    val avatarData: InitialsAvatarData,
)

@Composable
fun ConversationDisplay(
    modifier: Modifier = Modifier,
    data: ConversationDisplayData,
    onCheckedChanged: (Boolean) -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colors.background)
            .padding(8.dp),
    ) {
        val (avatar, checkbox, title, subtitle, date) = createRefs()

        if (data.checked != null) {
            Checkbox(
                modifier = Modifier
                    .constrainAs(checkbox) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                    }
                    .height(36.dp),
                checked = data.checked,
                onCheckedChange = onCheckedChanged,
                colors = CheckboxDefaults.colors(),
            )
        } else {
            Spacer(modifier = Modifier
                .widthIn(8.dp)
                .constrainAs(checkbox) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                })
        }

        InitialsAvatar(data = data.avatarData, modifier = Modifier.constrainAs(avatar) {
            start.linkTo(checkbox.end)
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
            },
            text = data.subtitle,
            style = LocalTypography.current.captionRegular,
        )
        Text(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            },
            text = data.date,
            style = LocalTypography.current.captionRegular,
        )
    }
}

@Preview
@Composable
fun ConversationDisplayPreview() {
    ChatBackupTheme {
        ConversationDisplay(
            data = ConversationDisplayData(
                contactId = "C_id1",
                id = "id1",
                title = "Conv title",
                subtitle = "conv subtitle long message to make it really long",
                date = "13th Mar 2022 19:45:44",
                checked = true,
                avatarData = InitialsAvatarData("CO", Color.Blue),
            ),
            onCheckedChanged = {}
        )
    }
}