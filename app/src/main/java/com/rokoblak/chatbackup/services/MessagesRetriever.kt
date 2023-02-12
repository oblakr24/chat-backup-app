package com.rokoblak.chatbackup.services

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.net.Uri
import android.provider.Telephony
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.data.Message
import com.rokoblak.chatbackup.data.MinimalContact
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
                    val id = c.getString(c.getColumnIndexOrThrow("_id"))
                    val smsDateStr = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    val number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val threadId = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))

                    val typeCol = Telephony.TextBasedSmsColumns.TYPE

                    val typeColIdx =
                        c.getInt(c.getColumnIndexOrThrow(typeCol))  // 1 - Inbox, 2 - Sent

                    c.moveToNext()

                    val epoch = smsDateStr.toLongOrNull()
                    val timestamp = Instant.ofEpochMilli(epoch ?: return@mapNotNull null)

                    val contactId = "C$number"
                    val isInbox = typeColIdx == Telephony.Sms.MESSAGE_TYPE_INBOX
                    Message(
                        id = id,
                        content = body,
                        contact = MinimalContact(id = contactId, number = number),
                        timestamp = timestamp,
                        incoming = isInbox
                    )
                }
            } else {
                return@withContext Conversations(emptyMap(), emptyList())
            }
            c.close()

            builder.groupMessages(messages)
        } else {
            Conversations(emptyMap(), emptyList())
        }
    }

    suspend fun saveMessages(messages: List<Message>): OperationResult {
        val (incomingMsgs, sentMsgs) = messages.partition { it.incoming }
        val incomingUrl = Telephony.Sms.Inbox.CONTENT_URI
        val sentUrl = Telephony.Sms.Sent.CONTENT_URI
        val res = saveMessagesForUri(incomingMsgs, incomingUrl)
        if (res !is OperationResult.Done) return res
        return saveMessagesForUri(sentMsgs, sentUrl)
    }

    private suspend fun saveMessagesForUri(messages: List<Message>, uri: Uri): OperationResult =
        withContext(Dispatchers.IO) {
            val values = messages.map { it.values() }.toTypedArray()
            val cr = appScope.appContext.contentResolver
            val inseted = cr.bulkInsert(uri, values)
            if (inseted == messages.size) {
                OperationResult.Done
            } else {
                OperationResult.Error("Save error: inserted $inseted out of ${messages.size} total")
            }
        }

    private fun Message.values() = ContentValues().apply {
        put("address", contact.number)
        put("body", content)
        put("read", "1") // 1 = read, 0 = not read
        put("date", timestamp.toEpochMilli())
    }

    suspend fun deleteMessages(ids: Set<String>): OperationResult = withContext(Dispatchers.IO) {
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
                OperationResult.Done
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            OperationResult.Error(e.message ?: "Error deleting: unknown error")
        }
    }
}

sealed interface OperationResult {
    object Done : OperationResult
    data class Error(val msg: String) : OperationResult
}