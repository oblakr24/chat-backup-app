package com.rokoblak.chatbackup.services.parsing

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.rokoblak.chatbackup.data.*
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject


class ConversationsImporter @Inject constructor(
    private val appScope: AppScope,
    private val serializer: JsonSerializer,
    private val xmlParser: XMLParser,
) {

    suspend fun importJson(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val cr = appScope.appContext.contentResolver
        val inputStream = cr.openInputStream(uri)
            ?: return@withContext ImportResult.Error("Failed to open input stream")

        val filename = queryName(uri)

        val parsed = serializer.decodeStream(ConversationsDTO.serializer(), inputStream)

        val conversations = parsed.conversations.map { conv ->
            val contact = Contact(
                name = conv.contactName,
                number = conv.contactNumber,
                avatarUri = null,
                phoneType = PhoneType.HOME,
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
                    incoming = msg.incoming
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
        ImportResult.Success(filename, convs)
    }

    suspend fun importXML(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val cr = appScope.appContext.contentResolver
        val inputStream = cr.openInputStream(uri)
            ?: return@withContext ImportResult.Error("Failed to open input stream")
        val filename = queryName(uri)
        val convsResult = xmlParser.parse(inputStream, filename)
        convsResult
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

sealed interface ImportResult {
    data class Error(val message: String) : ImportResult
    data class Success(val filename: String, val convs: Conversations) : ImportResult
}