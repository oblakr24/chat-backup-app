package com.rokoblak.chatbackup.conversation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.ChatDisplayData
import com.rokoblak.chatbackup.commonui.ChatListing
import com.rokoblak.chatbackup.commonui.DetailsContent
import com.rokoblak.chatbackup.commonui.PreviewDataUtils
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
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
    
    ConversationScreenContent(state = state, onNavigateUp = {
        viewModel.navigateUp()
    })
}

@Composable
private fun ConversationScreenContent(state: ConversationScreenUIState, onNavigateUp: () -> Unit) {
    DetailsContent(title = state.title, onBackPressed = {
        onNavigateUp()
    }) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = state.subtitle,
            style = LocalTypography.current.captionRegular
        )
        ChatListing(state.items)
    }
}

@Preview
@Composable
fun ConversationScreenPreview() {
    ChatBackupTheme {
        val state = ConversationScreenUIState(
            items = PreviewDataUtils.mockChats,
            title = "Conversation with Firstlongname Surnamelong",
            subtitle = "subtitle",
        )
        ConversationScreenContent(state = state, onNavigateUp = {})
    }
}