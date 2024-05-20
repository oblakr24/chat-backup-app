package com.rokoblak.chatbackup.feature.createchat

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rokoblak.chatbackup.ui.navigation.AppRoute
import com.rokoblak.chatbackup.ui.navigation.setupNavigation
import kotlinx.serialization.Serializable

@Serializable
data object CreateChatRoute : AppRoute {

    @Composable
    fun Content(navHostController: NavHostController) {
        val viewModel = hiltViewModel<CreateChatViewModel>()
        setupNavigation(navHostController = navHostController, vm = viewModel)
        CreateChatScreen(viewModel)
    }

    fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<CreateChatRoute> {
            it.toRoute<CreateChatRoute>().Content(navHostController = navController)
        }
    }
}
