package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.data.model.Conversations
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.util.ConversationSearcher
import com.rokoblak.chatbackup.data.repo.ConversationsRepository
import com.rokoblak.chatbackup.data.util.SearchResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ConversationsSearchUseCase @Inject constructor(
    private val conversationsRepo: ConversationsRepository,
    private val searcher: ConversationSearcher,
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _editState = MutableStateFlow(EditState())
    private val _selections = MutableStateFlow(emptyMap<String, Boolean>())

    private val conversations = conversationsRepo.deviceConvsFlow.onEach { conv ->
        conv?.let {
            _editState.update { it.copy(editing = false) }
            _selections.update { conv.mapping.map { it.key.id to true }.toMap() }
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val searchStates: Flow<SearchState> = searchQuery.debounce(100L).flatMapLatest { q ->
        if (q.isBlank()) return@flatMapLatest flowOf(SearchState.NoSearch)
        val convs = conversations.value ?: return@flatMapLatest flowOf(SearchState.NoSearch)
        flow<SearchState> {
            emit(SearchState.Searching)
            val res = searcher.searchForQuery(convs, q)
            emit(res?.let { SearchState.ResultsFound(it) } ?: SearchState.NoResults)
        }
    }

    val mappedConvs =
        combine(conversations, _selections, _editState) { convs, sel, editState ->
            when {
                convs == null -> ConvsState.Loading
                convs.mapping.isEmpty() -> ConvsState.Empty
                else -> ConvsState.Loaded(convs, sel.takeIf { editState.editing }, editing = editState.editing)
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), ConvsState.Loading)

    fun updateCheckedState(contactId: String, checked: Boolean) {
        _selections.update {
            it.toMutableMap().apply {
                put(contactId, checked)
            }.toMap()
        }
    }

    fun queryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSelections() {
        _selections.update {
            it.toMutableMap().mapValues { false }.toMap()
        }
    }

    fun selectAll() {
        _selections.update {
            it.toMutableMap().mapValues { true }.toMap()
        }
    }

    fun setExportedSelections() {
        conversationsRepo.setExportedSelections(_selections.value)
    }

    fun enterEdit() {
        _editState.update { it.copy(editing = true) }
    }

    fun exitEdit() {
        _editState.update { it.copy(editing = false) }
    }

    suspend fun deleteSelected(): OperationResult<Unit> {
        val selectedIds = _selections.value.filter { it.value }.keys
        return conversationsRepo.deleteDeviceConvs(selectedIds)
    }

    fun dispose() {
        scope.cancel()
    }
}

data class EditState(
    val editing: Boolean = false,
)

sealed interface ConvsState {
    object Empty : ConvsState
    object Loading : ConvsState
    data class Loaded(
        val convs: Conversations,
        val selections: Map<String, Boolean>?,
        val editing: Boolean,
    ) : ConvsState
}

sealed interface SearchState {
    object NoSearch : SearchState
    object Searching : SearchState
    object NoResults : SearchState
    data class ResultsFound(val results: SearchResults) : SearchState
}