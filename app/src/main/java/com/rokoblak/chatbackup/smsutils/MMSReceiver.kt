package com.rokoblak.chatbackup.smsutils

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.google.android.mms.ContentType
import com.google.android.mms.pdu_alt.GenericPdu
import com.google.android.mms.pdu_alt.NotificationInd
import com.google.android.mms.pdu_alt.PduBody
import com.google.android.mms.pdu_alt.PduParser
import com.google.android.mms.pdu_alt.PduPart
import com.google.android.mms.pdu_alt.RetrieveConf
import com.rokoblak.chatbackup.data.datasources.MessagesDataSource
import com.rokoblak.chatbackup.domain.usecases.AppEventsUseCase
import com.rokoblak.chatbackup.domain.usecases.SMSEvent
import com.rokoblak.chatbackup.ui.notif.NotifUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MMSReceiver  : HiltBroadcastReceiver() {

    @Inject
    lateinit var eventsUseCase: AppEventsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION) return
        val bundle = intent.extras ?: return
        val data: ByteArray = bundle.getByteArray("data") ?: return

        val pduParser = PduParser(data)
        when (val pdu: GenericPdu? = pduParser.parse()) {
            is NotificationInd -> {
                // TODO: Handle MMS notification
            }
            is RetrieveConf -> handleMmsContent(context, pdu)
        }
    }

    private fun handleMmsContent(context: Context, retrieveConf: RetrieveConf) {
        val body: PduBody = retrieveConf.body ?: return

        val from = retrieveConf.from
        val address = from.string

        for (i in 0 until body.partsNum) {
            val part: PduPart = body.getPart(i)
            val contentType = String(part.contentType)

            if (contentType == ContentType.TEXT_PLAIN) {
                val textContent = String(part.data)
                MessagesDataSource.saveSingleMms(context, imageData = null, incoming = true, body = textContent, address = address)
            } else if (contentType.startsWith("image/")) {
                val imageData = part.data
                MessagesDataSource.saveSingleMms(context, imageData, incoming = true, body = "", address = address)
            }
        }

        NotifUtils.showIncomingSMSNotif(
            context,
            body = "MMS",
            title = "MMS from $address",
            address = address,
        )

        eventsUseCase.onNewEvent(SMSEvent.NewReceived)
    }
}