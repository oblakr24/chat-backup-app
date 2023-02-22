package com.rokoblak.chatbackup.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.ChatDisplayData
import com.rokoblak.chatbackup.commonui.ChatListing
import com.rokoblak.chatbackup.commonui.DetailsHeader
import com.rokoblak.chatbackup.commonui.PreviewDataUtils
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.collections.immutable.ImmutableList

data class ConversationScreenUIState(
    val items: ImmutableList<ChatDisplayData>,
    val title: String = "",
    val subtitle: String = "",
    val showInput: Boolean,
)

@Composable
fun ConversationScreen(viewModel: ConversationViewModel) {
    val state = viewModel.uiState.collectAsState().value
    val input = viewModel.input.collectAsState().value

    ConversationScreenContent(state = state, input = input, onNavigateUp = {
        viewModel.navigateUp()
    }, onAction = { act ->
        viewModel.handleAction(act)
    })
}

@Composable
private fun ConversationScreenContent(
    state: ConversationScreenUIState,
    input: String,
    onNavigateUp: () -> Unit,
    onAction: (ConversationAction) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colors.surface)
    ) {
        DetailsHeader(
            text = state.title,
            rightButtonText = null,
            onIconPressed = null,
            onBackPressed = { onNavigateUp() }
        )
        ChatListing(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), subtitle = state.subtitle, items = state.items)
        if (state.showInput) {
            InputBar(
                input = input,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp, start = 8.dp, end = 8.dp),
                onChange = {
                    onAction(ConversationAction.InputChanged(it))
                },
                onSend = {
                    onAction(ConversationAction.SendClicked(it))
                })
        }
    }
}

@Preview
@Composable
fun ConversationScreenPreview() {
    ChatBackupTheme {
        val state = ConversationScreenUIState(
            items = PreviewDataUtils.mockChats,
            title = "Conversation with Firstlongname Surnamelong",
            subtitle = "9 messages (May 19 2022 to Jun 23 2022)",
            showInput = true,
        )
        ConversationScreenContent(state = state, input = "input", onNavigateUp = {}, onAction = {})
    }
}