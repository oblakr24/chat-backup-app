package com.rokoblak.chatbackup.conversation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.rokoblak.chatbackup.navigation.NavRoute
import com.rokoblak.chatbackup.navigation.getOrThrow

private const val KEY_CHAT_ID = "key-chat-id"

object ConversationRoute : NavRoute<ConversationViewModel> {

    override val route = "chat/{$KEY_CHAT_ID}/"

    fun get(contactId: String): String = route.replace("{$KEY_CHAT_ID}", contactId)

    fun getIdFrom(savedStateHandle: SavedStateHandle) =
        savedStateHandle.getOrThrow<String>(KEY_CHAT_ID)

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_CHAT_ID) { type = NavType.StringType })

    @Composable
    override fun viewModel(): ConversationViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ConversationViewModel) = ConversationScreen(viewModel)
}