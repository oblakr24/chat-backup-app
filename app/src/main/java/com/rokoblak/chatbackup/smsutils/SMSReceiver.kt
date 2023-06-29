package com.rokoblak.chatbackup.smsutils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import com.rokoblak.chatbackup.data.datasources.MessagesDataSource
import com.rokoblak.chatbackup.domain.usecases.AppEventsUseCase
import com.rokoblak.chatbackup.domain.usecases.SMSEvent
import com.rokoblak.chatbackup.ui.notif.NotifUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Workaround for Hilt injection
abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
}

@AndroidEntryPoint
class SMSReceiver : HiltBroadcastReceiver() {

    @Inject
    lateinit var eventsUseCase: AppEventsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
        val bundle = intent.extras ?: return

        val smsBytesArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getSerializable(SMS_BUNDLE_PDUS, Array<java.io.Serializable>::class.java)
        } else {
            bundle.getSerializable(SMS_BUNDLE_PDUS) as? Array<java.io.Serializable>
        } ?: return

        smsBytesArray.forEach {
            val format = bundle.getString("format")
            val sms = SmsMessage.createFromPdu(it as ByteArray, format)

            val msgBody = sms.messageBody.toString()
            val displayAddress = sms.displayOriginatingAddress.orEmpty()
            val orgAddress = sms.originatingAddress.orEmpty()

            MessagesDataSource.saveSingle(
                context,
                incoming = true,
                body = msgBody,
                address = orgAddress,
            )

            NotifUtils.showIncomingSMSNotif(
                context,
                body = msgBody,
                title = "Message from $displayAddress",
                address = orgAddress,
            )
        }

        eventsUseCase.onNewEvent(SMSEvent.NewReceived)
    }

    companion object {

        private const val SMS_BUNDLE_PDUS = "pdus"
    }
}