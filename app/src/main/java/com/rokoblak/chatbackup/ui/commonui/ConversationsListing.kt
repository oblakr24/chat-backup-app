package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.rokoblak.chatbackup.ui.commonui.PreviewDataUtils.mockConversations
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.alpha
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed interface ConversationsListingUIState {
    object Empty : ConversationsListingUIState
    object Loading : ConversationsListingUIState
    data class Loaded(
        val items: ImmutableList<ConversationDisplayData>,
        val headerTitle: String? = null
    ) :
        ConversationsListingUIState
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationsListing(
    state: ConversationsListingUIState,
    onItemClicked: (contactId: String, number: String) -> Unit,
    onCheckedChanged: (contactId: String, checked: Boolean) -> Unit,
    onImportClicked: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(state = lazyListState, modifier = Modifier.verticalScrollbar(lazyListState)) {
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
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background.alpha(0.8f))
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Loading messages...")
                    }
                }
            }
            is ConversationsListingUIState.Loaded -> {
                if (state.headerTitle != null) {
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colors.background.alpha(0.8f))
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = state.headerTitle)
                        }
                    }
                }
                items(
                    count = state.items.size,
                    key = { state.items[it].id },
                    itemContent = { idx ->
                        val data = state.items[idx]
                        ConversationDisplay(modifier = Modifier
                            .clickable {
                                onItemClicked(data.contactId, data.number)
                            }
                            .animateItemPlacement(),
                            background = if (idx.mod(2) == 1) {
                                MaterialTheme.colors.primaryVariant.alpha(0.16f)
                            } else {
                                MaterialTheme.colors.background
                            },
                            data = data,
                            onCheckedChanged = { checked ->
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
            state = ConversationsListingUIState.Loaded(
                mockConversations.toImmutableList(),
            ),
            onItemClicked = { _, _ ->

            }, onCheckedChanged = { _, _ -> }, onImportClicked = {})

    }
}