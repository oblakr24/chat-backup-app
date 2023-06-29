package com.rokoblak.chatbackup.faq

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.chatbackup.ui.navigation.NavRoute


object FAQRoute : NavRoute<FAQViewModel> {

    override val route = "faq/"

    @Composable
    override fun viewModel(): FAQViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: FAQViewModel) = FAQScreen(viewModel)
}