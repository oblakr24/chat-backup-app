package com.rokoblak.chatbackup.services

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.rokoblak.chatbackup.commonui.AvatarData
import com.rokoblak.chatbackup.commonui.ChatDisplayData
import com.rokoblak.chatbackup.commonui.ConversationDisplayData
import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.data.Conversations
import com.rokoblak.chatbackup.data.Message
import com.rokoblak.chatbackup.ui.theme.*
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
                        mapSingle(
                            c.contact,
                            selections,
                            conversations,
                            query = searchResults.query,
                            matchingMessage = c.last
                        )
                    }
                    is MatchedContact.MatchingInName -> {
                        mapSingle(
                            c.contact,
                            selections,
                            conversations,
                            query = searchResults.query,
                            matchingMessage = c.matchingLastMsg,
                            matchingInName = true
                        )
                    }
                    MatchedContact.NotMatched -> null
                }
            }.toImmutableList()

            return mappedFromSearch
        }

        val mapped = conversations.sortedContactsByLastMsg.map { contact ->
            mapSingle(contact, selections, conversations)
        }.toImmutableList()

        memoizedMappings[conversations] = MemoizedDisplays(selections, mapped)

        return mapped
    }

    fun mapMessageToUI(message: Message, contact: Contact): ChatDisplayData = with(message) {
        val date = timestamp.formatRelative()
        return ChatDisplayData(
            id = id,
            content = content,
            date = date,
            alignedLeft = incoming,
            avatar = if (incoming) {
                contact.avatar()
            } else {
                null
            }
        )
    }

    private val memoizedMappings = mutableMapOf<Conversations, MemoizedDisplays>()

    data class MemoizedDisplays(
        val selections: Map<String, Boolean>?,
        val displays: ImmutableList<ConversationDisplayData>
    )

    private fun mapSingle(
        contact: Contact,
        selections: Map<String, Boolean>?,
        conversations: Conversations,
        matchingMessage: Message? = null,
        matchingInName: Boolean = false,
        query: String? = null,
    ): ConversationDisplayData {
        val msgs = conversations.mapping[contact]!!.messages
        val lastMsg = msgs.last()
        val dateString = lastMsg.timestamp.formatRelative()
        val id = contact.number + msgs.size + lastMsg.timestamp.toString()
        val checked = selections?.let { it[contact.id] }

        val baseTitle = "${contact.displayName} (${msgs.size} total)"
        val title = if (query != null && matchingInName) {
            baseTitle.highlightOccurrences(query)
        } else {
            AnnotatedString(baseTitle)
        }

        val subtitle = if (query != null && matchingMessage != null) {
            matchingMessage.content.highlightOccurrences(query)
        } else {
            AnnotatedString(lastMsg.content)
        }
        return ConversationDisplayData(
            contactId = contact.id,
            id = id,
            title = title,
            subtitle = subtitle,
            date = dateString,
            checked = checked,
            avatar = contact.avatar(),
            number = contact.number,
        )
    }

    fun mapAvatar(contact: Contact) = contact.avatar()

    private fun Contact.avatar(): AvatarData {
        if (avatarUri != null) {
            return AvatarData.LocalPhoto(avatarUri)
        }
        val color = initials?.let {
            avatarColors.random(Random(id.hashCode()))
        } ?: Color.Gray

        return AvatarData.Initials(
            initials ?: "?", color,
        )
    }

    private fun String.highlightOccurrences(substring: String): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var startIdx = 0
        var idx: Int
        do {
            idx = indexOf(substring, startIdx, ignoreCase = true)
            if (idx >= 0) {
                builder.append(substring(startIdx, idx))
                builder.append(buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Black,
                            textDecoration = TextDecoration.Underline,
                            shadow = Shadow(),
                        )
                    ) {
                        append(substring(idx, idx + substring.length))
                    }
                })
                startIdx = idx + substring.length
            } else {
                if (startIdx < length) {
                    builder.append(substring(startIdx, lastIndex + 1))
                }
            }
        } while (idx >= 0)

        return builder.toAnnotatedString()
    }

    private val avatarColors = listOf(
        DarkRed,
        Color.Blue,
        Color.Gray,
        DarkGreen,
        Color.DarkGray,
        Color.Magenta,
        DarkYellow,
        DarkOrange,
        DarkBrown,
    )
}