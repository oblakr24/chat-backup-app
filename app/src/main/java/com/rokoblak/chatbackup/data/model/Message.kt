package com.rokoblak.chatbackup.data.model

import android.net.Uri
import java.time.Instant

data class Message(
    val id: String,
    val content: String,
    val contact: MinimalContact,
    val imageUri: Uri?,
    val timestamp: Instant,
    val incoming: Boolean
)

data class MinimalContact(val orgNumber: String) {
    val number: String = Contact.normalizeNumber(orgNumber)
    val id: String = Contact.idFromNumber(number)
}

data class Conversation(val contact: Contact, val messages: List<Message>)

data class Conversations(
    val mapping: Map<Contact, Conversation>,
    val sortedContactsByLastMsg: List<Contact>,
) {
    private val idMapping = mapping.mapKeys { it.key.id }
    private val numberMapping = mapping.mapKeys { it.key.number }

    val sortedConversations by lazy {
        sortedContactsByLastMsg.map {
            Conversation(it, mapping[it]!!.messages)
        }
    }

    val contactIds by lazy {
        sortedContactsByLastMsg.map { it.id }.toSet()
    }

    val totalChats by lazy {
        sortedConversations.size
    }

    val totalMessages by lazy {
        sortedConversations.sumOf { it.messages.size }
    }

    fun resolveConvByContactId(contactId: String): Conversation? {
        return idMapping[contactId]
    }

    fun resolveConv(contactId: String?, number: String): Conversation? {
        val resolved = contactId?.let {
            idMapping[contactId]
        }
        return resolved ?: numberMapping[number]
    }

    fun removeConvs(contactIds: Set<String>): Conversations {
        val newContacts = sortedContactsByLastMsg.filterNot { contactIds.contains(it.id) }
        val newMapping = mapping.filterNot { contactIds.contains(it.key.id) }
        return Conversations(newMapping, newContacts)
    }

    fun retrieveMessages(contactIds: Set<String>): List<Message> {
        return idMapping.filterKeys { contactIds.contains(it) }.values.flatMap {
            it.messages
        }
    }
}