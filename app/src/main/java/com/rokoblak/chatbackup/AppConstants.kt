package com.rokoblak.chatbackup

import android.Manifest
import android.os.Build

object AppConstants {

    const val REPO_URL = "https://github.com/oblakr24/chat-backup-app"

    val MESSAGES_PEFRMISSIONS: List<String> = listOfNotNull(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_MMS,
    )
}