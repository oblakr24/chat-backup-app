package com.rokoblak.chatbackup.feature.importfile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.commonui.ButtonWithIcon
import com.rokoblak.chatbackup.ui.commonui.ConversationDisplayData
import com.rokoblak.chatbackup.ui.commonui.ConversationsListing
import com.rokoblak.chatbackup.ui.commonui.ConversationsListingUIState
import com.rokoblak.chatbackup.ui.commonui.DetailsContent
import com.rokoblak.chatbackup.ui.commonui.PreviewDataUtils.mockConversations
import com.rokoblak.chatbackup.ui.theme.AppThemePreviews
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.collections.immutable.ImmutableList

sealed interface ImportScreenUIState {
    object Initial : ImportScreenUIState
    object Loading : ImportScreenUIState
    data class Loaded(
        val title: String,
        val subtitle: String?,
        val showLoading: Boolean,
        val toolbar: ImportTopToolbarUIState,
        val listing: ImmutableList<ConversationDisplayData>
    ) : ImportScreenUIState
}

@Composable
fun ImportContent(
    state: ImportScreenUIState,
    onBackPressed: () -> Unit,
    onAction: (ImportAction) -> Unit
) {
    val title = (state as? ImportScreenUIState.Loaded)?.title ?: "Import"
    DetailsContent(title, onBackPressed = onBackPressed) {
        when (state) {
            ImportScreenUIState.Initial -> {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Import a file saved by this app. It needs to be of the right format, otherwise the import will fail.\n\nYou can choose a file from local or cloud storage.",
                    modifier = Modifier.padding(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    ButtonWithIcon("Import JSON", icon = Icons.Filled.DataObject) {
                        onAction(ImportAction.ImportJSONClicked)
                    }
                }
            }
            ImportScreenUIState.Loading -> {
                ConversationsListing(
                    ConversationsListingUIState.Loading,
                    onItemClicked = { _, _ -> },
                    onCheckedChanged = { _, _ -> }, onImportClicked = {})
            }
            is ImportScreenUIState.Loaded -> {
                ImportTopToolbar(state = state.toolbar, onAction = onAction)
                if (state.subtitle != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.showLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text(state.subtitle, style = MaterialTheme.typography.labelSmall)
                    }
                }
                val conversationsState = ConversationsListingUIState.Loaded(state.listing)
                ConversationsListing(
                    conversationsState,
                    onItemClicked = { cId, num ->
                        onAction(ImportAction.ConversationClicked(cId, num))
                    },
                    onCheckedChanged = { cId, checked ->
                        onAction(ImportAction.ConversationChecked(cId, checked))
                    }, onImportClicked = {})
            }
        }
    }
}

@AppThemePreviews
@Preview
@Composable
fun ImportContentPreview() {
    ChatBackupTheme {
        val state = ImportScreenUIState.Loaded(
            title = "1234.json",
            toolbar = ImportTopToolbarUIState(
                showEdit = true,
                downloadShowsPrompt = true,
                deleteEnabled = true,
                downloadEnabled = true
            ),
            listing = mockConversations,
            subtitle = "450/4000 downloaded",
            showLoading = true,
        )
        ImportContent(state = state, onBackPressed = {}, onAction = {})
    }
}