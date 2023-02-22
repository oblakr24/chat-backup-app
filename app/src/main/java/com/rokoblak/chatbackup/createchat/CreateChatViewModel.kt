package com.rokoblak.chatbackup.createchat

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.commonui.ContactDisplayData
import com.rokoblak.chatbackup.conversation.ConversationRoute
import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.navigation.RouteNavigator
import com.rokoblak.chatbackup.services.ContactsRepository
import com.rokoblak.chatbackup.services.ConversationUIMapper
import com.rokoblak.chatbackup.services.OperationResult
import com.rokoblak.chatbackup.util.SingleEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class CreateChatViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val contactsRepo: ContactsRepository,
    private val mapper: ConversationUIMapper,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val effects = SingleEventFlow<CreateChatEffect>()

    private val searchQuery = MutableStateFlow("")

    private val searchQueriesDebounced = searchQuery.debounce { q ->
        if (q.isBlank()) 0L else 100L
    }

    val uiState: StateFlow<CreateChatUIState> by lazy {
        scope.launchMolecule(clock = RecompositionClock.ContextClock) {
            CreateChatPresenter(contactsRepo.contactsFlow, searchQueriesDebounced)
        }
    }

    val queries = searchQuery.asStateFlow()

    @SuppressLint("ComposableNaming")
    @Composable
    private fun CreateChatPresenter(
        contactsFlow: Flow<OperationResult<List<Contact>>?>,
        queriesFlow: Flow<String>,
    ): CreateChatUIState {

        val contacts = when (val loadResult = contactsFlow.collectAsState(null).value) {
            null -> return CreateChatUIState(CreateChatUIState.Content.Loading)
            is OperationResult.Error -> return CreateChatUIState(CreateChatUIState.Content.Empty("Error loading: ${loadResult.msg}"))
            is OperationResult.Done -> loadResult.data
        }

        val query = queriesFlow.collectAsState(initial = "").value
        val filtered = contactsRepo.filter(contacts, query)
        val items = filtered.groupBy { c ->
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

        val content =
            CreateChatUIState.Content.Contacts(ContactsListingData(items.toImmutableList()))
        return CreateChatUIState(content)
    }

    fun handleAction(act: CreateChatAction) {
        when (act) {
            is CreateChatAction.ContactClicked -> {
                val input = ConversationRoute.Input(resolvedContactId = act.contactId, act.number)
                navigateToRoute(ConversationRoute.get(input))
            }
            is CreateChatAction.QueryChanged -> searchQuery.update { act.query }
        }
    }
}