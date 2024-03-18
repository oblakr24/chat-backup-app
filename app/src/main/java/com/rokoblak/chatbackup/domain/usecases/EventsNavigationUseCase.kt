package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.feature.conversation.ConversationRoute
import com.rokoblak.chatbackup.feature.createchat.CreateChatRoute
import com.rokoblak.chatbackup.ui.navigation.RouteNavigator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class EventsNavigationUseCase @Inject constructor(
    private val eventsUseCase: AppEventsUseCase,
    private val routeNavigator: RouteNavigator,
) {

    suspend fun handleEventsNavigation() = eventsUseCase.smsEvents.onEach { event ->
        when (event) {
            SMSEvent.NewReceived -> Unit
            is SMSEvent.OpenCreateChat -> {
                eventsUseCase.markEventConsumed()
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