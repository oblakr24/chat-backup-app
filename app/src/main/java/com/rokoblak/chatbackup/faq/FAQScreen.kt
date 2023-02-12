package com.rokoblak.chatbackup.faq

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.chatbackup.commonui.DetailsContent
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography
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
        LazyColumn(state = rememberLazyListState()) {
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
                                    .padding(start = 12.dp, bottom = 8.dp)
                                    .animateItemPlacement(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.subtitle, style = LocalTypography.current.subheadRegular)
                            }
                        }
                        is FAQScreenUIState.Item.Title -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItemPlacement(),
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
                                Text(item.title, style = LocalTypography.current.bodySemiBold)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun FAQScreenPreview() {
    ChatBackupTheme {
        val mockItems = (0..10).map {
            if (it.mod(2) == 0) {
                FAQScreenUIState.Item.Title(it.toString(), it, "title $it", expanded = true)
            } else {
                FAQScreenUIState.Item.Subtitle(it.toString(), "subtitle $it")
            }
        }
        val state = FAQScreenUIState(mockItems.toImmutableList())
        FAQScreenContent(state = state, onNavigateUp = {}, onAction = {})
    }
}