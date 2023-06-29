package com.rokoblak.chatbackup.export

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokoblak.chatbackup.ui.navigation.NavRoute


object ExportRoute : NavRoute<ExportViewModel> {

    override val route = "export/"

    @Composable
    override fun viewModel(): ExportViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ExportViewModel) = ExportScreen(viewModel)
}