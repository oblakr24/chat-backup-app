package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.PreviewDataUtils.mockConversations
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed interface ConversationsListingUIState {
    object Empty : ConversationsListingUIState
    object Loading : ConversationsListingUIState
    data class Loaded(val items: ImmutableList<ConversationDisplayData>) :
        ConversationsListingUIState
}

@Composable
fun ConversationsListing(
    state: ConversationsListingUIState,
    onItemClicked: (contactId: String) -> Unit,
    onCheckedChanged: (contactId: String, checked: Boolean) -> Unit,
    onImportClicked: () -> Unit,
) {
    LazyColumn(state = rememberLazyListState()) {
        when (state) {
            ConversationsListingUIState.Empty -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = "No messages on this device. Import?"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ButtonWithIcon("Import", Icons.Filled.ImportExport) {
                            onImportClicked()
                        }
                    }
                }

            }
            ConversationsListingUIState.Loading -> {
                item {
                    Text(modifier = Modifier.padding(12.dp), text = "Loading messages...")
                }
            }
            is ConversationsListingUIState.Loaded -> {
                items(
                    count = state.items.size,
                    key = { state.items[it].id },
                    itemContent = { idx ->
                        val data = state.items[idx]
                        ConversationDisplay(modifier = Modifier.clickable {
                            onItemClicked(data.contactId)
                        }, data = data, onCheckedChanged = { checked ->
                            onCheckedChanged(data.contactId, checked)
                        })

                        if (idx < state.items.lastIndex) {
                            Divider(color = MaterialTheme.colors.primary, thickness = 1.dp)
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun ConversationListingPreview() {
    ChatBackupTheme {
        ConversationsListing(
            state = ConversationsListingUIState.Loaded(mockConversations.toImmutableList()),
            onItemClicked = {}, onCheckedChanged = { _, _ -> }, onImportClicked = {})

    }
}