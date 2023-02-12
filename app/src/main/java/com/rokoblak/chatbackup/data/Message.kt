package com.rokoblak.chatbackup.data

import java.time.Instant

data class Message(
    val id: String,
    val content: String,
    val contact: MinimalContact,
    val timestamp: Instant,
    val incoming: Boolean
)

data class MinimalContact(val number: String, val id: String)

data class Conversation(val contact: Contact, val messages: List<Message>)

data class Conversations(
    val mapping: Map<Contact, Conversation>,
    val sortedContactsByLastMsg: List<Contact>,
) {
    private val idMapping = mapping.mapKeys { it.key.id }

    val sortedConversations by lazy {
        sortedContactsByLastMsg.map {
            Conversation(it, mapping[it]!!.messages)
        }
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

    fun removeConvs(contactIds: Set<String>): Conversations {
        val newContacts = sortedContactsByLastMsg.filterNot { contactIds.contains(it.id) }
        val newMapping = mapping.filterNot { contactIds.contains(it.key.id) }
        return Conversations(newMapping, newContacts)
    }
}