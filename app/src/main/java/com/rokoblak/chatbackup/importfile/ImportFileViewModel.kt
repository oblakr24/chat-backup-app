package com.rokoblak.chatbackup.importfile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.conversation.ConversationRoute
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.navigation.RouteNavigator
import com.rokoblak.chatbackup.services.*
import com.rokoblak.chatbackup.util.SingleEventFlow
import com.rokoblak.chatbackup.util.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportFileViewModel @Inject constructor(
    private val appScope: AppScope,
    private val routeNavigator: RouteNavigator,
    private val importer: ConversationsImporter,
    private val uiMapper: ConversationUIMapper,
    private val repo: ConversationsRepo,
    private val retriever: MessagesRetriever,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val effects = SingleEventFlow<ImportEffect>()

    private val loading = MutableStateFlow(false)
    private val downloading = MutableStateFlow<DownloadProgress?>(null)
    private val editState = MutableStateFlow(EditState())
    private val selections = MutableStateFlow(emptyMap<String, Boolean>())
    private val isDefaultSMSApp = MutableStateFlow(appScope.isDefaultSMSApp())
    private val importedConvs = MutableStateFlow<ImportResult?>(null)

    val uiState: StateFlow<ImportScreenUIState> by lazy {
        scope.launchMolecule(clock = RecompositionClock.ContextClock) {
            ImportPresenter(
                importedConvs,
                selections,
                editState,
                isDefaultSMSApp,
                loading,
                downloading
            )
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun ImportPresenter(
        importedConvsFlow: StateFlow<ImportResult?>,
        selectionsFlow: StateFlow<Map<String, Boolean>>,
        editStateFlow: StateFlow<EditState>,
        isDefaultSMSAppFlow: StateFlow<Boolean>,
        loadingFlow: StateFlow<Boolean>,
        downloadingFlow: StateFlow<DownloadProgress?>,
    ): ImportScreenUIState {
        val isLoading = loadingFlow.collectAsState().value
        if (isLoading) {
            return ImportScreenUIState.Loading
        }
        val importResult =
            importedConvsFlow.collectAsState().value ?: return ImportScreenUIState.Initial

        val res = when (importResult) {
            is ImportResult.Error -> {
                effects.send(ImportEffect.ShowToast("Error importing: ${importResult.message}"))
                return ImportScreenUIState.Initial
            }
            is ImportResult.Success -> importResult
        }
        val convs = res.convs

        val selections = selectionsFlow.collectAsState().value
        val editState = editStateFlow.collectAsState().value
        val isDefaultSMSApp = isDefaultSMSAppFlow.collectAsState().value
        val downloadProgress = downloadingFlow.collectAsState().value
        val mappedItems = uiMapper.mapToUI(convs, selections.takeIf { editState.editing })

        val hasAnySelections = selections.any { it.value }
        val toolbar = ImportTopToolbarUIState(
            showEdit = editState.editing.not(),
            downloadShowsPrompt = isDefaultSMSApp.not(),
            downloadEnabled = hasAnySelections,
            deleteEnabled = hasAnySelections,
        )
        val selectedContactIds = selections.filter { it.value }.keys
        val selectedMsgs = convs.retrieveMessages(selectedContactIds)
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
            is ImportAction.JSONFileSelected -> importJSONFile(act.uri)
            is ImportAction.XMLFileSelected -> importXMLFile(act.uri)
            ImportAction.ImportJSONClicked -> openJSONFilePicker()
            ImportAction.ImportXMLClicked -> openXMLFilePicker()
            is ImportAction.ConversationClicked -> openConversation(act.contactId)
            ImportAction.ClearSelection -> clearSelections()
            ImportAction.CloseEditClicked -> editState.update { it.copy(editing = false) }
            ImportAction.EditClicked -> editState.update { it.copy(editing = true) }
            ImportAction.DeleteClicked -> deleteSelected()
            ImportAction.DownloadConfirmed -> downloadSelected()
            is ImportAction.OpenSetAsDefaultClicked -> {
                effects.send(ImportEffect.ShowSetAsDefaultPrompt(act.owner))
            }
            ImportAction.SelectAll -> selectAll()
            ImportAction.SetAsDefaultUpdated -> isDefaultSMSApp.value = appScope.isDefaultSMSApp()
            is ImportAction.ConversationChecked -> updateCheckedState(act.contactId, act.checked)
        }
    }

    private fun deleteSelected() = viewModelScope.launch {
        val res = importedConvs.value as? ImportResult.Success ?: return@launch
        val convs = res.convs
        val selected = selections.value
        val removed = convs.removeConvs(selected.filter { it.value }.keys)
        repo.setImportedConversations(removed)
        importedConvs.value = res.copy(convs = removed)
    }

    private fun downloadSelected() = viewModelScope.launch(Dispatchers.IO) {
        val res = importedConvs.value as? ImportResult.Success ?: return@launch
        val convs = res.convs
        val selected = selections.value
        val ids = selected.filter { it.value }.keys
        val selectedMsgs = convs.retrieveMessages(selected.filter { it.value }.keys)
        val total = selectedMsgs.size
        if (total > MessagesRetriever.CHUNK_SIZE) {
            effects.send(ImportEffect.ShowToast("Saving $total messages..."))
        }

        var done = 0
        downloading.update { DownloadProgress(0, total = total) }
        retriever.saveMessages(selectedMsgs).onEach { chunkRes ->
            when (chunkRes) {
                is OperationResult.Done -> {
                    done += chunkRes.data
                    downloading.update { DownloadProgress(done, total = total) }
                }
                is OperationResult.Error -> {
                    effects.send(ImportEffect.ShowToast(chunkRes.msg))
                }
            }
        }.collect()
        if (done < total) {
            effects.send(ImportEffect.ShowToast("Some messages might not be saved - only saved $done out of $total"))
        }
        delay(1000)
        downloading.update { null }
        effects.send(ImportEffect.ShowToast("Messages saved to device"))
        repo.triggerReload()
        delay(1000)
        navigateUp()
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

    private fun updateCheckedState(contactId: String, checked: Boolean) {
        selections.update {
            it.toMutableMap().apply {
                put(contactId, checked)
            }.toMap()
        }
    }

    private fun importJSONFile(uri: Uri) {
        importFile {
            importer.importJson(uri)
        }
    }

    private fun importXMLFile(uri: Uri) {
        importFile {
            importer.importXML(uri)
        }
    }

    private fun importFile(doImport: suspend () -> ImportResult) =
        viewModelScope.launch {
            loading.value = true
            importedConvs.value = doImport().also { res ->
                if (res is ImportResult.Success) {
                    selections.update { res.convs.mapping.map { it.key.id to true }.toMap() }
                    repo.setImportedConversations(res.convs)
                }
            }
            loading.value = false
        }

    private fun openJSONFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        effects.send(ImportEffect.OpenJSONFilePicker(intent))
    }

    private fun openXMLFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        effects.send(ImportEffect.OpenXMLFilePicker(intent))
    }

    private fun openConversation(contactId: String) {
        routeNavigator.navigateToRoute(ConversationRoute.get(contactId))
    }
}

private data class EditState(
    val editing: Boolean = false,
)

private data class DownloadProgress(val done: Int, val total: Int)