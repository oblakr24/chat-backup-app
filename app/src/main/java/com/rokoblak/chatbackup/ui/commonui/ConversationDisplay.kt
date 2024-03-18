package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

data class ConversationDisplayData(
    val contactId: String,
    val number: String,
    val id: String,
    val title: AnnotatedString,
    val subtitle: AnnotatedString,
    val date: String,
    val checked: Boolean?,
    val avatar: AvatarData,
)

@Composable
fun ConversationDisplay(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
    data: ConversationDisplayData,
    onCheckedChanged: (Boolean) -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(background)
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

        AvatarBadge(data = data.avatar, modifier = Modifier.constrainAs(avatar) {
            start.linkTo(checkbox.end)
            top.linkTo(parent.top)
        })

        Text(
            modifier = Modifier.constrainAs(title) {
                start.linkTo(avatar.end, 12.dp)
                top.linkTo(parent.top)
                end.linkTo(date.start, 4.dp)
                width = Dimension.fillToConstraints
            }, text = data.title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            modifier = Modifier.constrainAs(subtitle) {
                start.linkTo(avatar.end, 12.dp)
                top.linkTo(title.bottom)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
            text = data.subtitle,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            modifier = Modifier.constrainAs(date) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            },
            text = data.date,
            style = MaterialTheme.typography.labelSmall,
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
                number = "num1",
                title = AnnotatedString("Conv title"),
                subtitle = AnnotatedString("conv subtitle long message to make it really long and fit more than one line"),
                date = "13th Mar 2022 19:45:44",
                checked = true,
                avatar = AvatarData.Initials("CO", Color.Blue),
            ),
            onCheckedChanged = {}
        )
    }
}

@Preview
@Composable
fun ConversationDisplayLongNamePreview() {
    ChatBackupTheme {
        ConversationDisplay(
            data = ConversationDisplayData(
                contactId = "C_id1",
                id = "id1",
                number = "num1",
                title = AnnotatedString("Conv title - Very Long name"),
                subtitle = AnnotatedString("conv subtitle long message to make it really long and fit more than one line"),
                date = "13th Mar 2022 19:45:44",
                checked = true,
                avatar = AvatarData.Initials("CO", Color.Blue),
            ),
            onCheckedChanged = {}
        )
    }
}