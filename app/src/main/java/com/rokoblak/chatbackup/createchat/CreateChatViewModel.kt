package com.rokoblak.chatbackup.createchat

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.conversation.ConversationRoute
import com.rokoblak.chatbackup.createchat.CreateChatUIState.Content
import com.rokoblak.chatbackup.data.model.Contact
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.domain.usecases.ContactsFilteringUseCase
import com.rokoblak.chatbackup.ui.commonui.ContactDisplayData
import com.rokoblak.chatbackup.ui.mapper.ConversationUIMapper
import com.rokoblak.chatbackup.ui.navigation.RouteNavigator
import com.rokoblak.chatbackup.util.SingleEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class CreateChatViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val contactsFilteringUseCase: ContactsFilteringUseCase,
    private val mapper: ConversationUIMapper,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val effects = SingleEventFlow<CreateChatEffect>()

    val uiState: StateFlow<CreateChatUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.ContextClock) {
            CreateChatPresenter(contactsFilteringUseCase.filteredContacts())
        }
    }

    val queries = contactsFilteringUseCase.searchQuery

    @SuppressLint("ComposableNaming")
    @Composable
    private fun CreateChatPresenter(
        contactsFlow: Flow<OperationResult<List<Contact>>?>,
    ): CreateChatUIState {
        return when (val loadResult = contactsFlow.collectAsState(null).value) {
            null -> return CreateChatUIState(Content.Loading)
            is OperationResult.Error -> return CreateChatUIState(Content.Empty("Error loading: ${loadResult.msg}"))
            is OperationResult.Done -> {
                val items = loadResult.data.groupBy { c ->
                    c.displayName.firstOrNull()?.takeIf { it.isLetter() }?.titlecase() ?: "*"
                }.map { (section, contacts) ->
                    section to contacts.sortedBy { it.displayName }.map {
                        ContactDisplayData(
                            id = it.id,
                            avatar = mapper.mapAvatar(it),
                            title = it.displayName,
                            subtitle = it.number,
                            type = it.phoneType.displayName(),
                            number = it.number,
                        )
                    }.toImmutableList()
                }.sortedBy { it.first }
                val content = Content.Contacts(ContactsListingData(items.toImmutableList()))
                CreateChatUIState(content)
            }
        }
    }

    fun handleAction(act: CreateChatAction) {
        when (act) {
            is CreateChatAction.ContactClicked -> {
                val input = ConversationRoute.Input(resolvedContactId = act.contactId, act.number)
                navigateToRoute(ConversationRoute.get(input))
            }

            is CreateChatAction.QueryChanged -> contactsFilteringUseCase.updateQuery(act.query)
        }
    }
}