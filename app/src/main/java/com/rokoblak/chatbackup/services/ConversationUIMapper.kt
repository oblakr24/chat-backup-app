package com.rokoblak.chatbackup.services

import androidx.compose.ui.graphics.Color
import com.rokoblak.chatbackup.commonui.ConversationDisplayData
import com.rokoblak.chatbackup.commonui.InitialsAvatarData
import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.util.formatRelative
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlin.random.Random

class ConversationUIMapper @Inject constructor() {

    fun mapToUI(
        conversations: Conversations,
        selections: Map<String, Boolean>? = null,
        searchResults: SearchResults? = null,
    ): ImmutableList<ConversationDisplayData> {
        val memoized = memoizedMappings[conversations]
        if (searchResults == null && memoized != null) {
            if (memoized.selections == selections) {
                return memoized.displays
            }
            return memoized.displays.map { convDisplay ->
                val checked = selections?.let { it[convDisplay.contactId] }
                convDisplay.copy(
                    checked = checked,
                )
            }.toImmutableList()
        }

        if (searchResults != null) {
            val mappedFromSearch = searchResults.matchingContacts.mapNotNull { c ->
                when (c) {
                    is MatchedContact.MatchingInMessage -> {
                        // TODO: construct search-specific UI: bold the message part
                        mapSingle(c.contact, selections, conversations)
                    }
                    is MatchedContact.MatchingInName -> {
                        // TODO: construct search-specific UI: bold the name part
                        mapSingle(c.contact, selections, conversations)
                    }
                    MatchedContact.NotMatched -> null
                }
            }.toImmutableList()

            return mappedFromSearch
        }

        val mapped = conversations.sortedContactsByLastMsg.map { contact ->
            val msgs = conversations.mapping[contact]!!.messages
            val lastMsg = msgs.last()
            val dateString = lastMsg.timestamp.formatRelative()
            val id = contact.number + msgs.size + lastMsg.timestamp.toString()
            val checked = selections?.let { it[contact.id] }
            ConversationDisplayData(
                contactId = contact.id,
                id = id,
                title = "${contact.displayName} (${msgs.size} total)",
                subtitle = lastMsg.content,
                date = dateString,
                checked = checked,
                avatarData = contact.avatar(),
            )
        }.toImmutableList()

        memoizedMappings[conversations] = MemoizedDisplays(selections, mapped)

        return mapped
    }

    private val memoizedMappings = mutableMapOf<Conversations, MemoizedDisplays>()

    data class MemoizedDisplays(
        val selections: Map<String, Boolean>?,
        val displays: ImmutableList<ConversationDisplayData>
    )

    private fun mapSingle(
        contact: Contact,
        selections: Map<String, Boolean>?,
        conversations: Conversations
    ): ConversationDisplayData {
        val msgs = conversations.mapping[contact]!!.messages
        val lastMsg = msgs.last()
        val dateString = lastMsg.timestamp.formatRelative()
        val id = contact.number + msgs.size + lastMsg.timestamp.toString()
        val checked = selections?.let { it[contact.id] }
        return ConversationDisplayData(
            contactId = contact.id,
            id = id,
            title = "${contact.displayName} (${msgs.size} total)",
            subtitle = lastMsg.content,
            date = dateString,
            checked = checked,
            avatarData = contact.avatar(),
        )
    }

    private fun Contact.avatar(): InitialsAvatarData {
        val initials = name?.let { n ->
            val split = n.split(" ")
            split.joinToString(separator = "") { it.first().uppercase() }
        } ?: "?"
        val color = name?.let {
            avatarColors.random(Random(id.hashCode()))
        } ?: Color.Gray

        return InitialsAvatarData(
            initials, color,
        )
    }

    private val avatarColors = listOf(
        Color.Red,
        Color.Blue,
        Color.Gray,
        Color.Green,
        Color.DarkGray,
        Color.Magenta,
        Color.Yellow,
    )
}