package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography
import com.rokoblak.chatbackup.ui.theme.alpha

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    text: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(50.dp).clip(RoundedCornerShape(25.dp)),
        elevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colors.onPrimary.alpha(0.5f)),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colors.background
    ) {
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
                        text = placeholder,
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
        SearchBar(text = "search query", placeholder = "Filter conversations", onChange = {})
    }
}