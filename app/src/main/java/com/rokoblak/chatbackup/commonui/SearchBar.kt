package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(text: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    Surface(modifier = modifier.fillMaxWidth()
        .height(50.dp),
        elevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colors.onPrimary),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colors.background) {
        Row(
            modifier = Modifier.background(MaterialTheme.colors.background),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Enter Edit mode",
            )
            TextField(
                value = text,
                onValueChange = onChange,
                maxLines = 1,
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                placeholder = {
                    Text(
                        text = "Filter conversations",
                        style = LocalTypography.current.subheadRegular
                    )
                },
                textStyle = LocalTypography.current.subheadRegular,
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colors.background)
            )

            if (text.isNotBlank()) {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search query"
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
        SearchBar(text = "search query", onChange = {})
    }
}