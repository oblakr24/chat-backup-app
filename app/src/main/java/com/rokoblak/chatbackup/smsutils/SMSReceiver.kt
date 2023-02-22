package com.rokoblak.chatbackup.smsutils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.di.SMSEvent
import com.rokoblak.chatbackup.services.MessagesRetriever
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Workaround for Hilt injection
abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
}

@AndroidEntryPoint
class SMSReceiver : HiltBroadcastReceiver() {

    @Inject lateinit var appScope: AppScope

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != null && intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            val bundle = intent.extras
            if (bundle != null) {
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

                    MessagesRetriever.saveSingle(context, incoming = true, body = msgBody, address = orgAddress)

                    NotifUtils.showIncomingSMSNotif(
                        context,
                        body = msgBody,
                        title = "Message from $displayAddress",
                        address = orgAddress
                    )
                }

                appScope.onNewEvent(SMSEvent.NewReceived)
            }
        }
    }

    companion object {

        private const val SMS_BUNDLE_PDUS = "pdus"
    }
}