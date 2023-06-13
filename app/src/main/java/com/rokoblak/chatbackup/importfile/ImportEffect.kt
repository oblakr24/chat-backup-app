package com.rokoblak.chatbackup.importfile

import android.content.Context
import android.content.Intent

sealed interface ImportEffect {
    data class ShowToast(val message: String) : ImportEffect
    data class OpenJSONFilePicker(val intent: Intent) : ImportEffect
    data class ShowSetAsDefaultPrompt(val owner: Context): ImportEffect
}