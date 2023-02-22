package com.rokoblak.chatbackup.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.*
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography
import kotlinx.collections.immutable.toImmutableList

data class HomeContentUIState(
    val title: String,
    val subtitle: String,
    val exportEnabled: Boolean,
    val state: ConversationsListingUIState,
)

@Composable
fun HomeInnerContent(
    searchQuery: String?,
    state: HomeContentUIState,
    onAction: (HomeAction) -> Unit,
) {
    val innerState = state.state
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        if (innerState is ConversationsListingUIState.Loaded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(top = 8.dp)
                ) {
                    Text(text = state.title, style = LocalTypography.current.subheadRegular)
                    Text(text = state.subtitle, style = LocalTypography.current.captionRegular)
                }
                ButtonWithIcon("Export", Icons.Filled.Upload, enabled = state.exportEnabled) {
                    onAction(HomeAction.ExportClicked)
                }
            }
        }

        if (searchQuery != null && innerState is ConversationsListingUIState.Loaded) {
            SearchBar(
                text = searchQuery, placeholder = "Filter conversations",
                onChange = {
                    onAction(HomeAction.QueryChanged(it))
                }, modifier = Modifier
            )
        }

        ConversationsListing(state = innerState, onItemClicked = { cId, num ->
            onAction(HomeAction.ConversationClicked(cId, num))
        }, onCheckedChanged = { cId, checked ->
            onAction(HomeAction.ConversationChecked(cId, checked))
        }, onImportClicked = {
            onAction(HomeAction.ImportClicked)
        })
    }
}

@Preview
@Composable
fun HomeInnerContentPreview() {
    ChatBackupTheme {
        val mockItems = PreviewDataUtils.mockConversations
        val convState = ConversationsListingUIState.Loaded(mockItems.toImmutableList())
        val state = HomeContentUIState(
            title = "title",
            subtitle = "subtitle",
            state = convState,
            exportEnabled = true,
        )

        HomeInnerContent(state = state, searchQuery = "Search query", onAction = {})
    }
}