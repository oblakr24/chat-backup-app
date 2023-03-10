package com.rokoblak.chatbackup.createchat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.ContactDisplay
import com.rokoblak.chatbackup.commonui.ContactDisplayData
import com.rokoblak.chatbackup.commonui.SectionItem
import com.rokoblak.chatbackup.commonui.verticalScrollbar
import kotlinx.collections.immutable.ImmutableList

data class ContactsListingData(
    val items: ImmutableList<Pair<String, ImmutableList<ContactDisplayData>>>,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsListing(data: ContactsListingData, onItemClicked: (cId: String, number: String) -> Unit) {
    val lazyListState = rememberLazyListState()
    LazyColumn(state = lazyListState, modifier = Modifier.verticalScrollbar(lazyListState)) {
        data.items.forEach { (section, items) ->
            stickyHeader {
                SectionItem(data = section)
            }
            items(
                count = items.size,
                key = { items[it].id },
                itemContent = { idx ->
                    val item = items[idx]
                    ContactDisplay(
                        modifier = Modifier
                            .clickable {
                                onItemClicked(item.id, item.number)
                            }
                            .animateItemPlacement(),
                        data = item,
                    )
                    if (idx < data.items.lastIndex) {
                        Divider(color = MaterialTheme.colors.primary, thickness = 1.dp)
                    }
                }
            )
        }
    }
}