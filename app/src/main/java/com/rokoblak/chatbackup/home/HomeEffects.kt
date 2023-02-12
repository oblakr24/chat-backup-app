package com.rokoblak.chatbackup.home

import android.content.Intent

sealed interface HomeEffects {
    object ShowSetAsDefaultPrompt : HomeEffects
    data class ShowToast(val message: String) : HomeEffects
    data class OpenIntent(val intent: Intent) : HomeEffects
}