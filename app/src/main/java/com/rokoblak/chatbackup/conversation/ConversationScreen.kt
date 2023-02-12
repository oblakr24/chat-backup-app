package com.rokoblak.chatbackup.conversation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.ChatDisplayData
import com.rokoblak.chatbackup.commonui.ChatListing
import com.rokoblak.chatbackup.commonui.DetailsContent
import com.rokoblak.chatbackup.ui.theme.LocalTypography
import kotlinx.collections.immutable.ImmutableList

data class ConversationScreenUIState(
    val items: ImmutableList<ChatDisplayData>,
    val title: String = "",
    val subtitle: String = "",
)

@Composable
fun ConversationScreen(viewModel: ConversationViewModel) {
    val state = viewModel.uiState.collectAsState().value
    DetailsContent(title = state.title, onBackPressed = {
        viewModel.navigateUp()
    }) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = state.subtitle,
            style = LocalTypography.current.captionRegular
        )
        ChatListing(state.items)
    }
}