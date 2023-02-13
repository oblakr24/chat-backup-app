package com.rokoblak.chatbackup.commonui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

object PreviewDataUtils {

    val mockConversations = (0..10).map {
        ConversationDisplayData(
            contactId = "C_id1",
            id = "id$it",
            title = AnnotatedString("title$it"),
            subtitle = AnnotatedString("subtitle"),
            date = "date$it",
            checked = true,
            avatarData = InitialsAvatarData("C1", Color.Blue),
        )
    }
}