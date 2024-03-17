package com.rokoblak.chatbackup.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rokoblak.chatbackup.ui.commonui.ButtonWithIcon
import com.rokoblak.chatbackup.ui.commonui.ConversationsListingUIState
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import kotlinx.coroutines.launch

data class HomeScaffoldUIState(
    val appbar: HomeAppbarUIState,
    val drawer: HomeDrawerUIState,
    val contentPermissions: HomeContentUIPermissionsState
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    searchQuery: String?,
    state: HomeScaffoldUIState,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onAction: (HomeAction) -> Unit,
    onLaunchPermissions: () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val coroutineScope = rememberCoroutineScope()
            ModalDrawerSheet {
                HomeDrawer(state.drawer) {
                    coroutineScope.launch {
                        drawerState.close()
                        onAction(it)
                    }
                }
            }
        }
    ) {
        HomeScaffoldContent(
            searchQuery = searchQuery,
            state = state,
            drawerState = drawerState,
            onAction = onAction,
            onLaunchPermissions = onLaunchPermissions,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffoldContent(
    searchQuery: String?,
    state: HomeScaffoldUIState,
    drawerState: DrawerState,
    onAction: (HomeAction) -> Unit,
    onLaunchPermissions: () -> Unit,
) {
    Scaffold(
        topBar = {
            val coroutineScope = rememberCoroutineScope()

            HomeTopAppBar(state.appbar, onAction) {
                coroutineScope.launch {
                    drawerState.open()
                }
            }
        },
        floatingActionButton = {
            if (state.contentPermissions is HomeContentUIPermissionsState.PermissionsGiven) {
                ButtonWithIcon(modifier = Modifier,
                    text = "Compose",
                    icon = Icons.AutoMirrored.Filled.Message,
                    onClick = { onAction(HomeAction.ComposeClicked) })
            }
        }
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()
        HomeContent(modifier = Modifier.padding(top = topPadding), searchQuery, state.contentPermissions, onAction, onLaunchPermissions)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun HomeScaffoldPreview() {
    val drawerState = HomeDrawerUIState(
        darkMode = true,
        showDefaultSMSLabel = true,
        versionLabel = "Version 1.0.0",
        showComposeAndImport = true
    )
    val appbarState = HomeAppbarUIState(
        hideIcons = false,
        showEdit = false,
        showDelete = true,
        deleteEnabled = true,
        deleteShowsPrompt = false,
    )
    ChatBackupTheme {
        val state = ConversationsListingUIState.Loading
        HomeScaffold(
            searchQuery = "",
            drawerState = DrawerState(DrawerValue.Closed),
            state = HomeScaffoldUIState(
                appbarState,
                drawerState,
                HomeContentUIPermissionsState.PermissionsGiven(
                    HomeContentUIState(
                        title = "title",
                        subtitle = "subtitle",
                        exportEnabled = true,
                        state
                    )
                )
            ),
            onAction = {}, onLaunchPermissions = {})
    }
}
