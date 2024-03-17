package com.rokoblak.chatbackup.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.rokoblak.chatbackup.ui.commonui.OptionsPopupMenu
import com.rokoblak.chatbackup.ui.commonui.PopupOptions
import com.rokoblak.chatbackup.ui.commonui.PromptDialog

data class HomeAppbarUIState(
    val hideIcons: Boolean,
    val showEdit: Boolean,
    val showDelete: Boolean,
    val deleteEnabled: Boolean,
    val deleteShowsPrompt: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    state: HomeAppbarUIState,
    onAction: (HomeAction) -> Unit,
    onNavIconClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        title = { Text(text = "Chat Backup") },
        actions = {
            if (state.hideIcons) {
                return@TopAppBar
            }

            var openDialog by remember {
                mutableStateOf(false)
            }

            if (openDialog) {
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = {
                        openDialog = false
                    },
                    properties = PopupProperties()
                ) {
                    Box(
                        Modifier
                            .padding(8.dp)
                            .shadow(8.dp)
                            .wrapContentSize()
                            .background(
                                MaterialTheme.colorScheme.surface,
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

                            Button(onClick = {
                                openDialog = false
                                onAction(HomeAction.OpenSetAsDefaultClicked)
                            }) {
                                Text("Set as default")
                            }
                        }
                    }
                }
            }

            if (state.showDelete) {
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
                                        onAction(HomeAction.SelectAll)
                                    },
                                    PopupOptions.Option("Clear selections") {
                                        openMoreOptions = false
                                        onAction(HomeAction.ClearSelection)
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
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Show more options"
                    )
                }

                var deleteConvsPrompt by remember {
                    mutableStateOf(false)
                }

                if (deleteConvsPrompt) {
                    PromptDialog(
                        title = "Delete conversations",
                        subtitle = "Are you sure you want to delete the selected conversations?",
                        dismiss = {
                            deleteConvsPrompt = false
                        }, onConfirm = {
                            onAction(HomeAction.DeleteClicked)
                        })
                }

                IconButton(enabled = state.deleteEnabled,
                    onClick = {
                        if (state.deleteShowsPrompt) {
                            openDialog = true
                        } else {
                            deleteConvsPrompt = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete selected"
                    )
                }
            }

            if (state.showEdit) {
                IconButton(
                    onClick = {
                        onAction(HomeAction.EditClicked)
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
                        onAction(HomeAction.CloseEditClicked)
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
            IconButton(
                onClick = {
                    onNavIconClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Open Navigation Drawer"
                )
            }
        }
    )
}