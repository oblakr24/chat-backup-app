package com.rokoblak.chatbackup.feature.export

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.commonui.ButtonWithIcon
import com.rokoblak.chatbackup.ui.commonui.DetailsContent
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.util.showToast

data class ExportScreenUIState(
    val title: String,
    val subtitle: String,
    val showLoading: Boolean,
    val showShareButton: Boolean,
)

@Composable
fun ExportScreen(viewModel: ExportViewModel) {
    val state = viewModel.uiState.collectAsState().value

    ExportScreenContent(state = state, onNavigateBack = {
        viewModel.navigateUp()
    }, handleAction = {
        viewModel.handleAction(it)
    })

    Effects(viewModel)
}

@Composable
private fun ExportScreenContent(
    state: ExportScreenUIState,
    onNavigateBack: () -> Unit,
    handleAction: (ExportAction) -> Unit
) {
    DetailsContent(title = state.title, onBackPressed = {
        onNavigateBack()
    }) {
        if (state.subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(state.subtitle)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (state.showLoading) {
            Text("Loading...")
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }
        if (state.showShareButton) {
            val ctx = LocalContext.current
            ButtonWithIcon("Share", Icons.Filled.Share) {
                handleAction(ExportAction.ShareClicked(ctx))
            }
        }
    }
}

@Composable
private fun Effects(viewModel: ExportViewModel) {
    val ctx = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effects.consumeEvents { effect ->
            when (effect) {
                is ExportEffect.ShowToast -> {
                    ctx.showToast(effect.message)
                }
            }
        }
    }
}


@Preview
@Composable
fun ExportScreenPreview() {
    ChatBackupTheme {
        val state = ExportScreenUIState(
            title = "Export",
            subtitle = "subtitle",
            showLoading = true,
            showShareButton = false,
        )
        ExportScreenContent(state = state, onNavigateBack = { }, handleAction = {})
    }
}