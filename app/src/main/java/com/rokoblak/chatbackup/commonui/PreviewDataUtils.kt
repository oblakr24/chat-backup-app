package com.rokoblak.chatbackup.commonui

import androidx.compose.ui.graphics.Color

object PreviewDataUtils {

    val mockConversations = (0..10).map {
        ConversationDisplayData(
            contactId = "C_id1",
            id = "id$it",
            title = "title$it",
            subtitle = "subtitle",
            date = "date$it",
            checked = true,
            avatarData = InitialsAvatarData("C1", Color.Blue),
        )
    }
}