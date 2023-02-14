package com.rokoblak.chatbackup

import android.Manifest

object AppConstants {

    const val REPO_URL = "https://github.com/oblakr24/chat-backup-app"

    val MESSAGES_PEFRMISSIONS: List<String> = listOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS
    )
}