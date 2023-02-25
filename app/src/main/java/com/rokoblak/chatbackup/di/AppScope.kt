package com.rokoblak.chatbackup.di

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.rokoblak.chatbackup.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppScope @Inject constructor(@ApplicationContext context: Context) {

    val appContext: Context = context

    private val _smsEvents = MutableSharedFlow<SMSEvent?>(replay = 1)
    val smsEvents: Flow<SMSEvent> = _smsEvents.filterNotNull()

    fun onNewEvent(event: SMSEvent) {
        _smsEvents.tryEmit(event)
    }

    fun markEventConsumed() {
        _smsEvents.tryEmit(null)
    }

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

sealed interface SMSEvent {
    object NewReceived: SMSEvent
    data class OpenCreateChat(val address: String?): SMSEvent
}