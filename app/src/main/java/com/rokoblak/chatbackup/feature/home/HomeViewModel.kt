package com.rokoblak.chatbackup.feature.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.AppConstants
import com.rokoblak.chatbackup.BuildConfig
import com.rokoblak.chatbackup.data.datasources.MessageDeletionError
import com.rokoblak.chatbackup.feature.conversation.ConversationRoute
import com.rokoblak.chatbackup.feature.createchat.CreateChatRoute
import com.rokoblak.chatbackup.data.model.Conversations
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.domain.usecases.ConversationsSearchUseCase
import com.rokoblak.chatbackup.domain.usecases.ConvsState
import com.rokoblak.chatbackup.domain.usecases.DarkModeToggleUseCase
import com.rokoblak.chatbackup.domain.usecases.EventsNavigationUseCase
import com.rokoblak.chatbackup.domain.usecases.PermissionsState
import com.rokoblak.chatbackup.domain.usecases.PermissionsStateUseCase
import com.rokoblak.chatbackup.domain.usecases.SearchState
import com.rokoblak.chatbackup.feature.export.ExportRoute
import com.rokoblak.chatbackup.feature.faq.FAQRoute
import com.rokoblak.chatbackup.feature.home.HomeAction.ClearSelection
import com.rokoblak.chatbackup.feature.home.HomeAction.CloseEditClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.ComposeClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.ConversationChecked
import com.rokoblak.chatbackup.feature.home.HomeAction.ConversationClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.DeleteClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.EditClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.ExportClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.FAQClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.ImportClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.OpenRepoUrl
import com.rokoblak.chatbackup.feature.home.HomeAction.OpenSetAsDefaultClicked
import com.rokoblak.chatbackup.feature.home.HomeAction.PermissionsUpdated
import com.rokoblak.chatbackup.feature.home.HomeAction.QueryChanged
import com.rokoblak.chatbackup.feature.home.HomeAction.SelectAll
import com.rokoblak.chatbackup.feature.home.HomeAction.SetAsDefaultUpdated
import com.rokoblak.chatbackup.feature.home.HomeAction.SetDarkMode
import com.rokoblak.chatbackup.feature.importfile.ImportRoute
import com.rokoblak.chatbackup.ui.commonui.ConversationsListingUIState.Empty
import com.rokoblak.chatbackup.ui.commonui.ConversationsListingUIState.Loaded
import com.rokoblak.chatbackup.ui.commonui.ConversationsListingUIState.Loading
import com.rokoblak.chatbackup.ui.mapper.ConversationUIMapper
import com.rokoblak.chatbackup.ui.navigation.RouteNavigator
import com.rokoblak.chatbackup.util.SingleEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventsNavigationUseCase: EventsNavigationUseCase,
    private val permissionsStateUseCase: PermissionsStateUseCase,
    private val convsUseCase: ConversationsSearchUseCase,
    private val darkModeToggleUseCase: DarkModeToggleUseCase,
    routeNavigator: RouteNavigator,
    private val uiMapper: ConversationUIMapper,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    init {
        viewModelScope.launch { eventsNavigationUseCase.handleEventsNavigation() }
    }

    val effects = SingleEventFlow<HomeEffects>()

    @SuppressLint("ComposableNaming")
    @Composable
    private fun HomePresenter(
        convsFlow: StateFlow<ConvsState>,
        darkModeFlow: Flow<Boolean?>,
        permissions: StateFlow<PermissionsState>,
        searchStatesFlow: Flow<SearchState>,
    ): HomeScreenUIState {
        val darkMode = darkModeFlow.collectAsState(initial = null).value
        val perms = permissions.collectAsState().value

        var editing = false
        var convs: Conversations? = null
        var selections: Map<String, Boolean> = emptyMap()
        val conversationsState = if (perms.hasPermissions) {
            when (val state = convsFlow.collectAsState().value) {
                ConvsState.Empty -> Empty
                ConvsState.Loading -> Loading
                is ConvsState.Loaded -> {
                    convs = state.convs
                    selections = state.selections ?: emptyMap()
                    editing = state.editing
                    val searchState = searchStatesFlow
                        .collectAsState(initial = SearchState.NoSearch).value
                    when (searchState) {
                        SearchState.NoSearch -> Loaded(uiMapper.mapToUI(state.convs, state.selections))
                        SearchState.Searching -> Loaded(uiMapper.mapToUI(state.convs, state.selections), "Searching...")
                        SearchState.NoResults -> Loaded(persistentListOf(), "No results")
                        is SearchState.ResultsFound -> Loaded(uiMapper.mapToUI(state.convs, state.selections, searchState.results))
                    }
                }
            }
        } else {
            Loading
        }

        val selectionsEmpty = selections.filter { it.value }.isEmpty()
        val exportEnabled = selectionsEmpty.not()
        val appbarState = HomeAppbarUIState(
            hideIcons = perms.hasPermissions.not() || convs?.mapping.isNullOrEmpty(),
            showDelete = editing,
            showEdit = editing.not(),
            deleteEnabled = editing && selectionsEmpty.not(),
            deleteShowsPrompt = perms.isDefaultSMSHandlerApp.not(),
        )
        val contentState = if (perms.isDefaultSMSHandlerApp.not()) {
            HomeScreenUIState.InnerContent.NotDefaultSMSHandlerApp
        } else {
            HomeScreenUIState.InnerContent.Content(
                HomeContentUIState(
                    state = conversationsState,
                    title = uiMapper.title(convs),
                    subtitle = uiMapper.subtitle(convs, editing, selections),
                    exportEnabled = exportEnabled,
                )
            )
        }
        return HomeScreenUIState(appbarState, mapToDrawerUI(perms, darkMode), contentState)
    }

    val uiState: StateFlow<HomeScreenUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.ContextClock) {
            HomePresenter(
                convsUseCase.mappedConvs,
                darkModeToggleUseCase.darkModeEnabled(),
                permissionsStateUseCase.permissions,
                convsUseCase.searchStates,
            )
        }
    }

    val queries = convsUseCase.searchQuery

    fun handleAction(act: HomeAction) {
        when (act) {
            is ConversationChecked -> updateCheckedState(act.contactId, act.checked)
            is ConversationClicked -> openConversation(act.contactId, act.number)
            is ImportClicked -> navigateToRoute(ImportRoute)
            is ComposeClicked -> navigateToRoute(CreateChatRoute)
            is ExportClicked -> navigateToExport()
            FAQClicked -> navigateToRoute(FAQRoute)
            is SetDarkMode -> setDarkMode(act.enabled)
            CloseEditClicked -> convsUseCase.exitEdit()
            EditClicked -> enterEdit()
            PermissionsUpdated -> permissionsStateUseCase.updatePermissions()
            is OpenSetAsDefaultClicked -> effects.send(HomeEffects.ShowSetAsDefaultPrompt)
            SetAsDefaultUpdated -> permissionsStateUseCase.updatePermissions()
            DeleteClicked -> deleteSelected()
            ClearSelection -> convsUseCase.clearSelections()
            SelectAll -> convsUseCase.selectAll()
            OpenRepoUrl -> effects.send(HomeEffects.OpenIntent(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(AppConstants.REPO_URL)
            }))
            is QueryChanged -> convsUseCase.queryChanged(act.query)
        }
    }

    private fun openConversation(contactId: String, number: String) {
        navigateToRoute(
            ConversationRoute(
            resolvedContactId = contactId,
            address = number,
            isImport = false
        ))
    }

    private fun enterEdit() {
        effects.send(HomeEffects.HideKeyboard)
        convsUseCase.enterEdit()
    }

    private fun deleteSelected() = viewModelScope.launch {
        val deleteRes = convsUseCase.deleteSelected()
        if (deleteRes is OperationResult.Error) {
            val errorMsg = when (val e = deleteRes.error) {
                is MessageDeletionError.DeletionError -> "Deleted ${e.deleted} out of ${e.totalToDelete}"
                is MessageDeletionError.GenericDeletionError -> e.message
            }
            effects.send(HomeEffects.ShowToast(errorMsg))
        }
    }

    private fun navigateToExport() {
        convsUseCase.setExportedSelections()
        navigateToRoute(ExportRoute)
    }

    private fun updateCheckedState(contactId: String, checked: Boolean) {
        convsUseCase.updateCheckedState(contactId, checked)
    }

    private fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        darkModeToggleUseCase.setDarkMode(enabled)
    }

    private fun mapToDrawerUI(perms: PermissionsState, darkMode: Boolean?) = HomeDrawerUIState(
        darkMode = darkMode,
        showDefaultSMSLabel = perms.isDefaultSMSHandlerApp,
        versionLabel = "Version ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})",
        showComposeAndImport = perms.hasPermissions
    )

    override fun onCleared() {
        super.onCleared()
        convsUseCase.dispose()
    }
}
