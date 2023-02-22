package com.rokoblak.chatbackup.conversation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography
import com.rokoblak.chatbackup.ui.theme.alpha


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputBar(
    input: String,
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit,
    onChange: (String) -> Unit,
) {
    val bgColor = MaterialTheme.colors.primaryVariant
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        elevation = 4.dp,
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
                    backgroundColor = bgColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Text message",
                        style = LocalTypography.current.subheadRegular
                    )
                },
                textStyle = LocalTypography.current.subheadRegular,
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
                        contentDescription = "Clear input"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchBarPreview() {
    ChatBackupTheme {
        InputBar(input = "New SMS to be sent", onChange = {}, onSend = {

        })
    }
}