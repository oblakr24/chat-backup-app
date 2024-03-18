package com.rokoblak.chatbackup.feature.export

sealed interface ExportEffect {
    data class ShowToast(val message: String) : ExportEffect
}