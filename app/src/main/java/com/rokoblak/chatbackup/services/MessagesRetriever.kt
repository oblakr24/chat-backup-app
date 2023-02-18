package com.rokoblak.chatbackup.services

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.rokoblak.chatbackup.commonui.PreviewDataUtils.obfuscateContent
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.data.Message
import com.rokoblak.chatbackup.data.MinimalContact
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject


class MessagesRetriever @Inject constructor(
    private val appScope: AppScope,
    private val builder: ConversationBuilder
) {

    suspend fun retrieveMessages(): Conversations = withContext(Dispatchers.IO) {
        val cr = appScope.appContext.contentResolver
        val c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null)

        if (c != null) {
            val totalSMS = c.count
            if (totalSMS == 0) return@withContext Conversations(emptyMap(), emptyList())

            val messages = if (c.moveToFirst()) {
                (0 until totalSMS).mapNotNull {
                    c.readMessage().also { c.moveToNext() }
                }
            } else {
                return@withContext Conversations(emptyMap(), emptyList())
            }
            c.close()

            builder.groupMessages(messages)
        } else {
            Timber.e("Content SMS query returned null")
            Conversations(emptyMap(), emptyList())
        }
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

        val contactId = "C$number"
        val isInbox = typeColIdx == Telephony.Sms.MESSAGE_TYPE_INBOX
        return Message(
            id = id,
            content = body,
            contact = MinimalContact(id = contactId, number = number),
            timestamp = timestamp,
            incoming = isInbox
        )
    }

    suspend fun saveMessages(messages: List<Message>): Flow<OperationResult<Int>> {
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
    ): Flow<OperationResult<Int>> =
        withContext(Dispatchers.IO) {
            val allValues = messages.map { it.values() }
            val cr = appScope.appContext.contentResolver
            // For some reason, the content resolver operation will fail when bulk inserts are too big, so we chunk them
            flow {
                allValues.chunked(CHUNK_SIZE).forEach { valuesList ->
                    val inserted = cr.bulkInsert(uri, valuesList.toTypedArray()).also {
                        Timber.i("Inserted: $it out of ${valuesList.size}")
                    }
                    if (inserted != valuesList.size) {
                        emit(OperationResult.Error("Failed to insert messages: $inserted out of ${valuesList.size}"))
                        return@forEach
                    } else {
                        emit(OperationResult.Done(inserted))
                    }
                }
            }
        }

    private fun Message.values() = ContentValues().apply {
        put("address", contact.number)
        put("body", content)
        put("read", "1") // 1 = read, 0 = not read
        put("date", timestamp.toEpochMilli())
    }

    suspend fun deleteMessages(ids: Set<String>): OperationResult<Unit> =
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
                    OperationResult.Error("Error deleting ${ids.size} messages: only deleted $totalDeleted")
                } else {
                    OperationResult.Done(Unit)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                OperationResult.Error(e.message ?: "Error deleting: unknown error")
            }
        }

    companion object {
        const val CHUNK_SIZE = 250
    }
}

sealed interface OperationResult<out T : Any?> {
    data class Done<out T : Any>(val data: T) : OperationResult<T>
    data class Error(val msg: String) : OperationResult<Nothing>
}