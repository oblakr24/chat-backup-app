package com.rokoblak.chatbackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokoblak.chatbackup.di.AppStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    storage: AppStorage,
) : ViewModel() {

    val darkModeEnabled = storage.prefsFlow().map { it.darkMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}