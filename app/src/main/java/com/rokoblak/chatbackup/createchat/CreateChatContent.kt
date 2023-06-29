package com.rokoblak.chatbackup.createchat

import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.commonui.AvatarData
import com.rokoblak.chatbackup.ui.commonui.ContactDisplayData
import com.rokoblak.chatbackup.ui.commonui.DetailsContent
import com.rokoblak.chatbackup.ui.commonui.SearchBar
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.collections.immutable.toImmutableList

data class CreateChatUIState(val content: Content) {
    sealed interface Content {
        data class Contacts(val items: ContactsListingData) : Content
        object Loading : Content
        data class Empty(val message: String) : Content
    }
}

@Composable
fun CreateChatContent(
    searchQuery: String,
    state: CreateChatUIState,
    onNavigateUp: () -> Unit,
    onAction: (CreateChatAction) -> Unit
) {
    DetailsContent(title = "New conversation", onBackPressed = {
        onNavigateUp()
    }) {
        when (state.content) {
            is CreateChatUIState.Content.Contacts -> {
                SearchBar(
                    text = searchQuery, placeholder = "Search contacts",
                    onChange = {
                        onAction(CreateChatAction.QueryChanged(it))
                    }, modifier = Modifier.padding(top = 8.dp)
                )

                ContactsListing(data = state.content.items, onItemClicked = { cId, num ->
                    onAction(CreateChatAction.ContactClicked(contactId = cId, number = num))
                })
            }
            is CreateChatUIState.Content.Empty -> {
                Text(state.content.message)
            }
            CreateChatUIState.Content.Loading -> {
                Text("Loading...")
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun CreateChatContentPreview() {
    ChatBackupTheme {
        val items = listOf("A", "B", "C").map { section ->
            section to (0..3).map {
                ContactDisplayData(
                    id = section + it.toString(),
                    number = it.toString(),
                    avatar = AvatarData.Initials("${section}B", Color.Blue),
                    title = "$section AD$it",
                    subtitle = "num$it",
                    type = "Mobile",
                )
            }.toImmutableList()
        }.toImmutableList()
        val state =
            CreateChatUIState(CreateChatUIState.Content.Contacts(ContactsListingData(items)))
        CreateChatContent(state = state, searchQuery = "", onNavigateUp = {}, onAction = {})
    }
}