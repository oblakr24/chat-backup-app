package com.rokoblak.chatbackup.feature.importfile

import android.content.Context
import android.net.Uri

sealed interface ImportAction {
    object ImportJSONClicked : ImportAction
    data class JSONFileSelected(val uri: Uri) : ImportAction
    data class ConversationClicked(val contactId: String, val number: String) : ImportAction
    data class ConversationChecked(val contactId: String, val checked: Boolean) : ImportAction
    object SelectAll: ImportAction
    object ClearSelection: ImportAction
    data class OpenSetAsDefaultClicked(val owner: Context) : ImportAction
    object SetAsDefaultUpdated : ImportAction
    object EditClicked : ImportAction
    object CloseEditClicked : ImportAction
    object DeleteClicked : ImportAction
    object DownloadConfirmed: ImportAction
}