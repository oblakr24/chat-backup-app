package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

@Composable
fun ButtonWithIcon(text: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        enabled = enabled, modifier = Modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.widthIn(80.dp, 320.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Icon ${icon.name}"
            )
            Text(text = text, maxLines = 1, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Preview
@Composable
fun IconButtonPreviewShort() {
    ChatBackupTheme {
        ButtonWithIcon(text = "Short", icon = Icons.Filled.ImportExport) {}
    }
}

@Preview
@Composable
fun IconButtonPreviewLong() {
    ChatBackupTheme {
        ButtonWithIcon(text = "Long title", icon = Icons.Filled.ImportExport) {}
    }
}