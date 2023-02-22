package com.rokoblak.chatbackup.services.sms

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.di.SMSEvent
import com.rokoblak.chatbackup.services.MessagesRetriever
import com.rokoblak.chatbackup.services.OperationResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume


class SMSSender @Inject constructor(private val appScope: AppScope) {

    suspend fun send(address: String, body: String): OperationResult<Unit> {
        val context = appScope.appContext
        val smsManager = ContextCompat.getSystemService(context, SmsManager::class.java)
            ?: throw IllegalAccessError("No SMS manager")

        val action = ACTION_SENT + address + body
        val sendPendingIntent =
            PendingIntent.getBroadcast(context, 0, Intent(action), PendingIntent.FLAG_IMMUTABLE)

        val res = withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine<OperationResult<Unit>> { cont ->
                val sentIntentReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent?) {
                        val resultCode = resultCode
                        val result = resultCode.mapCodeToResult()
                        appScope.appContext.unregisterReceiver(this)
                        cont.resume(result)
                    }
                }

                smsManager.sendTextMessage(address, null, body, sendPendingIntent, null)
                appScope.appContext.registerReceiver(sentIntentReceiver, IntentFilter(action))
            }
        } ?: OperationResult.Error("Error sending: timeout")

        // TODO: Handle error cases
        when (res) {
            is OperationResult.Done -> {
                Timber.i("Message sent: $body to $address")
            }
            is OperationResult.Error -> {
                Timber.e("Send failure: ${res.msg}")
            }
        }
        MessagesRetriever.saveSingle(context, incoming = false, body = body, address = address)

        appScope.onNewEvent(SMSEvent.NewReceived)

        return res
    }

    private fun Int.mapCodeToResult() = when (this) {
        Activity.RESULT_OK -> OperationResult.Done(Unit)
        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> OperationResult.Error("Generic failure")
        SmsManager.RESULT_ERROR_NO_SERVICE -> OperationResult.Error("No service")
        SmsManager.RESULT_ERROR_NULL_PDU -> OperationResult.Error("Null PDU")
        SmsManager.RESULT_ERROR_RADIO_OFF -> OperationResult.Error("Radio off")
        else -> OperationResult.Error("Send failure")
    }

    companion object {
        private const val ACTION_SENT = "sms-action-sent"
    }
}