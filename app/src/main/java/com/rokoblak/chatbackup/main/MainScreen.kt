package com.rokoblak.chatbackup.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rokoblak.chatbackup.conversation.ConversationRoute
import com.rokoblak.chatbackup.createchat.CreateChatRoute
import com.rokoblak.chatbackup.export.ExportRoute
import com.rokoblak.chatbackup.faq.FAQRoute
import com.rokoblak.chatbackup.home.HomeRoute
import com.rokoblak.chatbackup.importfile.ImportRoute
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

data class MainScreenUIState(
    val isDarkTheme: Boolean?,
)

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState().value

    val navController = rememberNavController()

    ChatBackupTheme(overrideDarkMode = state.isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            MainNavHostContainer(navController)
        }
    }
}

@Composable
private fun MainNavHostContainer(navController: NavHostController) {
    NavHost(navController = navController, startDestination = HomeRoute.route) {
        HomeRoute.register(this, navController)
        FAQRoute.register(this, navController)
        ConversationRoute.register(this, navController)
        ImportRoute.register(this, navController)
        ExportRoute.register(this, navController)
        CreateChatRoute.register(this, navController)
    }
}