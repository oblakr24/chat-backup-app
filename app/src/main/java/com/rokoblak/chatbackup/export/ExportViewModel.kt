package com.rokoblak.chatbackup.export

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.home.*
import com.rokoblak.chatbackup.navigation.RouteNavigator
import com.rokoblak.chatbackup.services.ConversationsRepo
import com.rokoblak.chatbackup.services.FileManager
import com.rokoblak.chatbackup.services.parsing.MessagesExporter
import com.rokoblak.chatbackup.services.parsing.MessagesExporter.*
import com.rokoblak.chatbackup.util.SingleEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exporter: MessagesExporter,
    private val conversationsRepo: ConversationsRepo,
    private val routeNavigator: RouteNavigator,
    private val fileManager: FileManager,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    private val exportFlow = flow {
        emit(null)
        emit(exportConversations()).also {
            effects.send(ExportEffect.ShowToast("Export file ready"))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val effects = SingleEventFlow<ExportEffect>()

    val uiState: StateFlow<ExportScreenUIState> by lazy {
        scope.launchMolecule(clock = RecompositionClock.ContextClock) {
            ExportPresenter(exportFlow)
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun ExportPresenter(
        exportResultsFlow: StateFlow<ExportResult?>,
    ): ExportScreenUIState {
        val exportResult = exportResultsFlow.collectAsState().value
        val isLoading = exportResult == null
        val uri = (exportResult as? ExportResult.Success)?.uri
        val convToExport = convToExport()
        return ExportScreenUIState(
            title = "Export ${convToExport?.size} conversations",
            subtitle = if (uri != null) "Export file ready to share" else "",
            showLoading = isLoading,
            showShareButton = uri != null
        )
    }

    fun handleAction(act: ExportAction) {
        when (act) {
            is ExportAction.ShareClicked -> share(act.ownerContext)
        }
    }

    private suspend fun exportConversations(): ExportResult {
        val conv = convToExport()
            ?: return ExportResult.Error(java.lang.IllegalStateException("Nothing to export"))
        return exporter.serialize(conv)
    }

    private fun convToExport() = conversationsRepo.retrieveExportedConvs()

    private fun share(ownerContext: Context) {
        val uri = (exportFlow.value as? ExportResult.Success)?.uri ?: return
        fileManager.share(ownerContext, uri)
    }
}