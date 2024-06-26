package com.rokoblak.chatbackup.domain.usecases

import android.net.Uri
import com.rokoblak.chatbackup.data.model.Conversation
import com.rokoblak.chatbackup.data.util.FileManager
import com.rokoblak.chatbackup.data.util.JsonSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MessagesExportUseCase @Inject constructor(
    private val serializer: JsonSerializer,
    private val fileManager: FileManager
) {

    suspend fun serialize(sortedConversations: List<Conversation>): Uri =
        withContext(Dispatchers.IO) {
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

            fileManager.createNewJson(encoded)
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