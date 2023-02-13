package com.rokoblak.chatbackup.services

import com.rokoblak.chatbackup.data.Conversation
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationsRepo @Inject constructor(
    private val appScope: AppScope,
    private val smsRetriever: MessagesRetriever,
) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    val deviceConversations: Conversations? get() = deviceConvsFlow.value

    private var importedConversations: Conversations? = null

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

    fun retrieveConversation(contactId: String): Conversation? =
        deviceConversations?.resolveConvByContactId(contactId) ?: importedConversations?.resolveConvByContactId(contactId)

    private val loadTypeFlow = MutableStateFlow(ConvLoadType())

    private val deletionsFlow = MutableStateFlow<Set<String>?>(null)

    private val deviceLoadConvsFlow: Flow<Conversations?> = flow {
        emit(null)
        val conv =if (appScope.hasMessagesPermissions()) {
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

    val deviceConvsFlow = loadTypeFlow.flatMapLatest {
        deviceLoadConvsFlow
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
        loadTypeFlow.value = ConvLoadType()
    }

    private fun getSmsIdsForConversation(contactId: String): Set<String> {
        val conv = deviceConvsFlow.value ?: return emptySet()
        return conv.resolveConvByContactId(contactId)?.messages?.map {
            it.id
        }?.toSet() ?: emptySet()
    }

    private class ConvLoadType(val ts: Long = Instant.now().toEpochMilli())
}