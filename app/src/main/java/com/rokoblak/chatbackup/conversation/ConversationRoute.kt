package com.rokoblak.chatbackup.conversation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.rokoblak.chatbackup.navigation.NavRoute
import com.rokoblak.chatbackup.navigation.getOrThrow

private const val KEY_RESOLVED_CONTACT_ID = "key-resolved-chat-id"
private const val KEY_ADDRESS = "key-address"
private const val KEY_IS_IMPORT = "key-is-import"

object ConversationRoute : NavRoute<ConversationViewModel> {

    override val route =
        "chat/{$KEY_ADDRESS}?$KEY_RESOLVED_CONTACT_ID={$KEY_RESOLVED_CONTACT_ID}&$KEY_IS_IMPORT={$KEY_IS_IMPORT}"

    fun get(input: Input): String = route
        .replace("{$KEY_ADDRESS}", input.address)
        .replace("{$KEY_RESOLVED_CONTACT_ID}", input.resolvedContactId ?: "null")
        .replace("{$KEY_IS_IMPORT}", input.isImport.toString())

    fun getIdFrom(savedStateHandle: SavedStateHandle): Input {
        val address = savedStateHandle.getOrThrow<String>(KEY_ADDRESS)
        val resolvedContactId = savedStateHandle.get<String>(KEY_RESOLVED_CONTACT_ID)
        val isImport = savedStateHandle.getOrThrow<Boolean>(KEY_IS_IMPORT)
        return Input(resolvedContactId = resolvedContactId, address = address, isImport = isImport)
    }

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_ADDRESS) { type = NavType.StringType },
        navArgument(KEY_RESOLVED_CONTACT_ID) {
            type = NavType.StringType
            nullable = true
        },
        navArgument(KEY_IS_IMPORT) {
            type = NavType.BoolType
            defaultValue = false
        },
    )

    @Composable
    override fun viewModel(): ConversationViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ConversationViewModel) = ConversationScreen(viewModel)

    data class Input(
        val resolvedContactId: String?, // In case we already have a contact loaded
        val address: String,
        val isImport: Boolean = false,
    )
}