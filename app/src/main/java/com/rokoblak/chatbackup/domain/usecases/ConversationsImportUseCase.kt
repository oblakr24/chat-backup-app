package com.rokoblak.chatbackup.domain.usecases

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.rokoblak.chatbackup.data.model.Contact
import com.rokoblak.chatbackup.data.model.Conversation
import com.rokoblak.chatbackup.data.model.Conversations
import com.rokoblak.chatbackup.data.model.Message
import com.rokoblak.chatbackup.data.model.MinimalContact
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.model.RootError
import com.rokoblak.chatbackup.data.util.JsonSerializer
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject


class ConversationsImportUseCase @Inject constructor(
    private val appScope: AppScope,
    private val serializer: JsonSerializer,
) {

    suspend fun importJson(uri: Uri): OperationResult<ImportResult, ImportError> =
        withContext(Dispatchers.IO) {
            val cr = appScope.appContext.contentResolver
            val inputStream = cr.openInputStream(uri)
                ?: return@withContext OperationResult.Error(ImportError.FailedToOpenStream)

            val filename = queryName(uri)

            val parsed = serializer.decodeStream(ConversationsDTO.serializer(), inputStream)

            val conversations = parsed.conversations.map { conv ->
                val contact = Contact(
                    name = conv.contactName,
                    orgNumber = conv.contactNumber,
                )
                val messages = conv.messages.map { msg ->
                    val body = msg.content
                    val timestampMs = msg.timestampMs
                    val msgId = contact.id + body.hashCode() + timestampMs
                    Message(
                        id = msgId,
                        content = body,
                        contact = MinimalContact(conv.contactNumber),
                        timestamp = Instant.ofEpochMilli(timestampMs),
                        incoming = msg.incoming,
                        imageUri = null,
                    )
                }
                Conversation(contact, messages = messages)
            }

            val sortedConversations = conversations.sortedBy { it.messages.maxOf { it.timestamp } }
            val contacts = sortedConversations.map { it.contact }.distinctBy { it.id }
            val mapping = sortedConversations.associateBy {
                it.contact
            }
            val convs = Conversations(mapping = mapping, sortedContactsByLastMsg = contacts)
            OperationResult.Done(ImportResult(filename, convs))
        }

    private fun queryName(uri: Uri): String {
        val returnCursor: Cursor =
            appScope.appContext.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}

sealed interface ImportError : RootError {
    data object FailedToOpenStream : ImportError
}

data class ImportResult(val filename: String, val convs: Conversations)
