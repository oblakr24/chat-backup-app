package com.rokoblak.chatbackup.domain.usecases

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.rokoblak.chatbackup.data.datasources.MessagesDataSource
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.model.RootError
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume


class SMSSendUseCase @Inject constructor(
    private val appScope: AppScope,
    private val eventsUseCase: AppEventsUseCase,
) {

    suspend fun send(address: String, body: String): OperationResult<Unit, SMSSendError> {
        val context = appScope.appContext
        val smsManager = ContextCompat.getSystemService(context, SmsManager::class.java)
            ?: throw IllegalAccessError("No SMS manager")

        val action = ACTION_SENT + address + body
        val sendPendingIntent =
            PendingIntent.getBroadcast(context, 0, Intent(action), PendingIntent.FLAG_IMMUTABLE)

        val res = withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine<OperationResult<Unit, SMSSendError>> { cont ->
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
        } ?: OperationResult.Error(SMSSendError.Timeout)

        // TODO: Handle error cases
        when (res) {
            is OperationResult.Done -> {
                Timber.i("Message sent: $body to $address")
            }
            is OperationResult.Error -> {
                Timber.e("Send failure: ${res.error}")
            }
        }
        MessagesDataSource.saveSingle(context, incoming = false, body = body, address = address)

        eventsUseCase.onNewEvent(SMSEvent.NewReceived)

        return res
    }

    private fun Int.mapCodeToResult(): OperationResult<Unit, SMSSendError> = when (this) {
        Activity.RESULT_OK -> OperationResult.Done(Unit)
        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> OperationResult.Error(SMSSendError.GenericFailure)
        SmsManager.RESULT_ERROR_NO_SERVICE -> OperationResult.Error(SMSSendError.NoService)
        SmsManager.RESULT_ERROR_NULL_PDU -> OperationResult.Error(SMSSendError.NullPDU)
        SmsManager.RESULT_ERROR_RADIO_OFF -> OperationResult.Error(SMSSendError.RadioOff)
        else -> OperationResult.Error(SMSSendError.OtherError)
    }

    companion object {
        private const val ACTION_SENT = "sms-action-sent"
    }
}

sealed interface SMSSendError: RootError {
    data object GenericFailure: SMSSendError
    data object NoService: SMSSendError
    data object NullPDU: SMSSendError
    data object RadioOff: SMSSendError
    data object OtherError: SMSSendError
    data object Timeout: SMSSendError
}