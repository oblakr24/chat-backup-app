package com.rokoblak.chatbackup.smsutils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import timber.log.Timber

// Per docs: https://developer.android.com/reference/android/provider/Telephony
class HandleSMSSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        Timber.i("Received SMS send intent")
        // TODO: Service that delivers messages from the phone "quick response"
        // TODO: Parse this according to docs https://android.googlesource.com/platform/frameworks/base/+/android-4.4.2_r1/telephony/java/android/telephony/TelephonyManager.java#128
        //  1. parse Uri and text from EXTRA_TEXT
        //  2. send SMS
        //  3. save SMS
        // TODO: find a way to test this intent - currently, the phone app's quick response is not consumed by this app but another, despite this app being the default handler

        return null
    }
}