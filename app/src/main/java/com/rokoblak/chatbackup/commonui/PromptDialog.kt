package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rokoblak.chatbackup.ui.theme.LocalTypography


@Composable
fun PromptDialog(
    title: String,
    subtitle: String,
    dismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = {
            Text(text = title, style = LocalTypography.current.titleSemiBold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(subtitle)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    dismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = dismiss
            ) {
                Text("Cancel")
            }
        }
    )
}