package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.data.datasources.MessageInsertionError
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.repo.ConversationsRepository
import com.rokoblak.chatbackup.data.datasources.MessagesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DownloadConversationUseCase @Inject constructor(
    private val repo: ConversationsRepository,
    private val retriever: MessagesDataSource,
) {

    private val downloading = MutableStateFlow<DownloadProgress?>(null)
    private val selections = MutableStateFlow(emptyMap<String, Boolean>())
    private val importedConvs = MutableStateFlow<ImportResult?>(null)

    val state = combine(downloading, selections, importedConvs) { progress, selections, importRes ->
        ImportDownloadState(progress, selections, importRes)
    }

    fun clearSelections() {
        selections.update {
            it.toMutableMap().mapValues { false }.toMap()
        }
    }

    fun selectAll() {
        selections.update {
            it.toMutableMap().mapValues { true }.toMap()
        }
    }

    fun updateCheckedState(contactId: String, checked: Boolean) {
        selections.update {
            it.toMutableMap().apply {
                put(contactId, checked)
            }.toMap()
        }
    }

    suspend fun importFile(doImport: suspend () -> ImportResult) {
        importedConvs.value = doImport().also { res ->
            if (res is ImportResult.Success) {
                selections.update { res.convs.mapping.map { it.key.id to true }.toMap() }
                repo.setImportedConversations(res.convs)
            }
        }
    }

    fun deleteSelected() {
        val res = importedConvs.value as? ImportResult.Success ?: return
        val convs = res.convs
        val selected = selections.value
        val keys = selected.filter { it.value }.keys
        val removed = convs.removeConvs(keys)
        repo.setImportedConversations(removed)
        selections.update { it.toMutableMap().filterKeys { k -> keys.contains(k).not() } }
        importedConvs.value = res.copy(convs = removed)
    }

    suspend fun downloadSelected(onProgressMsg: (String) -> Unit) = withContext(Dispatchers.IO) {
        val res = importedConvs.value as? ImportResult.Success ?: return@withContext
        val convs = res.convs
        val selected = selections.value
        val selectedMsgs = convs.retrieveMessages(selected.filter { it.value }.keys)
        val total = selectedMsgs.size
        if (total > MessagesDataSource.CHUNK_SIZE) {
            onProgressMsg("Saving $total messages...")
        }

        var done = 0
        downloading.update { DownloadProgress(0, total = total) }
        retriever.saveMessages(selectedMsgs).onEach { chunkRes ->
            when (chunkRes) {
                is OperationResult.Done -> {
                    done += chunkRes.data
                    downloading.update {
                        DownloadProgress(
                            done,
                            total = total
                        )
                    }
                }

                is OperationResult.Error -> {
                    val msg = when (val e = chunkRes.error) {
                        is MessageInsertionError.InsertionError -> "Inserted ${e.inserted} out of ${e.totalToInsert} total"
                    }
                    onProgressMsg(msg)
                }
            }
        }.collect()
        if (done < total) {
            onProgressMsg("Some messages might not be saved - only saved $done out of $total")
        }
        delay(1000)
        downloading.update { null }
        onProgressMsg("Messages saved to device")
        repo.triggerReload()
        delay(1000)
    }
}

data class ImportDownloadState(
    val progress: DownloadProgress?,
    val selections: Map<String, Boolean>,
    val importResult: ImportResult?,
)

data class DownloadProgress(val done: Int, val total: Int)