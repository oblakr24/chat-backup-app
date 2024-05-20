package com.rokoblak.chatbackup.feature.export

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
data object ExportRoute : AppRoute {

    @Composable
    fun Content(navHostController: NavHostController) {
        val viewModel = hiltViewModel<ExportViewModel>()
        setupNavigation(navHostController = navHostController, vm = viewModel)
        ExportScreen(viewModel)
    }

    fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<ExportRoute> {
            it.toRoute<ExportRoute>().Content(navHostController = navController)
        }
    }
}
