package com.rokoblak.chatbackup.feature.conversation

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
data class ConversationRoute(
    val resolvedContactId: String?, // In case we already have a contact loaded
    val address: String,
    val isImport: Boolean = false,
): AppRoute {

    @Composable
    fun Content(navHostController: NavHostController) {
        val viewModel = hiltViewModel<ConversationViewModel, ConversationViewModel.Factory>(
            creationCallback = { factory -> factory.create(this) }
        )
        setupNavigation(navHostController = navHostController, vm = viewModel)
        ConversationScreen(viewModel)
    }

    companion object {
        fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
            navGraphBuilder.composable<ConversationRoute> {
                it.toRoute<ConversationRoute>().Content(navHostController = navController)
            }
        }
    }
}
