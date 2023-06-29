package com.rokoblak.chatbackup.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEventsUseCase @Inject constructor() {

    private val _smsEvents = MutableSharedFlow<SMSEvent?>(replay = 1)
    val smsEvents: Flow<SMSEvent> = _smsEvents.filterNotNull()

    fun onNewEvent(event: SMSEvent) {
        _smsEvents.tryEmit(event)
    }

    fun markEventConsumed() {
        _smsEvents.tryEmit(null)
    }
}

sealed interface SMSEvent {
    object NewReceived: SMSEvent
    data class OpenCreateChat(val address: String?): SMSEvent
}