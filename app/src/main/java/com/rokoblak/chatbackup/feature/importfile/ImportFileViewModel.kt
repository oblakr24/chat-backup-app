package com.rokoblak.chatbackup.feature.importfile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.domain.usecases.ConversationsImportUseCase
import com.rokoblak.chatbackup.domain.usecases.DownloadConversationUseCase
import com.rokoblak.chatbackup.domain.usecases.ImportDownloadState
import com.rokoblak.chatbackup.domain.usecases.ImportError
import com.rokoblak.chatbackup.domain.usecases.ImportResult
import com.rokoblak.chatbackup.domain.usecases.PermissionsStateUseCase
import com.rokoblak.chatbackup.feature.conversation.ConversationRoute
import com.rokoblak.chatbackup.feature.importfile.ImportAction.ClearSelection
import com.rokoblak.chatbackup.feature.importfile.ImportAction.CloseEditClicked
import com.rokoblak.chatbackup.feature.importfile.ImportAction.ConversationChecked
import com.rokoblak.chatbackup.feature.importfile.ImportAction.ConversationClicked
import com.rokoblak.chatbackup.feature.importfile.ImportAction.DeleteClicked
import com.rokoblak.chatbackup.feature.importfile.ImportAction.DownloadConfirmed
import com.rokoblak.chatbackup.feature.importfile.ImportAction.EditClicked
import com.rokoblak.chatbackup.feature.importfile.ImportAction.ImportJSONClicked
import com.rokoblak.chatbackup.feature.importfile.ImportAction.JSONFileSelected
import com.rokoblak.chatbackup.feature.importfile.ImportAction.OpenSetAsDefaultClicked
import com.rokoblak.chatbackup.feature.importfile.ImportAction.SelectAll
import com.rokoblak.chatbackup.feature.importfile.ImportAction.SetAsDefaultUpdated
import com.rokoblak.chatbackup.ui.mapper.ConversationUIMapper
import com.rokoblak.chatbackup.ui.navigation.RouteNavigator
import com.rokoblak.chatbackup.util.SingleEventFlow
import com.rokoblak.chatbackup.util.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportFileViewModel @Inject constructor(
    routeNavigator: RouteNavigator,
    private val permissionsUseCase: PermissionsStateUseCase,
    private val importer: ConversationsImportUseCase,
    private val downloadUseCase: DownloadConversationUseCase,
    private val uiMapper: ConversationUIMapper,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val effects = SingleEventFlow<ImportEffect>()

    private val loading = MutableStateFlow(false)
    private val editState = MutableStateFlow(EditState())
    private val isDefaultSMSApp = permissionsUseCase.permissions.map { it.isDefaultSMSHandlerApp }

    val uiState: StateFlow<ImportScreenUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.ContextClock) {
            ImportPresenter(
                downloadUseCase.state,
                editState,
                isDefaultSMSApp,
                loading,
            )
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun ImportPresenter(
        importStateFlow: Flow<ImportDownloadState>,
        editStateFlow: StateFlow<EditState>,
        isDefaultSMSAppFlow: Flow<Boolean>,
        loadingFlow: StateFlow<Boolean>,
    ): ImportScreenUIState {
        val isLoading = loadingFlow.collectAsState().value
        if (isLoading) return ImportScreenUIState.Loading
        val importState = importStateFlow.collectAsState(initial = null).value
        val importResult = importState?.importResult ?: return ImportScreenUIState.Initial

        val res = when (importResult) {
            is OperationResult.Done -> importResult.data
            is OperationResult.Error -> {
                effects.send(ImportEffect.ShowToast("Error importing: $importResult"))
                return ImportScreenUIState.Initial
            }
        }

        val selections = importState.selections
        val editState = editStateFlow.collectAsState().value
        val isDefaultSMSApp = isDefaultSMSAppFlow.collectAsState(false).value
        val downloadProgress = importState.progress
        val mappedItems = uiMapper.mapToUI(res.convs, selections.takeIf { editState.editing })

        val hasAnySelections = selections.any { it.value }
        val toolbar = ImportTopToolbarUIState(
            showEdit = editState.editing.not(),
            downloadShowsPrompt = isDefaultSMSApp.not(),
            downloadEnabled = hasAnySelections,
            deleteEnabled = hasAnySelections,
        )
        val selectedContactIds = selections.filter { it.value }.keys
        val selectedMsgs = res.convs.retrieveMessages(selectedContactIds)
        val subtitle = downloadProgress?.let {
            "${it.done}/${it.total} downloaded"
        } ?: "${selectedContactIds.size} selected (${selectedMsgs.size} total messages)"
        return ImportScreenUIState.Loaded(
            title = StringUtils.coerceFilename(res.filename),
            toolbar = toolbar,
            listing = mappedItems.toImmutableList(),
            subtitle = subtitle,
            showLoading = downloadProgress != null,
        )
    }

    fun handleAction(act: ImportAction) {
        when (act) {
            is JSONFileSelected -> importJSONFile(act.uri)
            ImportJSONClicked -> openJSONFilePicker()
            is ConversationClicked -> openConversation(act.contactId, act.number)
            ClearSelection -> downloadUseCase.clearSelections()
            CloseEditClicked -> editState.update { it.copy(editing = false) }
            EditClicked -> editState.update { it.copy(editing = true) }
            DeleteClicked -> downloadUseCase.deleteSelected()
            DownloadConfirmed -> downloadSelected()
            is OpenSetAsDefaultClicked -> {
                effects.send(ImportEffect.ShowSetAsDefaultPrompt(act.owner))
            }
            SelectAll -> downloadUseCase.selectAll()
            SetAsDefaultUpdated -> permissionsUseCase.updatePermissions()
            is ConversationChecked -> downloadUseCase.updateCheckedState(act.contactId, act.checked)
        }
    }

    private fun downloadSelected() = viewModelScope.launch {
        downloadUseCase.downloadSelected { msg ->
            effects.send(ImportEffect.ShowToast(msg))
        }
    }

    private fun importJSONFile(uri: Uri) {
        importFile {
            importer.importJson(uri)
        }
    }

    private fun importFile(doImport: suspend () -> OperationResult<ImportResult, ImportError>) =
        viewModelScope.launch {
            loading.value = true
            downloadUseCase.importFile(doImport)
            loading.value = false
        }

    private fun openJSONFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        effects.send(ImportEffect.OpenJSONFilePicker(intent))
    }

    private fun openConversation(contactId: String, number: String) {
        val input = ConversationRoute(contactId, address = number, isImport = true)
        navigateToRoute(input)
    }
}

private data class EditState(
    val editing: Boolean = false,
)