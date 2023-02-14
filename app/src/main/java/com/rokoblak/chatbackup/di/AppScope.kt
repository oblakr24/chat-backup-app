package com.rokoblak.chatbackup.di

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.rokoblak.chatbackup.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppScope @Inject constructor(@ApplicationContext context: Context) {

    val appContext: Context = context

    fun hasMessagesPermissions(): Boolean {
        return AppConstants.MESSAGES_PEFRMISSIONS.all {
            ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isDefaultSMSApp(): Boolean {
        val defaultSMSAppPackageName = Telephony.Sms.getDefaultSmsPackage(appContext)
        return appContext.packageName == defaultSMSAppPackageName
    }
}