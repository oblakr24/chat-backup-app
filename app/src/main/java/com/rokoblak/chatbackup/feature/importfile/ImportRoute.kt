package com.rokoblak.chatbackup.feature.importfile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.chatbackup.ui.navigation.NavRoute


object ImportRoute : NavRoute<ImportFileViewModel> {

    override val route = "import/"

    @Composable
    override fun viewModel(): ImportFileViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ImportFileViewModel) = ImportScreen(viewModel)
}