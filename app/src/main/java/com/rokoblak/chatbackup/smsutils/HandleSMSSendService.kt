package com.rokoblak.chatbackup.smsutils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import timber.log.Timber

class HandleSMSSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        Timber.i("Received SMS send intent")
        // TODO: Service that delivers messages from the phone "quick response"
        return null
    }
}