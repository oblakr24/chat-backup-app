package com.rokoblak.chatbackup.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.core.app.RemoteInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.di.AppScope
import com.rokoblak.chatbackup.di.AppStorage
import com.rokoblak.chatbackup.di.SMSEvent
import com.rokoblak.chatbackup.services.sms.SMSSender
import com.rokoblak.chatbackup.smsutils.NotifUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    storage: AppStorage,
    private val appScope: AppScope,
    private val smsSender: SMSSender,
) : ViewModel() {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    private val darkModeEnabled = storage.prefsFlow().map { it.darkMode }

    val uiState: StateFlow<MainScreenUIState> by lazy {
        scope.launchMolecule(clock = RecompositionClock.ContextClock) {
            MainPresenter(darkModeEnabled)
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun MainPresenter(
        darkModeEnabledFlow: Flow<Boolean?>,
    ): MainScreenUIState {
        val darkModeEnabled = darkModeEnabledFlow.collectAsState(initial = null).value
        return MainScreenUIState(
            isDarkTheme = darkModeEnabled,
        )
    }

    fun onIntentReceived(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SENDTO -> {
                val scheme = intent.data?.scheme
                val ssp = intent.data?.schemeSpecificPart
                when (scheme) {
                    "sms" -> {
                        val address = ssp?.takeIf { it.isNotBlank() }
                        appScope.onNewEvent(SMSEvent.OpenCreateChat(address))
                    }
                    else -> Unit // TODO: Handle MMS
                }
            }
            else -> handleRemoteSendAction(intent)
        }
    }

    private fun handleRemoteSendAction(intent: Intent) {
        val context = appScope.appContext
        val remoteInputBundle = RemoteInput.getResultsFromIntent(intent)
        if (remoteInputBundle != null) {
            val address = intent.extras?.getString(NotifUtils.EXTRA_ADDRESS)
            val notifId =
                intent.extras?.getString(NotifUtils.EXTRA_NOTIF_ID)?.toIntOrNull() ?: 1
            val body = remoteInputBundle.getString(NotifUtils.RESULT_BUNDLE_KEY)
            if (body != null && address != null) {
                viewModelScope.launch {
                    smsSender.send(address = address, body = body)
                    NotifUtils.updateReplySentNotif(context, notifId)
                }
            }
        }
    }
}