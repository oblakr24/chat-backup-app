package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.AppThemePreviews
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.alpha
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListing(
    modifier: Modifier = Modifier,
    subtitle: String,
    items: ImmutableList<ChatDisplayData>
) {
    val state = rememberLazyListState()
    var initialScrollDone by remember {
        mutableStateOf(false)
    }
    val itemsSize = remember(items) {
        derivedStateOf {
            items.size
        }
    }
    LaunchedEffect(itemsSize) {
        if (itemsSize.value > 0) {
            if (initialScrollDone) {
                state.animateScrollToItem(itemsSize.value - 1)
            } else {
                initialScrollDone = true
                state.scrollToItem(itemsSize.value - 1)
            }
        }
    }
    LazyColumn(modifier = modifier, state = state) {
        stickyHeader {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.alpha(0.2f))
                    .padding(8.dp),
                text = subtitle,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
            )
        }
        items(
            count = items.size,
            key = { items[it].id },
            itemContent = {
                ChatDisplay(data = items[it], modifier = Modifier.padding(horizontal = 8.dp))
            }
        )
    }
}

@AppThemePreviews
@Preview
@Composable
fun ChatListingPreview() {
    ChatBackupTheme {
        ChatListing(items = PreviewDataUtils.mockChats, subtitle = "Subtitle")
    }
}