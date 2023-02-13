package com.rokoblak.chatbackup.commonui

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(text: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, MaterialTheme.colors.onPrimary, RoundedCornerShape(28.dp))
            .background(MaterialTheme.colors.background, RoundedCornerShape(28.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Enter Edit mode",
        )
        TextField(
            value = text,
            onValueChange = onChange,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
            placeholder = {
                Text(text = "Filter conversations", style = LocalTypography.current.subheadRegular)
            },
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colors.background)
        )

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

@Preview
@Composable
fun SearchBarPreview() {
    ChatBackupTheme {
        SearchBar(text = "seach query", onChange = {})
    }
}