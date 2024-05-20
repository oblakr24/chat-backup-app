package com.rokoblak.chatbackup.feature.importfile

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
data object ImportRoute : AppRoute {

    @Composable
    fun Content(navHostController: NavHostController) {
        val viewModel = hiltViewModel<ImportFileViewModel>()
        setupNavigation(navHostController = navHostController, vm = viewModel)
        ImportScreen(viewModel)
    }

    fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<ImportRoute> {
            it.toRoute<ImportRoute>().Content(navHostController = navController)
        }
    }
}
