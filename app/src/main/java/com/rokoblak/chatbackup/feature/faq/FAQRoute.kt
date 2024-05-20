package com.rokoblak.chatbackup.feature.faq

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
data object FAQRoute : AppRoute {

    @Composable
    fun Content(navHostController: NavHostController) {
        val viewModel = hiltViewModel<FAQViewModel>()
        setupNavigation(navHostController = navHostController, vm = viewModel)
        FAQScreen(viewModel)
    }

    fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<FAQRoute> {
            it.toRoute<FAQRoute>().Content(navHostController = navController)
        }
    }
}