package com.rokoblak.chatbackup.data.util

import com.rokoblak.chatbackup.data.model.Contact
import com.rokoblak.chatbackup.data.model.Conversation
import com.rokoblak.chatbackup.data.model.Conversations
import com.rokoblak.chatbackup.data.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ConversationSearcher @Inject constructor() {

    suspend fun searchForQuery(convs: Conversations, query: String) =
        withContext(Dispatchers.Default) {
            val matched = convs.sortedContactsByLastMsg.map {
                it.matches(convs.mapping[it]!!, query)
            }
            if (matched.all { it is MatchedContact.NotMatched }) return@withContext null
            SearchResults(query, matched)
        }

    private fun Contact.matches(conv: Conversation, query: String): MatchedContact {
        val inName = displayName.contains(query, ignoreCase = true)
        val matchingMsg = conv.messages.lastOrNull { it.matches(query) }
        if (inName) return MatchedContact.MatchingInName(this, matchingMsg)
        if (matchingMsg != null) {
            return MatchedContact.MatchingInMessage(this, matchingMsg)
        }
        return MatchedContact.NotMatched
    }

    private fun Message.matches(query: String): Boolean {
        return content.contains(query, ignoreCase = true)
    }
}

data class SearchResults(
    val query: String,
    val matchingContacts: List<MatchedContact>
)

sealed interface MatchedContact {
    object NotMatched : MatchedContact
    data class MatchingInName(val contact: Contact, val matchingLastMsg: Message?) : MatchedContact
    data class MatchingInMessage(val contact: Contact, val last: Message) : MatchedContact
}
