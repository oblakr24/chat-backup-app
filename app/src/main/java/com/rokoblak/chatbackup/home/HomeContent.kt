package com.rokoblak.chatbackup.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermPhoneMsg
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.BuildConfig
import com.rokoblak.chatbackup.commonui.ButtonWithIcon
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

sealed interface HomeContentUIPermissionsState {
    data class PermissionsNeeded(
        val shouldShowRationale: Boolean,
        val shouldShowSettingsBtn: Boolean
    ) : HomeContentUIPermissionsState

    data class PermissionsGiven(val content: HomeContentUIState) : HomeContentUIPermissionsState
}

@Composable
fun HomeContent(
    state: HomeContentUIPermissionsState,
    onAction: (HomeAction) -> Unit,
    onLaunchPermissions: () -> Unit,
) {

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { _ ->
            // Let the viewmodel know if permissions changed, in which case we will recompose
            onAction(HomeAction.PermissionsUpdated)
        })

    when (state) {
        is HomeContentUIPermissionsState.PermissionsGiven -> {
            val innerState = state.content
            HomeInnerContent(state = innerState, onAction)
        }
        is HomeContentUIPermissionsState.PermissionsNeeded -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                if (state.shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // then gently explain why the app requires this permission
                    Text("The app needs to read messages and contacts in order to list and export the data. Please grant the necessary permissions.")

                    ButtonWithIcon("Grant permissions", icon = Icons.Filled.PermPhoneMsg) {
                        onLaunchPermissions()
                    }
                } else {
                    // It's the first time the user lands on this feature, or the user doesn't want to be asked again for this permission
                    val text =
                        "The app cannot list and export the messages without the necessary permissions. Please grant the necessary permissions in order to enable this functionality."
                    Text(text)

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.shouldShowSettingsBtn) {
                        ButtonWithIcon("Open settings", icon = Icons.Filled.Settings) {
                            val packageName = BuildConfig.APPLICATION_ID
                            settingsLauncher.launch(
                                Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    addCategory(Intent.CATEGORY_DEFAULT)
                                    data = Uri.fromParts("package", packageName, null)
                                }
                            )
                        }
                    } else {
                        ButtonWithIcon("Grant permissions", icon = Icons.Filled.PermPhoneMsg) {
                            onLaunchPermissions()
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeContentNoPermissionsPreview() {
    ChatBackupTheme {
        val state = HomeContentUIPermissionsState.PermissionsNeeded(
            shouldShowRationale = false,
            shouldShowSettingsBtn = true
        )

        HomeContent(state = state, onAction = {}, onLaunchPermissions = {})
    }
}