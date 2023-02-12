package com.rokoblak.chatbackup.importfile

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.*
import com.rokoblak.chatbackup.commonui.PreviewDataUtils.mockConversations
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed interface ImportScreenUIState {
    object Initial : ImportScreenUIState
    object Loading : ImportScreenUIState
    data class Loaded(
        val title: String,
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
                    ButtonWithIcon("Import XML", icon = Icons.Filled.DataArray) {
                        onAction(ImportAction.ImportXMLClicked)
                    }
                }
            }
            ImportScreenUIState.Loading -> {
                ConversationsListing(
                    ConversationsListingUIState.Loading,
                    onItemClicked = {},
                    onCheckedChanged = { _, _ -> }, onImportClicked = {})
            }
            is ImportScreenUIState.Loaded -> {
                ImportTopToolbar(state = state.toolbar, onAction = onAction)
                val conversationsState = ConversationsListingUIState.Loaded(state.listing)
                ConversationsListing(
                    conversationsState,
                    onItemClicked = { cId ->
                        onAction(ImportAction.ConversationClicked(cId))
                    },
                    onCheckedChanged = { cId, checked ->
                        onAction(ImportAction.ConversationChecked(cId, checked))
                    }, onImportClicked = {})
            }
        }
    }
}

@Preview
@Composable
fun ImportContentPreview() {
    ChatBackupTheme {
        val state = ImportScreenUIState.Loaded(
            title = "1234.json",
            toolbar = ImportTopToolbarUIState(showEdit = true, downloadShowsPrompt = true),
            listing = mockConversations.toImmutableList(),
        )
        ImportContent(state = state, onBackPressed = {}, onAction = {})
    }
}