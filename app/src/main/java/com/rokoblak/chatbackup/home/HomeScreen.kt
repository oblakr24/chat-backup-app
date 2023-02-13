package com.rokoblak.chatbackup.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rokoblak.chatbackup.AppConstants
import com.rokoblak.chatbackup.util.MessagingUtils
import com.rokoblak.chatbackup.util.showToast


data class HomeScreenUIState(
    val appBar: HomeAppbarUIState,
    val drawer: HomeDrawerUIState,
    val content: HomeContentUIState,
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    var deniedPermissions: Boolean by rememberSaveable {
        mutableStateOf(false)
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = AppConstants.MESSAGES_PEFRMISSIONS
    ) { res ->
        val allGranted = res.all { it.value }
        if (!allGranted) {
            deniedPermissions = true
        }
        viewModel.handleAction(HomeAction.PermissionsUpdated)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            viewModel.handleAction(HomeAction.SetAsDefaultUpdated)
        })

    Effects(viewModel, launcher)

    val state = viewModel.uiState.collectAsState().value
    val query = viewModel.queries.collectAsState().value

    val contentPermissions = if (permissionsState.allPermissionsGranted) {
        HomeContentUIPermissionsState.PermissionsGiven(
            state.content
        )
    } else {
        val shouldShowRationale = permissionsState.shouldShowRationale
        HomeContentUIPermissionsState.PermissionsNeeded(
            shouldShowRationale = shouldShowRationale,
            shouldShowSettingsBtn = deniedPermissions && !shouldShowRationale,
        )
    }

    val mappedState = HomeScaffoldUIState(state.appBar, state.drawer, contentPermissions)

    HomeScaffold(query, mappedState, onAction = {
        viewModel.handleAction(it)
    }, onLaunchPermissions = {
        permissionsState.launchMultiplePermissionRequest()
    })
}

@Composable
private fun Effects(viewModel: HomeViewModel, launcher: ActivityResultLauncher<Intent>) {
    val ctx = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effects.consumeEvents { effect ->
            when (effect) {
                is HomeEffects.ShowSetAsDefaultPrompt -> {
                    MessagingUtils.launchChangeDefaultPrompt(ctx, launcher)
                }
                is HomeEffects.ShowToast -> {
                    ctx.showToast(effect.message)
                }
                is HomeEffects.OpenIntent -> {
                    ctx.startActivity(effect.intent)
                }
            }
        }
    }
}