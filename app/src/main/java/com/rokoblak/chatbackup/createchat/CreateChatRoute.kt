package com.rokoblak.chatbackup.createchat

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.chatbackup.ui.navigation.NavRoute


object CreateChatRoute : NavRoute<CreateChatViewModel> {

    override val route = "createChat/"

    @Composable
    override fun viewModel(): CreateChatViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: CreateChatViewModel) = CreateChatScreen(viewModel)
}