package com.rokoblak.chatbackup.services

import androidx.compose.ui.graphics.Color
import com.rokoblak.chatbackup.commonui.ConversationDisplayData
import com.rokoblak.chatbackup.commonui.InitialsAvatarData
import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.util.formatRelative
import javax.inject.Inject
import kotlin.random.Random

class ConversationUIMapper @Inject constructor() {

    fun map(
        conversations: Conversations,
        selections: Map<String, Boolean>? = null,
    ): List<ConversationDisplayData> {
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
        }

        return mapped
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