package com.rokoblak.chatbackup.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(12.dp)
                ) {
                    Text(text = state.title)
                    Text(text = state.subtitle, style = LocalTypography.current.subheadRegular)
                }
                ButtonWithIcon("Export", Icons.Filled.Upload, enabled = state.exportEnabled) {
                    onAction(HomeAction.ExportClicked)
                }
            }
        }

        ConversationsListing(state = innerState, onItemClicked = { cId ->
            onAction(HomeAction.ConversationClicked(cId))
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
        val mockItems = (0..20).map {
            ConversationDisplayData(
                it.toString(),
                id = it.toString(),
                title = "title $it",
                subtitle = "subtitle $it",
                date = "date $it",
                checked = null,
                avatarData = InitialsAvatarData("CO", Color.Blue),
            )
        }
        val convState = ConversationsListingUIState.Loaded(mockItems.toImmutableList())
        val state = HomeContentUIState(
            title = "title",
            subtitle = "subtitle",
            state = convState,
            exportEnabled = true,
        )

        HomeInnerContent(state = state, onAction = {})
    }
}