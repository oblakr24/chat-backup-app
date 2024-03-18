package com.rokoblak.chatbackup.feature.conversation

sealed interface ConversationAction {
    data class InputChanged(val input: String) : ConversationAction
    data class SendClicked(val input: String) : ConversationAction
}