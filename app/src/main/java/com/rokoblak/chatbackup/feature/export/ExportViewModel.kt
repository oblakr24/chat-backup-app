package com.rokoblak.chatbackup.feature.export

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.model.RootError
import com.rokoblak.chatbackup.data.repo.ConversationsRepository
import com.rokoblak.chatbackup.data.util.FileManager
import com.rokoblak.chatbackup.domain.usecases.MessagesExportUseCase
import com.rokoblak.chatbackup.ui.navigation.RouteNavigator
import com.rokoblak.chatbackup.util.SingleEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exporter: MessagesExportUseCase,
    private val conversationsRepo: ConversationsRepository,
    private val routeNavigator: RouteNavigator,
    private val fileManager: FileManager,
) : ViewModel(), RouteNavigator by routeNavigator {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    private val exportFlow = flow {
        emit(null)
        emit(exportConversations().doOnError {
            effects.send(ExportEffect.ShowToast("Nothing to export"))
        }.doOnSuccess {
            effects.send(ExportEffect.ShowToast("Export file ready"))
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val effects = SingleEventFlow<ExportEffect>()

    val uiState: StateFlow<ExportScreenUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.ContextClock) {
            ExportPresenter(exportFlow)
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun ExportPresenter(
        exportResultsFlow: StateFlow<OperationResult<Uri, NothingToExport>?>,
    ): ExportScreenUIState {
        val exportResult = exportResultsFlow.collectAsState().value
        val isLoading = exportResult == null
        val uri = exportResult?.optValue()
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

    private suspend fun exportConversations(): OperationResult<Uri, NothingToExport> {
        val conv = convToExport()
            ?: return OperationResult.Error(NothingToExport)
        return OperationResult.Done(exporter.serialize(conv))
    }

    private fun convToExport() = conversationsRepo.retrieveExportedConvs()

    private fun share(ownerContext: Context) {
        val uri = exportFlow.value?.optValue() ?: return
        fileManager.share(ownerContext, uri)
    }

    data object NothingToExport : RootError
}