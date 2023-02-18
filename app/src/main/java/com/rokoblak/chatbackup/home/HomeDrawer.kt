package com.rokoblak.chatbackup.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.commonui.ButtonWithIcon
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography


data class HomeDrawerUIState(
    val darkMode: Boolean?,
    val showDefaultSMSLabel: Boolean,
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
        ButtonWithIcon(modifier = Modifier.padding(horizontal = 16.dp),
            text = "Import",
            icon = Icons.Filled.OpenInNew,
            onClick = { onAction(HomeAction.ImportClicked) })
        Spacer(modifier = Modifier.height(8.dp))
        ButtonWithIcon(modifier = Modifier.padding(horizontal = 16.dp),
            text = "FAQ",
            icon = Icons.Filled.QuestionAnswer,
            onClick = { onAction(HomeAction.FAQClicked) })
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.primaryVariant)
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
                Text("This is the default SMS app", style = LocalTypography.current.captionRegular)
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                ) {
                    Text("This is not the default SMS app.\nChange to enable additional functionalities such as restoring imported messages and deleting.")
                    Spacer(modifier = Modifier.height(12.dp))
                    ButtonWithIcon("Make default", Icons.Filled.Message) {
                        onAction(HomeAction.OpenSetAsDefaultClicked)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(state.versionLabel, style = LocalTypography.current.captionRegular)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
private fun HomeDrawerPreview() {
    val darkMode = false
    ChatBackupTheme(overrideDarkMode = darkMode) {
        HomeDrawer(
            HomeDrawerUIState(
                darkMode = darkMode,
                showDefaultSMSLabel = false,
                versionLabel = "Version 1.0.0"
            ),
            onAction = {})
    }
}