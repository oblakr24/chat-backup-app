package com.rokoblak.chatbackup.importfile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.chatbackup.navigation.NavRoute


object ImportRoute : NavRoute<ImportFileViewModel> {

    override val route = "import/"

    @Composable
    override fun viewModel(): ImportFileViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ImportFileViewModel) = ImportScreen(viewModel)
}