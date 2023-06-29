package com.rokoblak.chatbackup.data.repo

import com.rokoblak.chatbackup.data.model.Conversation
import com.rokoblak.chatbackup.data.model.Conversations
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.datasources.MessagesDataSource
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.domain.usecases.AppEventsUseCase
import com.rokoblak.chatbackup.domain.usecases.PermissionsStateUseCase
import com.rokoblak.chatbackup.domain.usecases.SMSEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ConversationsRepository @Inject constructor(
    private val permissionsUseCase: PermissionsStateUseCase,
    private val eventsUseCase: AppEventsUseCase,
    private val smsRetriever: MessagesDataSource,
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    var importedConversations: Conversations? = null
        private set

    fun setImportedConversations(conversations: Conversations) {
        importedConversations = conversations
    }

    private var deviceSelections = emptySet<String>()

    fun setExportedSelections(mapping: Map<String, Boolean>) {
        deviceSelections = mapping.filter { it.value }.keys
    }

    fun retrieveExportedConvs(): List<Conversation>? {
        return deviceConvsFlow.value?.sortedConversations?.filter {
            deviceSelections.contains(it.contact.id)
        }
    }

    private val loadTypeFlow = MutableStateFlow(ConvLoadType(emitInitial = true))

    private val deletionsFlow = MutableStateFlow<Set<String>?>(null)

    private fun deviceLoadConvsFlow(emitInitial: Boolean): Flow<Conversations?> = flow {
        if (emitInitial) emit(null)
        val conv = if (permissionsUseCase.hasMessagesPermissions()) {
            smsRetriever.retrieveMessages()
        } else {
            Timber.e("No permissions but flow still collected")
            return@flow
        }
        val deletions = deletionsFlow.filterNotNull().runningFold(conv) { c, deletedIds ->
            c.removeConvs(deletedIds)
        }
        emitAll(deletions)
    }

    private val externalReloadEvents = eventsUseCase.smsEvents.filter { it is SMSEvent.NewReceived }
        .map { ConvLoadType(emitInitial = false) }.onEach { eventsUseCase.markEventConsumed() }
    private val mergedReloadEvents = listOf(loadTypeFlow, externalReloadEvents).merge()

    val deviceConvsFlow = mergedReloadEvents.flatMapLatest {
        deviceLoadConvsFlow(it.emitInitial)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    suspend fun deleteDeviceConvs(contactIds: Set<String>): OperationResult<Unit> {
        deletionsFlow.update { contactIds }

        val msgIds = contactIds.flatMap {
            getSmsIdsForConversation(it)
        }.toSet()

        val res = smsRetriever.deleteMessages(msgIds)
        triggerReload()

        return res
    }

    fun triggerReload() {
        deletionsFlow.value = null
        loadTypeFlow.value = ConvLoadType(emitInitial = true)
    }

    private fun getSmsIdsForConversation(contactId: String): Set<String> {
        val conv = deviceConvsFlow.value ?: return emptySet()
        return conv.resolveConvByContactId(contactId)?.messages?.map {
            it.id
        }?.toSet() ?: emptySet()
    }

    private class ConvLoadType(
        val emitInitial: Boolean,
        val ts: Long = Instant.now().toEpochMilli()
    )
}