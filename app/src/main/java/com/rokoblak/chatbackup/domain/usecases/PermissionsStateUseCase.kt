package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PermissionsStateUseCase @Inject constructor(
    private val appScope: AppScope,
) {

    private val _permissions = MutableStateFlow(
        PermissionsState(
            hasPermissions = appScope.hasMessagesPermissions(),
            isDefaultSMSHandlerApp = appScope.isDefaultSMSApp()
        )
    )

    val permissions = _permissions.asStateFlow()

    fun updatePermissions() {
        _permissions.value = PermissionsState(
            hasPermissions = appScope.hasMessagesPermissions(),
            isDefaultSMSHandlerApp = appScope.isDefaultSMSApp()
        )
    }
}

data class PermissionsState(
    val hasPermissions: Boolean,
    val isDefaultSMSHandlerApp: Boolean,
)