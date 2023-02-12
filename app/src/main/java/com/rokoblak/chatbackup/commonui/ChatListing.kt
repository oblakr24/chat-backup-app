package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ChatListing(items: ImmutableList<ChatDisplayData>) {
    LazyColumn(modifier = Modifier.padding(8.dp), state = rememberLazyListState()) {
        items(
            count = items.size,
            key = { items[it].id },
            itemContent = {
                ChatDisplay(data = items[it])
            }
        )
    }
}

@Preview
@Composable
fun ChatListingPreview() {
    ChatBackupTheme {
        val mockChats = (0..20).map {
            val isMine = it.mod(2) == 0
            ChatDisplayData(it.toString(), content = "content $it long text", date = "date for $it", alignedLeft = isMine.not())
        }
        ChatListing(items = mockChats.toImmutableList())
    }
}