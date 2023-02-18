package com.rokoblak.chatbackup.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.AppConstants
import com.rokoblak.chatbackup.BuildConfig
import com.rokoblak.chatbackup.commonui.ConversationsListingUIState
import com.rokoblak.chatbackup.conversation.ConversationRoute
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.di.AppStorage
import com.rokoblak.chatbackup.export.ExportRoute
import com.rokoblak.chatbackup.faq.FAQRoute
import com.rokoblak.chatbackup.importfile.ImportRoute
import com.rokoblak.chatbackup.navigation.RouteNavigator
import com.rokoblak.chatbackup.services.*
import com.rokoblak.chatbackup.util.SingleEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appScope: AppScope,
    private val conversationsRepo: ConversationsRepo,
    private val routeNavigator: RouteNavigator,
    private val uiMapper: ConversationUIMapper,
    private val searcher: ConversationSearcher,
    private val storage: AppStorage,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val effects = SingleEventFlow<HomeEffects>()

    private val permissions = MutableStateFlow(
        PermissionsState(
            hasPermissions = appScope.hasMessagesPermissions(),
            isDefaultSMSHandlerApp = appScope.isDefaultSMSApp()
        )
    )
    private val searchQuery = MutableStateFlow("")
    private val editState = MutableStateFlow(EditState())
    private val selections = MutableStateFlow(emptyMap<String, Boolean>())

    private val conversations = conversationsRepo.deviceConvsFlow.onEach { conv ->
        conv?.let {
            editState.update { it.copy(editing = false) }
            selections.update { conv.mapping.map { it.key.id to true }.toMap() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val searchStates: Flow<SearchState> = searchQuery.debounce(100L).flatMapLatest { q ->
        if (q.isBlank()) return@flatMapLatest flowOf(SearchState.NoSearch)
        val convs = conversations.value ?: return@flatMapLatest flowOf(SearchState.NoSearch)
        flow<SearchState> {
            emit(SearchState.Searching)
            val res = searcher.searchForQuery(convs, q)
            emit(res?.let { SearchState.ResultsFound(it) } ?: SearchState.NoResults)
        }
    }

    private val mappedConvs =
        combine(conversations, selections, editState) { convs, sel, editState ->
            if (convs == null) {
                ConversationsListingUIState.Loading
            } else {
                if (convs.mapping.isEmpty()) {
                    ConversationsListingUIState.Empty
                } else {
                    val mapped = uiMapper.mapToUI(convs, sel.takeIf { editState.editing })
                    ConversationsListingUIState.Loaded(mapped.toImmutableList())
                }
            }
        }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun HomePresenter(
        mappedConvsFlow: Flow<ConversationsListingUIState>,
        conversationsFlow: Flow<Conversations?>,
        selectionsFlow: StateFlow<Map<String, Boolean>>,
        settingsFlow: Flow<AppStorage.Prefs>,
        editStateFlow: StateFlow<EditState>,
        permissions: StateFlow<PermissionsState>,
        searchStatesFlow: Flow<SearchState>,
    ): HomeScreenUIState {
        val settings = settingsFlow.collectAsState(initial = AppStorage.defaultSettings).value
        val editState = editStateFlow.collectAsState().value
        val selections = selectionsFlow.collectAsState().value
        val selectionsIfEditing = selections.takeIf { editState.editing }
        val perms = permissions.collectAsState().value

        val drawerState = HomeDrawerUIState(
            darkMode = settings.darkMode,
            showDefaultSMSLabel = perms.isDefaultSMSHandlerApp,
            versionLabel = "Version ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
        )

        val conversations = if (perms.hasPermissions) {
            conversationsFlow.collectAsState(initial = null).value
        } else {
            null
        }

        val conversationsState = if (conversations != null) {
            val searchState = searchStatesFlow
                .collectAsState(initial = SearchState.NoSearch).value
            when (searchState) {
                SearchState.NoSearch -> {
                    mappedConvsFlow.collectAsState(initial = ConversationsListingUIState.Loading).value
                }
                SearchState.Searching -> {
                    val current =
                        mappedConvsFlow.collectAsState(initial = ConversationsListingUIState.Loading).value
                    val currItems = (current as? ConversationsListingUIState.Loaded)?.items
                    ConversationsListingUIState.Loaded(
                        currItems ?: persistentListOf(),
                        "Searching..."
                    )
                }
                SearchState.NoResults -> {
                    ConversationsListingUIState.Loaded(persistentListOf(), "No results")
                }
                is SearchState.ResultsFound -> {
                    val mapped =
                        uiMapper.mapToUI(conversations, selectionsIfEditing, searchState.results)
                    ConversationsListingUIState.Loaded(mapped)
                }
            }
        } else {
            ConversationsListingUIState.Loading
        }

        val selectionsEmpty = selections.filter { it.value }.isEmpty()
        val deleteEnabled = editState.editing && selectionsEmpty.not()
        val exportEnabled = selectionsEmpty.not()
        val convsAreEmpty = conversations?.mapping.isNullOrEmpty()
        val appbarState = HomeAppbarUIState(
            hideIcons = perms.hasPermissions.not() || convsAreEmpty,
            showDelete = editState.editing,
            showEdit = editState.editing.not(),
            deleteEnabled = deleteEnabled,
            deleteShowsPrompt = perms.isDefaultSMSHandlerApp.not(),
        )

        val contentState = if (perms.isDefaultSMSHandlerApp.not()) {
            HomeScreenUIState.InnerContent.NotDefaultSMSHandlerApp
        } else {
            val title = if (conversations != null) {
                "${conversations.totalChats} conversations, ${conversations.totalMessages} messages"
            } else {
                ""
            }
            val subtitle = if (conversations != null && editState.editing) {
                val selectedCount = selections.values.count { it }
                "$selectedCount selected"
            } else {
                ""
            }
            HomeScreenUIState.InnerContent.Content(
                HomeContentUIState(
                    state = conversationsState,
                    title = title,
                    subtitle = subtitle,
                    exportEnabled = exportEnabled,
                )
            )
        }

        return HomeScreenUIState(appbarState, drawerState, contentState)
    }

    val uiState: StateFlow<HomeScreenUIState> by lazy {
        scope.launchMolecule(clock = RecompositionClock.ContextClock) {
            HomePresenter(
                mappedConvs,
                conversations,
                selections,
                storage.prefsFlow(),
                editState,
                permissions,
                searchStates,
            )
        }
    }

    val queries = searchQuery.asStateFlow()

    fun handleAction(act: HomeAction) {
        when (act) {
            is HomeAction.ConversationChecked -> updateCheckedState(act.contactId, act.checked)
            is HomeAction.ConversationClicked -> navigateToRoute(ConversationRoute.get(act.contactId))
            is HomeAction.ImportClicked -> navigateToRoute(ImportRoute.route)
            is HomeAction.ExportClicked -> navigateToExport()
            HomeAction.FAQClicked -> navigateToRoute(FAQRoute.route)
            is HomeAction.SetDarkMode -> setDarkMode(act.enabled)
            HomeAction.CloseEditClicked -> editState.update { it.copy(editing = false) }
            HomeAction.EditClicked -> enterEdit()
            HomeAction.PermissionsUpdated -> updatePermissions()
            is HomeAction.OpenSetAsDefaultClicked -> effects.send(HomeEffects.ShowSetAsDefaultPrompt)
            HomeAction.SetAsDefaultUpdated -> updatePermissions()
            HomeAction.DeleteClicked -> deleteSelected()
            HomeAction.ClearSelection -> clearSelections()
            HomeAction.SelectAll -> selectAll()
            HomeAction.OpenRepoUrl -> effects.send(HomeEffects.OpenIntent(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(AppConstants.REPO_URL)
            }))
            is HomeAction.QueryChanged -> searchQuery.value = act.query
        }
    }

    private fun updatePermissions() {
        permissions.value = PermissionsState(
            hasPermissions = appScope.hasMessagesPermissions(),
            isDefaultSMSHandlerApp = appScope.isDefaultSMSApp()
        )
    }

    private fun enterEdit() {
        effects.send(HomeEffects.HideKeyboard)
        editState.update { it.copy(editing = true) }
    }

    private fun clearSelections() {
        selections.update {
            it.toMutableMap().mapValues { false }.toMap()
        }
    }

    private fun selectAll() {
        selections.update {
            it.toMutableMap().mapValues { true }.toMap()
        }
    }

    private fun deleteSelected() = viewModelScope.launch {
        val selectedIds = selections.value.filter { it.value }.keys
        val deleteRes = conversationsRepo.deleteDeviceConvs(selectedIds)
        if (deleteRes is OperationResult.Error) {
            effects.send(HomeEffects.ShowToast(deleteRes.msg))
        }
    }

    private fun navigateToExport() {
        conversationsRepo.setExportedSelections(selections.value)
        navigateToRoute(ExportRoute.route)
    }

    private fun updateCheckedState(contactId: String, checked: Boolean) {
        selections.update {
            it.toMutableMap().apply {
                put(contactId, checked)
            }.toMap()
        }
    }

    private fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        storage.updateDarkMode(enabled)
    }
}

private data class EditState(
    val editing: Boolean = false,
)

sealed interface SearchState {
    object NoSearch : SearchState
    object Searching : SearchState
    object NoResults : SearchState
    data class ResultsFound(val results: SearchResults) : SearchState
}

data class PermissionsState(
    val hasPermissions: Boolean,
    val isDefaultSMSHandlerApp: Boolean,
)