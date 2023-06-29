package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

data class PopupOptions(
    val options: List<Option>
) {
    data class Option(val text: String, val action: () -> Unit)
}

@Composable
fun OptionsPopupMenu(
    options: PopupOptions,
) {
    Box(
        Modifier
            .wrapContentSize().padding(8.dp).shadow(8.dp)
            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
            options.options.forEach { (text, action) ->
                Text(
                    text = text,
                    modifier = Modifier
                        .clickable {
                            action()
                        }
                        .padding(vertical = 10.dp, horizontal = 14.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OptionsPopupPreview() {
    ChatBackupTheme {
        OptionsPopupMenu(
            PopupOptions(
                options = listOf(
                    PopupOptions.Option("First") {},
                    PopupOptions.Option("Second") {},
                    PopupOptions.Option("Third") {}
                )
            )
        )
    }
}
