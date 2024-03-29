package com.rokoblak.chatbackup.feature.export

import android.content.Context
import android.net.Uri


sealed interface ExportAction {
    data class ShareClicked(val ownerContext: Context) : ExportAction
}