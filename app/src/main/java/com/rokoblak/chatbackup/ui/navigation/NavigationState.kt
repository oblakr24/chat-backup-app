package com.rokoblak.chatbackup.ui.navigation

import java.util.*

sealed class NavigationState {
    data object Idle : NavigationState()
    data class NavigateToRoute(val route: AppRoute, val id: String = UUID.randomUUID().toString()) :
        NavigationState()

    data class PopToRoute(val staticRoute: AppRoute, val id: String = UUID.randomUUID().toString()) :
        NavigationState()

    data class NavigateUp(val id: String = UUID.randomUUID().toString()) : NavigationState()
}