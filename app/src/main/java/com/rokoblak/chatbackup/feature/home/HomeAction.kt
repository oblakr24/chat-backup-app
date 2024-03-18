package com.rokoblak.chatbackup.feature.home

sealed interface HomeAction {
    object FAQClicked : HomeAction
    data class ConversationClicked(val contactId: String, val number: String) : HomeAction
    object ExportClicked : HomeAction
    object ImportClicked : HomeAction
    object ComposeClicked : HomeAction
    data class ConversationChecked(val contactId: String, val checked: Boolean) : HomeAction
    object SelectAll : HomeAction
    object ClearSelection : HomeAction
    data class SetDarkMode(val enabled: Boolean) : HomeAction
    object EditClicked : HomeAction
    object CloseEditClicked : HomeAction
    object DeleteClicked : HomeAction
    object PermissionsUpdated : HomeAction
    object OpenRepoUrl : HomeAction
    object OpenSetAsDefaultClicked : HomeAction
    object SetAsDefaultUpdated : HomeAction
    data class QueryChanged(val query: String) : HomeAction
}