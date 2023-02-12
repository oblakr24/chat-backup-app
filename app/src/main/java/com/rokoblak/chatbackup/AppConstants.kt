package com.rokoblak.chatbackup

import android.Manifest

object AppConstants {

    val MESSAGES_PEFRMISSIONS: List<String> = listOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS
    )
}