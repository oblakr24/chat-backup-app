package com.rokoblak.chatbackup.smsutils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        // TODO: BroadcastReceiver that listens for incoming SMS messages
    }
}