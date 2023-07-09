package com.rokoblak.chatbackup.conversation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.AppThemePreviews
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InputBar(
    input: String,
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit,
    onChange: (String) -> Unit,
) {
    val bgColor = MaterialTheme.colorScheme.primaryContainer
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color.Transparent),
        shape = RoundedCornerShape(25.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.background(bgColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = input,
                onValueChange = onChange,
                maxLines = 1,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = bgColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Text message",
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                textStyle = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .weight(1f)
                    .background(bgColor)
            )

            if (input.isNotBlank()) {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onSend(input)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentDescription = "Clear input"
                    )
                }
            }
        }
    }
}

@AppThemePreviews
@Preview
@Composable
fun SearchBarPreview() {
    ChatBackupTheme {
        InputBar(input = "New SMS to be sent", onChange = {}, onSend = {

        })
    }
}