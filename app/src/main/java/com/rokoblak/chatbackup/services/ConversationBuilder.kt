package com.rokoblak.chatbackup.services

import com.rokoblak.chatbackup.AppConstants
import com.rokoblak.chatbackup.commonui.PreviewDataUtils.obfuscate
import com.rokoblak.chatbackup.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConversationBuilder @Inject constructor() {

    suspend fun groupMessages(orgMessages: List<Message>, contactRetriever: suspend (number: String) -> Contact?) = withContext(Dispatchers.Default) {
        val messages = if (AppConstants.OBFUSCATE) orgMessages.map { it.obfuscate() } else orgMessages
        val grouped = messages.groupBy { it.contact.id }
        val allContacts = messages.map { it.contact }.distinctBy { it.id }.map { c ->
            val resolved = contactRetriever(c.number)
            resolved ?: Contact(name = null, orgNumber = c.number).let {
                if (AppConstants.OBFUSCATE) it.obfuscate() else it
            }
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
}