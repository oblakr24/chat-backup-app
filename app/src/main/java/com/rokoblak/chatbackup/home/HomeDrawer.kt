package com.rokoblak.chatbackup.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.commonui.ButtonWithIcon
import com.rokoblak.chatbackup.ui.theme.AppThemePreviews
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme


data class HomeDrawerUIState(
    val darkMode: Boolean?,
    val showDefaultSMSLabel: Boolean,
    val showComposeAndImport: Boolean,
    val versionLabel: String,
)

@Composable
fun HomeDrawer(
    state: HomeDrawerUIState,
    onAction: (HomeAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(), horizontalAlignment = Alignment.Start
    ) {
        if (state.showComposeAndImport) {
            ButtonWithIcon(modifier = Modifier.padding(horizontal = 16.dp),
                text = "Compose",
                icon = Icons.AutoMirrored.Filled.Message,
                onClick = { onAction(HomeAction.ComposeClicked) })
            Spacer(modifier = Modifier.height(8.dp))
            ButtonWithIcon(modifier = Modifier.padding(horizontal = 16.dp),
                text = "Import",
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                onClick = { onAction(HomeAction.ImportClicked) })
            Spacer(modifier = Modifier.height(8.dp))
        }
        ButtonWithIcon(modifier = Modifier.padding(horizontal = 16.dp),
            text = "FAQ",
            icon = Icons.Filled.QuestionAnswer,
            onClick = { onAction(HomeAction.FAQClicked) })
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {

            Row(
                Modifier
                    .wrapContentWidth()
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Dark mode")

                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = state.darkMode ?: isSystemInDarkTheme(),
                    onCheckedChange = { enabled ->
                        onAction(HomeAction.SetDarkMode(enabled))
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
            ) {
                Text("This is an open-source app.\nYou can post feedback, change requests or contribute on Github:")

                Spacer(modifier = Modifier.height(12.dp))

                ButtonWithIcon("Github", Icons.Filled.Code) {
                    onAction(HomeAction.OpenRepoUrl)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.showDefaultSMSLabel) {
                Text("This is the default SMS app", style = MaterialTheme.typography.labelSmall)
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                ) {
                    Text("This is not the default SMS app.\nChange to enable additional functionalities such as restoring imported messages and deleting.")
                    Spacer(modifier = Modifier.height(12.dp))
                    ButtonWithIcon("Make default", Icons.AutoMirrored.Filled.Message) {
                        onAction(HomeAction.OpenSetAsDefaultClicked)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(state.versionLabel, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@AppThemePreviews
@Preview
@Composable
private fun HomeDrawerPreview() {
    val darkMode = false
    ChatBackupTheme(overrideDarkMode = darkMode) {
        HomeDrawer(
            HomeDrawerUIState(
                darkMode = darkMode,
                showDefaultSMSLabel = false,
                versionLabel = "Version 1.0.0",
                showComposeAndImport = true,
            ),
            onAction = {})
    }
}