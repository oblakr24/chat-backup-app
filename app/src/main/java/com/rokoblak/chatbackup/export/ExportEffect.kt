package com.rokoblak.chatbackup.export

sealed interface ExportEffect {
    data class ShowToast(val message: String) : ExportEffect
}