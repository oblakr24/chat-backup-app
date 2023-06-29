package com.rokoblak.chatbackup.importfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.rokoblak.chatbackup.ui.commonui.ButtonWithIcon
import com.rokoblak.chatbackup.ui.commonui.OptionsPopupMenu
import com.rokoblak.chatbackup.ui.commonui.PopupOptions
import com.rokoblak.chatbackup.ui.commonui.PromptDialog
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.alpha

data class ImportTopToolbarUIState(
    val showEdit: Boolean,
    val downloadShowsPrompt: Boolean,
    val downloadEnabled: Boolean,
    val deleteEnabled: Boolean,
)

@Composable
fun ImportTopToolbar(
    state: ImportTopToolbarUIState,
    onAction: (ImportAction) -> Unit,
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.primaryVariant.alpha(0.9f),
        elevation = 0.dp,
        title = { },
        actions = {
            var openSMSDefaultPrompt by remember {
                mutableStateOf(false)
            }

            if (openSMSDefaultPrompt) {
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = {
                        openSMSDefaultPrompt = false
                    },
                    properties = PopupProperties()
                ) {
                    Box(
                        Modifier
                            .padding(8.dp)
                            .shadow(8.dp)
                            .wrapContentSize()
                            .background(
                                MaterialTheme.colors.surface,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "To perform this action, this app needs to be set as the default SMS app. " +
                                        "Press the button below to do so. You can change this any time in the settings.",
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                                    .widthIn(20.dp, 220.dp)
                            )
                            val owner = LocalContext.current
                            ButtonWithIcon("Set as default", Icons.Filled.Message) {
                                openSMSDefaultPrompt = false
                                onAction(ImportAction.OpenSetAsDefaultClicked(owner))
                            }
                        }
                    }
                }
            }

            var openMoreOptions by remember {
                mutableStateOf(false)
            }

            if (openMoreOptions) {
                Popup(
                    alignment = Alignment.TopEnd,
                    onDismissRequest = {
                        openMoreOptions = false
                    },
                    properties = PopupProperties()
                ) {
                    OptionsPopupMenu(
                        options = PopupOptions(
                            options = listOf(
                                PopupOptions.Option("Select all") {
                                    openMoreOptions = false
                                    onAction(ImportAction.SelectAll)
                                },
                                PopupOptions.Option("Clear selections") {
                                    openMoreOptions = false
                                    onAction(ImportAction.ClearSelection)
                                },
                            )
                        ),
                    )
                }
            }

            if (state.showEdit.not()) {
                IconButton(
                    onClick = {
                        openMoreOptions = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Show more options"
                    )
                }
                IconButton(enabled = state.deleteEnabled,
                    onClick = {
                        onAction(ImportAction.DeleteClicked)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete selected"
                    )
                }
            }

            var openDownloadPrompt by remember {
                mutableStateOf(false)
            }

            if (openDownloadPrompt) {
                PromptDialog(
                    title = "Save messages",
                    subtitle = "Are you sure you want to save the selected conversations to the device?",
                    dismiss = {
                        openDownloadPrompt = false
                    }, onConfirm = {
                        onAction(ImportAction.DownloadConfirmed)
                    })
            }

            IconButton(enabled = state.downloadEnabled,
                onClick = {
                    if (state.downloadShowsPrompt) {
                        openSMSDefaultPrompt = true
                    } else {
                        openDownloadPrompt = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download"
                )
            }

            if (state.showEdit) {
                IconButton(
                    onClick = {
                        onAction(ImportAction.EditClicked)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Enter Edit mode"
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        onAction(ImportAction.CloseEditClicked)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditOff,
                        contentDescription = "Close Edit mode"
                    )
                }
            }
        },
        navigationIcon = {
            var openMoreOptions by remember {
                mutableStateOf(false)
            }

            if (openMoreOptions) {
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = {
                        openMoreOptions = false
                    },
                    properties = PopupProperties()
                ) {
                    OptionsPopupMenu(
                        options = PopupOptions(
                            options = listOf(
                                PopupOptions.Option("Open new JSON") {
                                    openMoreOptions = false
                                    onAction(ImportAction.ImportJSONClicked)
                                },
                            )
                        ),
                    )
                }
            }
            IconButton(
                onClick = {
                    openMoreOptions = true
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = "Show open file submenu"
                )
            }
        }
    )
}

@Preview
@Composable
fun ImportTopAppBarPreview() {
    val state = ImportTopToolbarUIState(
        showEdit = true,
        downloadShowsPrompt = true,
        downloadEnabled = true,
        deleteEnabled = true,
    )
    ChatBackupTheme {
        ImportTopToolbar(state = state, onAction = {})
    }
}