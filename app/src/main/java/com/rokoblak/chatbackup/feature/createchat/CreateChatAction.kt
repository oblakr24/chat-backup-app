package com.rokoblak.chatbackup.feature.createchat

sealed interface CreateChatAction {
    data class ContactClicked(val contactId: String, val number: String) : CreateChatAction
    data class QueryChanged(val query: String) : CreateChatAction
}