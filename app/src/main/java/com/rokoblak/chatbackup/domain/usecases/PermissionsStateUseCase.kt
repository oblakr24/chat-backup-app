package com.rokoblak.chatbackup.domain.usecases

import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.rokoblak.chatbackup.AppConstants
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PermissionsStateUseCase @Inject constructor(
    private val appScope: AppScope,
) {

    private val _permissions = MutableStateFlow(
        PermissionsState(
            hasPermissions = hasMessagesPermissions(),
            isDefaultSMSHandlerApp = isDefaultSMSApp()
        )
    )

    val permissions = _permissions.asStateFlow()

    fun updatePermissions() {
        _permissions.value = PermissionsState(
            hasPermissions = hasMessagesPermissions(),
            isDefaultSMSHandlerApp = isDefaultSMSApp()
        )
    }

    fun hasMessagesPermissions(): Boolean {
        return AppConstants.MESSAGES_PEFRMISSIONS.all {
            ContextCompat.checkSelfPermission(appScope.appContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isDefaultSMSApp(): Boolean {
        val defaultSMSAppPackageName = Telephony.Sms.getDefaultSmsPackage(appScope.appContext)
        return appScope.appContext.packageName == defaultSMSAppPackageName
    }
}

data class PermissionsState(
    val hasPermissions: Boolean,
    val isDefaultSMSHandlerApp: Boolean,
)