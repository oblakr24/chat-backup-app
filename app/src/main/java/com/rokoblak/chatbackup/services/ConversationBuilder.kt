package com.rokoblak.chatbackup.services

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.data.Conversation
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.data.Message
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConversationBuilder @Inject constructor(private val appScope: AppScope) {

    suspend fun groupMessages(messages: List<Message>) = withContext(Dispatchers.Default) {
        val grouped = messages.groupBy { it.contact.id }
        val allContacts = messages.map { it.contact }.distinctBy { it.id }.map { c ->
            val contactName = c.number.resolveContactName(appScope.appContext)
            contactName?.let { resolvedName ->
                Contact(id = c.id, name = resolvedName, number = c.number)
            } ?: Contact(id = c.id, name = null, number = c.number)
        }
        val mappedContacts = allContacts.associateBy { it.id }

        val mapping = grouped.map { (contactId, msgs) ->
            val contact = mappedContacts[contactId]!!
            contact to Conversation(contact, msgs.sortedBy { it.timestamp })
        }.toMap()
        val sortedContacts =
            mapping.entries.sortedByDescending { it.value.messages.last().timestamp }
                .map { it.key }

        Conversations(mapping, sortedContacts)
    }

    private fun String.resolveContactName(context: Context): String? {
        val lookupUri =
            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(this))
        val c = context.contentResolver
            .query(lookupUri, arrayOf(ContactsContract.Data.DISPLAY_NAME), null, null, null)
            ?: return null
        return try {
            c.moveToFirst()
            if (c.count > 0) {
                val displayName = c.getString(0)
                displayName
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            c.close()
        }
    }
}