package com.rokoblak.chatbackup.feature.createchat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateChatScreen(viewModel: CreateChatViewModel = hiltViewModel()) {

    val state = viewModel.uiState.collectAsState().value
    val query = viewModel.queries.collectAsState().value

    Effects(viewModel)

    CreateChatContent(state = state, searchQuery = query, onNavigateUp = {
        viewModel.navigateUp()
    }, onAction = {
        viewModel.handleAction(it)
    })
}

@Composable
private fun Effects(viewModel: CreateChatViewModel) {
    LaunchedEffect(viewModel) {
        viewModel.effects.consumeEvents { effect ->
            when (effect) {
                else -> Unit
            }
        }
    }
}