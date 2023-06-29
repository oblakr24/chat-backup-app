package com.rokoblak.chatbackup.conversation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.data.model.Conversation
import com.rokoblak.chatbackup.domain.usecases.ConversationUseCase
import com.rokoblak.chatbackup.ui.navigation.RouteNavigator
import com.rokoblak.chatbackup.ui.mapper.ConversationUIMapper
import com.rokoblak.chatbackup.domain.usecases.SMSSendUseCase
import com.rokoblak.chatbackup.util.formatDateOnly
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    routeNavigator: RouteNavigator,
    conversationUseCase: ConversationUseCase,
    private val uiMapper: ConversationUIMapper,
    private val smsSender: SMSSendUseCase,
) :
    ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    private val routeInput = ConversationRoute.getIdFrom(savedStateHandle)

    private val convsFlow = conversationUseCase.conversationFor(
        contactId = routeInput.resolvedContactId,
        number = routeInput.address,
        isImport = routeInput.isImport
    )

    private val inputs = MutableStateFlow("")

    val uiState: StateFlow<ConversationScreenUIState> by lazy {
        scope.launchMolecule(clock = RecompositionClock.ContextClock) {
            ConversationPresenter(convsFlow)
        }
    }

    val input = inputs.asStateFlow()

    @SuppressLint("ComposableNaming")
    @Composable
    private fun ConversationPresenter(
        convFlow: Flow<Conversation>,
    ): ConversationScreenUIState {
        val conv = convFlow.collectAsState(initial = null).value

        val items = conv?.messages?.map { uiMapper.mapMessageToUI(it, conv.contact) } ?: emptyList()
        val msgs = conv?.messages ?: emptyList()
        val msgsCount = conv?.messages?.size ?: 0
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
            title = conv?.let { "Conversation with ${it.contact.displayName}" }.orEmpty(),
            subtitle = conv?.messages?.size?.let { "$it messages $msgInfo" }.orEmpty(),
            showInput = routeInput.isImport.not(),
        )
    }

    fun handleAction(act: ConversationAction) {
        when (act) {
            is ConversationAction.InputChanged -> {
                inputs.value = act.input
            }
            is ConversationAction.SendClicked -> {
                viewModelScope.launch {
                    val address = routeInput.address
                    val body = inputs.value
                    smsSender.send(address = address, body = body)
                    inputs.value = ""
                }
            }
        }
    }
}