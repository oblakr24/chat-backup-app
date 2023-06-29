package com.rokoblak.chatbackup.ui.commonui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DetailsContent(
    title: String = "",
    onBackPressed: () -> Unit,
    onIconClicked: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colors.surface)
    ) {
        DetailsHeader(
            text = title,
            rightButtonText = null,
            onIconPressed = onIconClicked,
            onBackPressed = { onBackPressed() }
        )
        this.content()
    }
}