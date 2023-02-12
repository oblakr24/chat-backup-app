package com.rokoblak.chatbackup.services

import android.net.Uri
import com.rokoblak.chatbackup.data.Conversation
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.export.ExportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MessagesExporter @Inject constructor(
    private val serializer: JsonSerializer,
    private val fileManager: FileManager
) {

    sealed interface ExportResult {
        data class Success(val uri: Uri): ExportResult
        data class Error(val throwable: Throwable): ExportResult
    }

    suspend fun serialize(sortedConversations: List<Conversation>): ExportResult = withContext(Dispatchers.IO) {
        val convDtos = sortedConversations.map { conversation ->
            val messageDtos = conversation.messages.map {
                MessageDTO(
                    content = it.content,
                    timestampMs = it.timestamp.toEpochMilli(),
                    incoming = it.incoming
                )
            }

            SingleConversationDTO(
                contactName = conversation.contact.name,
                contactNumber = conversation.contact.number,
                messages = messageDtos
            )
        }
        val dto = ConversationsDTO(convDtos)

        val encoded = serializer.encode(ConversationsDTO.serializer(), dto)

        val uri = fileManager.createNewJson(encoded)
        ExportResult.Success(uri)
    }
}

@kotlinx.serialization.Serializable
data class ConversationsDTO(val conversations: List<SingleConversationDTO>)

@kotlinx.serialization.Serializable
data class SingleConversationDTO(
    val contactName: String?,
    val contactNumber: String,
    val messages: List<MessageDTO>
)

@kotlinx.serialization.Serializable
data class MessageDTO(val content: String, val timestampMs: Long, val incoming: Boolean)