package com.rokoblak.chatbackup.services

import com.rokoblak.chatbackup.data.*
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConversationBuilder @Inject constructor() {

    suspend fun groupMessages(messages: List<Message>, contactRetriever: suspend (number: String) -> Contact?) = withContext(Dispatchers.Default) {
        val grouped = messages.groupBy { it.contact.id }
        val allContacts = messages.map { it.contact }.distinctBy { it.id }.map { c ->
            val resolved = contactRetriever(c.number)
            resolved ?: Contact(name = null, number = c.number, avatarUri = null, phoneType = PhoneType.HOME)
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