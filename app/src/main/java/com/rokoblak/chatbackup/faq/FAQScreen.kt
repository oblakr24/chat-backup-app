package com.rokoblak.chatbackup.faq

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.chatbackup.ui.commonui.DetailsContent
import com.rokoblak.chatbackup.ui.theme.AppThemePreviews
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class FAQScreenUIState(
    val items: ImmutableList<Item>,
) {
    sealed class Item(open val id: String) {
        data class Title(
            override val id: String,
            val idx: Int,
            val title: String,
            val expanded: Boolean
        ) : Item(id)

        data class Subtitle(override val id: String, val subtitle: String) : Item(id)
    }
}

@Composable
fun FAQScreen(viewModel: FAQViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState().value

    FAQScreenContent(state = state, onNavigateUp = {
        viewModel.navigateUp()
    }, onAction = {
        viewModel.handleAction(it)
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FAQScreenContent(
    state: FAQScreenUIState,
    onNavigateUp: () -> Unit,
    onAction: (FAQAction) -> Unit
) {
    DetailsContent(title = "FAQ", onBackPressed = {
        onNavigateUp()
    }) {
        LazyColumn(
            state = rememberLazyListState(),
        ) {
            val items = state.items
            items(
                count = items.size,
                key = { items[it].id },
                itemContent = {
                    when (val item = items[it]) {
                        is FAQScreenUIState.Item.Subtitle -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, bottom = 8.dp, end = 12.dp)
                                    .animateItemPlacement(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.subtitle, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        is FAQScreenUIState.Item.Title -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement()
                                    .clickable {
                                        onAction(FAQAction.ItemExpandedCollapsed(item.idx))
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    onAction(FAQAction.ItemExpandedCollapsed(item.idx))
                                }) {
                                    Icon(
                                        imageVector = if (item.expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "ExpandCollapse"
                                    )
                                }
                                Text(item.title, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            )
        }
    }
}

@AppThemePreviews
@Preview
@Composable
fun FAQScreenPreview() {
    ChatBackupTheme {
        val mockItems = (0..10).map {
            if (it.mod(2) == 0) {
                FAQScreenUIState.Item.Title(it.toString(), it, "title $it", expanded = true)
            } else {
                FAQScreenUIState.Item.Subtitle(
                    it.toString(),
                    "subtitle $it long long long long subtitle to make it go into two separate lines "
                )
            }
        }
        val state = FAQScreenUIState(mockItems.toImmutableList())
        FAQScreenContent(state = state, onNavigateUp = {}, onAction = {})
    }
}