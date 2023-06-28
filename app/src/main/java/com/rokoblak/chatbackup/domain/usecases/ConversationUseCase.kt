package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.data.Conversation
import com.rokoblak.chatbackup.services.ConversationsRepo
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class ConversationUseCase @Inject constructor(
    private val repo: ConversationsRepo,
    private val contactsUseCase: RetrieveContactUseCase,
) {

    fun conversationFor(contactId: String?, number: String, isImport: Boolean) = flow {
        val matching = contactsUseCase.resolveContact(number)
        val contact = matching ?: Contact(
            name = null,
            orgNumber = number,
        )
        if (isImport) {
            val conv =
                repo.importedConversations?.resolveConv(contactId = contactId, number = number)
            emit(conv ?: Conversation(contact, emptyList()))
        } else {
            emitAll(repo.deviceConvsFlow.mapNotNull {
                it?.resolveConv(contactId = contactId, number = number) ?: Conversation(
                    contact,
                    emptyList()
                )
            })
        }
    }
}