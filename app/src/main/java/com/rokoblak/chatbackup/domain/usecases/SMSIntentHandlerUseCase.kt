package com.rokoblak.chatbackup.domain.usecases

import android.content.Intent
import androidx.core.app.RemoteInput
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.ui.notif.NotifUtils
import javax.inject.Inject

class SMSIntentHandlerUseCase @Inject constructor(
    private val appScope: AppScope,
    private val eventsUseCase: AppEventsUseCase,
    private val smsSendUseCase: SMSSendUseCase,
) {

    suspend fun onIntentReceived(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SENDTO -> {
                val scheme = intent.data?.scheme
                val ssp = intent.data?.schemeSpecificPart
                when (scheme) {
                    "sms" -> {
                        val address = ssp?.takeIf { it.isNotBlank() }
                        eventsUseCase.onNewEvent(SMSEvent.OpenCreateChat(address))
                    }

                    else -> Unit // TODO: Handle MMS
                }
            }

            else -> handleRemoteSendAction(intent)
        }
    }

    private suspend fun handleRemoteSendAction(intent: Intent) {
        val remoteInputBundle = RemoteInput.getResultsFromIntent(intent)
        if (remoteInputBundle != null) {
            val address = intent.extras?.getString(NotifUtils.EXTRA_ADDRESS)
            val notifId =
                intent.extras?.getString(NotifUtils.EXTRA_NOTIF_ID)?.toIntOrNull() ?: 1
            val body = remoteInputBundle.getString(NotifUtils.RESULT_BUNDLE_KEY)
            if (body != null && address != null) {
                smsSendUseCase.send(address = address, body = body)
                NotifUtils.updateReplySentNotif(appScope.appContext, notifId)
            }
        }
    }
}
