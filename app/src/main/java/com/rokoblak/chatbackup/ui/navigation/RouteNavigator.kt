package com.rokoblak.chatbackup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AppRoute

interface RouteNavigator {
    fun onNavigated(state: NavigationState)
    fun navigateUp()
    fun popToRoute(route: AppRoute)
    fun navigateToRoute(route: AppRoute)

    val navigationState: StateFlow<NavigationState>
}

private fun updateNavigationState(
    navHostController: NavHostController,
    navigationState: NavigationState,
    onNavigated: (navState: NavigationState) -> Unit,
) {
    when (navigationState) {
        is NavigationState.NavigateToRoute -> {
            navHostController.navigate(navigationState.route)
            onNavigated(navigationState)
        }
        is NavigationState.PopToRoute -> {
            navHostController.popBackStack(navigationState.staticRoute, false)
            onNavigated(navigationState)
        }
        is NavigationState.NavigateUp -> {
            navHostController.navigateUp()
        }
        is NavigationState.Idle -> Unit
    }
}

@Composable
fun AppRoute.setupNavigation(navHostController: NavHostController, vm: RouteNavigator) {
    val viewStateAsState by vm.navigationState.collectAsState()

    LaunchedEffect(viewStateAsState) {
        updateNavigationState(navHostController, viewStateAsState, vm::onNavigated)
    }
}

class AppRouteNavigator : RouteNavigator {

    override val navigationState: MutableStateFlow<NavigationState> =
        MutableStateFlow(NavigationState.Idle)

    override fun onNavigated(state: NavigationState) {
        navigationState.compareAndSet(state, NavigationState.Idle)
    }

    override fun popToRoute(route: AppRoute) = navigate(NavigationState.PopToRoute(route))

    override fun navigateUp() = navigate(NavigationState.NavigateUp())

    override fun navigateToRoute(route: AppRoute) = navigate(NavigationState.NavigateToRoute(route))

    private fun navigate(state: NavigationState) {
        navigationState.value = state
    }
}