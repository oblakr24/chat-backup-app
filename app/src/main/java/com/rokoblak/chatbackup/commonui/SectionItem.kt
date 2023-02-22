package com.rokoblak.chatbackup.commonui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme
import com.rokoblak.chatbackup.ui.theme.LocalTypography
import com.rokoblak.chatbackup.ui.theme.alpha


@Composable
fun SectionItem(data: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background.alpha(0.9f))
            .padding(start = 20.dp, top = 12.dp, bottom = 8.dp, end = 16.dp),
        text = data,
        textAlign = TextAlign.Start,
        style = LocalTypography.current.bodySemiBold,
    )
}

@Preview
@Composable
fun SectionItemPreview() {
    ChatBackupTheme {
        SectionItem(data = "A")
    }
}