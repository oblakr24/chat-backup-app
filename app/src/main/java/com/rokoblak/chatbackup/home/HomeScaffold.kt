package com.rokoblak.chatbackup.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.ConversationsListingUIState
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.coroutines.launch

data class HomeScaffoldUIState(
    val appbar: HomeAppbarUIState,
    val drawer: HomeDrawerUIState,
    val contentPermissions: HomeContentUIPermissionsState
)

@Composable
fun HomeScaffold(
    searchQuery: String?,
    state: HomeScaffoldUIState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onAction: (HomeAction) -> Unit,
    onLaunchPermissions: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            HomeTopAppBar(state.appbar, onAction) {
                coroutineScope.launch {
                    scaffoldState.drawerState.open()
                }
            }
        },
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                HomeDrawer(state.drawer) {
                    coroutineScope.launch {
                        scaffoldState.drawerState.close()
                        onAction(it)
                    }
                }
            }
        }, content = {
            it.calculateBottomPadding()
            HomeContent(searchQuery, state.contentPermissions, onAction, onLaunchPermissions)
        })
}

@Preview
@Composable
private fun HomeScaffoldPreview() {
    val drawerOpenState = DrawerValue.Closed
    val drawerState = HomeDrawerUIState(darkMode = true, showDefaultSMSLabel = true, versionLabel = "Version 1.0.0")
    val appbarState = HomeAppbarUIState(
        hideIcons = false,
        showEdit = false,
        showDelete = true,
        deleteEnabled = true,
        deleteShowsPrompt = false,
    )
    ChatBackupTheme {
        val state = ConversationsListingUIState.Loading
        val scaffoldState = ScaffoldState(DrawerState(drawerOpenState), SnackbarHostState())
        HomeScaffold(
            searchQuery = "",
            scaffoldState = scaffoldState,
            state = HomeScaffoldUIState(
                appbarState,
                drawerState,
                HomeContentUIPermissionsState.PermissionsGiven(
                    HomeContentUIState(title = "title", subtitle = "subtitle", exportEnabled = true, state)
                )
            ),
            onAction = {}, onLaunchPermissions = {})
    }
}