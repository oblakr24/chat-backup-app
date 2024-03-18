package com.rokoblak.chatbackup.feature.home

import android.content.Intent

sealed interface HomeEffects {
    object ShowSetAsDefaultPrompt : HomeEffects
    object HideKeyboard : HomeEffects
    data class ShowToast(val message: String) : HomeEffects
    data class OpenIntent(val intent: Intent) : HomeEffects
}