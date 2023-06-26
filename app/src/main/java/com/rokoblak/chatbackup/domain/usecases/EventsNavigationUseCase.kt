package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.conversation.ConversationRoute
import com.rokoblak.chatbackup.createchat.CreateChatRoute
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.di.SMSEvent
import com.rokoblak.chatbackup.navigation.RouteNavigator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class EventsNavigationUseCase @Inject constructor(
    private val appScope: AppScope,
    private val routeNavigator: RouteNavigator,
) {

    suspend fun handleEventsNavigation() = appScope.smsEvents.onEach { event ->
        when (event) {
            SMSEvent.NewReceived -> Unit
            is SMSEvent.OpenCreateChat -> {
                appScope.markEventConsumed()
                if (event.address != null) {
                    val input = ConversationRoute.Input(
                        resolvedContactId = null,
                        address = event.address,
                        isImport = false
                    )
                    routeNavigator.navigateToRoute(ConversationRoute.get(input))
                } else {
                    routeNavigator.navigateToRoute(CreateChatRoute.route)
                }
            }
        }
    }.collect()

}