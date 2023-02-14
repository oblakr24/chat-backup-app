package com.rokoblak.chatbackup.smsutils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokoblak.chatbackup.ui.theme.ChatBackupTheme

class ComposeSMSActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatBackupTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ComposeSMSScreen()
                }
            }
        }
    }
}

@Composable
fun ComposeSMSScreen() {
    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text("This app is not yet setup to compose new messages.\nPlease use another app to do so.")
    }
}

@Preview
@Composable
fun ComposeSMSScreenPreview() {
    ChatBackupTheme {
        ComposeSMSScreen()
    }
}