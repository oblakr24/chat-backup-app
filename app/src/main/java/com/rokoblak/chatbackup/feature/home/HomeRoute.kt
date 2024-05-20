package com.rokoblak.chatbackup.feature.home

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
data object HomeRoute : AppRoute {

    @Composable
    fun Content(navHostController: NavHostController) {
        val viewModel = hiltViewModel<HomeViewModel>()
        setupNavigation(navHostController = navHostController, vm = viewModel)
        HomeScreen(viewModel)
    }

    fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<HomeRoute> {
            it.toRoute<HomeRoute>().Content(navHostController = navController)
        }
    }
}
