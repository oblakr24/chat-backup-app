package com.rokoblak.chatbackup.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokoblak.chatbackup.commonui.ChatDisplayData
import com.rokoblak.chatbackup.data.Conversation
import com.rokoblak.chatbackup.navigation.RouteNavigator
import com.rokoblak.chatbackup.services.ConversationUIMapper
import com.rokoblak.chatbackup.services.ConversationsRepo
import com.rokoblak.chatbackup.util.formatDateOnly
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routeNavigator: RouteNavigator,
    private val conversationsRepo: ConversationsRepo,
    private val uiMapper: ConversationUIMapper,
) :
    ViewModel(), RouteNavigator by routeNavigator {

    private val contactId = ConversationRoute.getIdFrom(savedStateHandle)

    val uiState = flow {
        emit(conversationsRepo.retrieveConversation(contactId))
    }.map {
        it.mapToUI()
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ConversationScreenUIState(
            emptyList<ChatDisplayData>().toImmutableList()
        )
    )

    private fun Conversation?.mapToUI(): ConversationScreenUIState {
        val items = if (this == null) {
            emptyList()
        } else {
            messages.map { uiMapper.mapMessageToUI(it, contact) }
        }
        val msgs = this?.messages ?: emptyList()
        val msgsCount = this?.messages?.size ?: 0
        val msgInfo = if (msgsCount > 1) {
            val last = msgs.last().timestamp.formatDateOnly()
            val first = msgs.first().timestamp.formatDateOnly()
            if (last != first) {
                " ($first to $last)"
            } else {
                ""
            }
        } else {
            ""
        }
        return ConversationScreenUIState(
            items.toImmutableList(),
            title = this?.let { "Conversation with ${contact.displayName}" }.orEmpty(),
            subtitle = this?.messages?.size?.let { "$it messages $msgInfo" }.orEmpty()
        )
    }
}