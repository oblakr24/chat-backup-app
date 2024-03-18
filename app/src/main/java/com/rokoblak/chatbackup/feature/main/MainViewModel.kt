package com.rokoblak.chatbackup.feature.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rokoblak.chatbackup.domain.usecases.DarkModeToggleUseCase
import com.rokoblak.chatbackup.domain.usecases.SMSIntentHandlerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val darkModeUseCase: DarkModeToggleUseCase,
    private val smsIntentHandlerUseCase: SMSIntentHandlerUseCase,
) : ViewModel() {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    val uiState: StateFlow<MainScreenUIState> by lazy {
        scope.launchMolecule(mode = RecompositionMode.ContextClock) {
            MainPresenter(darkModeUseCase.darkModeEnabled())
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

    fun onIntentReceived(intent: Intent) = viewModelScope.launch {
        smsIntentHandlerUseCase.onIntentReceived(intent)
    }
}