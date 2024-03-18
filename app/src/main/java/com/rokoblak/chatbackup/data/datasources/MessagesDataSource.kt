package com.rokoblak.chatbackup.data.datasources

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.rokoblak.chatbackup.data.model.Conversations
import com.rokoblak.chatbackup.data.model.Message
import com.rokoblak.chatbackup.data.model.MinimalContact
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.model.RootError
import com.rokoblak.chatbackup.data.util.ConversationBuilder
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.domain.usecases.RetrieveContactUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.Instant
import javax.inject.Inject


class MessagesDataSource @Inject constructor(
    private val appScope: AppScope,
    private val builder: ConversationBuilder,
    private val retrieveContactUseCase: RetrieveContactUseCase,
) {

    suspend fun retrieveMessages(): Conversations = withContext(Dispatchers.IO) {
        val messages = retrieveSMSMessages() + retrieveMMSMessages()

        if (messages.isNotEmpty()) {
            builder.groupMessages(messages, contactRetriever = { num ->
                retrieveContactUseCase.resolveContact(num)
            })
        } else {
            Conversations(emptyMap(), emptyList())
        }
    }

    private fun retrieveSMSMessages(): List<Message> {
        val cr = appScope.appContext.contentResolver
        val c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null) ?: return emptyList()

        val totalSMS = c.count
        if (totalSMS == 0) return emptyList()

        val messages = if (c.moveToFirst()) {
            (0 until totalSMS).mapNotNull {
                c.readMessage().also { c.moveToNext() }
            }
        } else {
            return emptyList()
        }
        c.close()

        return messages
    }

    private fun retrieveMMSMessages(): List<Message> {
        val context = appScope.appContext
        val cr = appScope.appContext.contentResolver
        val c = cr.query(Telephony.Mms.CONTENT_URI, null, null, null, null) ?: return emptyList()

        val totalSMS = c.count
        if (totalSMS == 0) return emptyList()

        val messages = if (c.moveToFirst()) {
            (0 until totalSMS).mapNotNull {
                c.readMMSMessage(context, cr).also { c.moveToNext() }
            }
        } else {
            return emptyList()
        }
        c.close()

        return messages
    }

    private fun getAddress(context: Context, messageId: Long): String? {
        val contentResolver = context.contentResolver
        val uri = ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, messageId).buildUpon()
            .appendPath("addr").build()
        val selection = "${Telephony.Mms.Addr.MSG_ID} = ? AND ${Telephony.Mms.Addr.TYPE} = ?"
        val selectionArgs = arrayOf(messageId.toString(), Telephony_Mms_Addr_TYPE_FROM)
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        var address: String? = null
        if (cursor?.moveToFirst() == true) {
            address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Addr.ADDRESS))
        }
        cursor?.close()

        return address
    }

    private fun Cursor.readMMSMessage(context: Context, cr: ContentResolver): Message? {
        val messageId = getLong(getColumnIndexOrThrow(Telephony.Mms._ID))
        val timestampMillis = getLong(getColumnIndexOrThrow(Telephony.Mms.DATE)) * 1000L

        val incoming = when (getInt(getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX))) {
            Telephony.Mms.MESSAGE_BOX_INBOX -> true
            Telephony.Mms.MESSAGE_BOX_SENT -> false
            else -> { // Other message box types (e.g., drafts, outbox, etc.)
                false
            }
        }

        val address = getAddress(context, messageId) ?: return null

        val partSelection = "${Telephony.Mms.Part.MSG_ID}=?"
        val partSelectionArgs = arrayOf(messageId.toString())
        val partCursor =
            cr.query(Telephony.Mms.Part.CONTENT_URI, null, partSelection, partSelectionArgs, null)

        var imageFileUri: Uri? = null
        var content: String? = null
        // TODO: Read other metadata, properties, image-related info
        while (partCursor?.moveToNext() == true) {
            val contentType =
                partCursor.getString(partCursor.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE))

            if (contentType == "text/plain") {
                content =
                    partCursor.getString(partCursor.getColumnIndexOrThrow(Telephony.Mms.Part.TEXT))
                // Handle text content
            } else if (contentType.startsWith("image/")) {
                val imageFilePath =
                    partCursor.getString(partCursor.getColumnIndexOrThrow(Telephony.Mms.Part._DATA))
                val fileName = imageFilePath.substringAfterLast('/')
                imageFileUri = Uri.withAppendedPath(Telephony.Mms.Part.CONTENT_URI, fileName)
                // TODO: path not accessible or invalid?
            }
        }
        partCursor?.close()

        return Message(
            id = messageId.toString(),
            content = content ?: "MMS Message",
            contact = MinimalContact(address),
            imageUri = imageFileUri,
            timestamp = Instant.ofEpochMilli(timestampMillis),
            incoming = incoming,
        )
    }

    private fun Cursor.readMessage(): Message? {
        val id = getString(getColumnIndexOrThrow("_id"))
        val smsDateStr = getString(getColumnIndexOrThrow(Telephony.Sms.DATE))
        val number = getString(getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
        val body = getString(getColumnIndexOrThrow(Telephony.Sms.BODY))

        val typeCol = Telephony.TextBasedSmsColumns.TYPE

        val typeColIdx = getInt(getColumnIndexOrThrow(typeCol))  // 1 - Inbox, 2 - Sent

        val epoch = smsDateStr.toLongOrNull()
        val timestamp = Instant.ofEpochMilli(epoch ?: return null)

        val isInbox = typeColIdx == Telephony.Sms.MESSAGE_TYPE_INBOX
        return Message(
            id = id,
            content = body,
            contact = MinimalContact(orgNumber = number),
            timestamp = timestamp,
            incoming = isInbox,
            imageUri = null,
        )
    }

    suspend fun saveMessages(messages: List<Message>): Flow<OperationResult<Int, MessageInsertionError>> {
        val (incomingMsgs, sentMsgs) = messages.partition { it.incoming }
        val incomingUrl = Telephony.Sms.Inbox.CONTENT_URI
        val sentUrl = Telephony.Sms.Sent.CONTENT_URI
        return flow {
            emitAll(saveMessagesForUri(incomingMsgs, incomingUrl))
            emitAll(saveMessagesForUri(sentMsgs, sentUrl))
        }
    }

    private suspend fun saveMessagesForUri(
        messages: List<Message>,
        uri: Uri
    ): Flow<OperationResult<Int, MessageInsertionError>> =
        withContext(Dispatchers.IO) {
            val allValues = messages.map {
                createMsgValues(
                    number = it.contact.number, content = it.content, timestamp = it.timestamp
                )
            }
            val cr = appScope.appContext.contentResolver
            // For some reason, the content resolver operation will fail when bulk inserts are too big, so we chunk them
            flow {
                allValues.chunked(CHUNK_SIZE).forEach { valuesList ->
                    val inserted = cr.bulkInsert(uri, valuesList.toTypedArray()).also {
                        Timber.i("Inserted: $it out of ${valuesList.size}")
                    }
                    if (inserted != valuesList.size) {
                        emit(OperationResult.Error(MessageInsertionError.InsertionError(totalToInsert = valuesList.size, inserted = inserted)))
                        return@forEach
                    } else {
                        emit(OperationResult.Done(inserted))
                    }
                }
            }
        }


    suspend fun deleteMessages(ids: Set<String>): OperationResult<Unit, MessageDeletionError> =
        withContext(Dispatchers.IO) {
            val cr = appScope.appContext.contentResolver

            val ops = ids.map {
                val deleteUri = Uri.parse("${Telephony.Sms.CONTENT_URI}/${it}")
                ContentProviderOperation.newDelete(deleteUri)
                    .withSelection(null, null).build()
            }
            try {
                val results = cr.applyBatch("sms", ArrayList(ops))
                val anyFailed = results.any {
                    it.count != 1
                }
                if (anyFailed) {
                    val totalDeleted = results.sumOf { it.count ?: 0 }
                    OperationResult.Error(MessageDeletionError.DeletionError(
                        totalToDelete = ids.size,
                        deleted = totalDeleted
                    ))
                } else {
                    OperationResult.Done(Unit)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                OperationResult.Error(MessageDeletionError.GenericDeletionError(e.message ?: "Error deleting: unknown error"))
            }
        }

    companion object {
        const val CHUNK_SIZE = 250

        private const val Telephony_Mms_Addr_TYPE_FROM = "137" // Magic number

        fun saveSingle(context: Context, incoming: Boolean, body: String, address: String) {
            val cr = context.contentResolver
            val values =
                createMsgValues(number = address, content = body, timestamp = Instant.now())

            val uri =
                if (incoming) Telephony.Sms.Inbox.CONTENT_URI else Telephony.Sms.Sent.CONTENT_URI
            cr.insert(uri, values)
        }

        fun saveSingleMms(
            context: Context,
            imageData: ByteArray?,
            incoming: Boolean,
            body: String,
            address: String
        ) {
            val cr = context.contentResolver
            val timestamp = Instant.now()

            // TODO: Use incoming for MESSAGE_BOX

            val imgPath = imageData?.let { bytes ->
                val imageFileName =
                    "temp_image${timestamp.toEpochMilli()}.jpg" // Use a unique and appropriate file name
                val imageFile = File(context.cacheDir, imageFileName)
                imageFile.writeBytes(bytes)
                imageFile.absolutePath
            }

            val messageUri = cr.insert(Telephony.Mms.CONTENT_URI, ContentValues().apply {
                put(Telephony.Mms.Addr.ADDRESS, address)
                put("read", "1") // 1 = read, 0 = not read
                put("date", timestamp.toEpochMilli())
            }) ?: return

            val messageId = ContentUris.parseId(messageUri)

            cr.insert(Telephony.Mms.Part.CONTENT_URI, ContentValues().apply {
                put(Telephony.Mms.Part.MSG_ID, messageId)
                put(Telephony.Mms.Part.CONTENT_TYPE, "text/plain")
                put(Telephony.Mms.Part.TEXT, body)
            })

            cr.insert(Telephony.Mms.Part.CONTENT_URI, ContentValues().apply {
                put(Telephony.Mms.Part.MSG_ID, messageId)
                put(Telephony.Mms.Part.CONTENT_TYPE, "image/jpeg")
                put(Telephony.Mms.Part._DATA, imgPath)
            })
        }

        private fun createMsgValues(number: String, content: String, timestamp: Instant) =
            ContentValues().apply {
                put("address", number)
                put("body", content)
                put("read", "1") // 1 = read, 0 = not read
                put("date", timestamp.toEpochMilli())
            }
    }
}

sealed interface MessageDeletionError: RootError {
    data class DeletionError(val totalToDelete: Int, val deleted: Int): MessageDeletionError
    data class GenericDeletionError(val message: String): MessageDeletionError
}

sealed interface MessageInsertionError: RootError {
    data class InsertionError(val totalToInsert: Int, val inserted: Int): MessageInsertionError
}