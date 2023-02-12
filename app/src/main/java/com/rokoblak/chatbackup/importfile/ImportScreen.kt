package com.rokoblak.chatbackup.importfile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.rokoblak.chatbackup.util.MessagingUtils
import com.rokoblak.chatbackup.util.showToast


@Composable
fun ImportScreen(viewModel: ImportFileViewModel) {
    val jsonChooserLauncher = createChooserLauncher(onAction = {
        viewModel.handleAction(ImportAction.JSONFileSelected(it))
    })
    val xmlChooserLauncher = createChooserLauncher(onAction = {
        viewModel.handleAction(ImportAction.XMLFileSelected(it))
    })

    val defaultAppLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            viewModel.handleAction(ImportAction.SetAsDefaultUpdated)
        })

    Effects(
        viewModel = viewModel,
        jsonLauncher = jsonChooserLauncher,
        xmlLauncher = xmlChooserLauncher,
        defaultAppLauncher = defaultAppLauncher,
    )

    val state = viewModel.uiState.collectAsState().value
    ImportContent(state = state, onBackPressed = { viewModel.navigateUp() }, onAction = {
        viewModel.handleAction(it)
    })
}

@Composable
private fun createChooserLauncher(onAction: (Uri) -> Unit) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
    onResult = { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        onAction(uri)
    })

@Composable
private fun Effects(
    viewModel: ImportFileViewModel,
    jsonLauncher: ActivityResultLauncher<Intent>,
    xmlLauncher: ActivityResultLauncher<Intent>,
    defaultAppLauncher: ActivityResultLauncher<Intent>,
) {
    val ctx = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effects.consumeEvents { effect ->
            when (effect) {
                is ImportEffect.OpenJSONFilePicker -> {
                    jsonLauncher.launch(effect.intent)
                }
                is ImportEffect.OpenXMLFilePicker -> {
                    xmlLauncher.launch(effect.intent)
                }
                is ImportEffect.ShowSetAsDefaultPrompt -> {
                    MessagingUtils.launchChangeDefaultPrompt(effect.owner, defaultAppLauncher)
                }
                is ImportEffect.ShowToast -> {
                    ctx.showToast(effect.message)
                }
            }
        }
    }
}