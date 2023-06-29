package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.AppConstants
import com.rokoblak.chatbackup.ui.commonui.PreviewDataUtils.obfuscate
import com.rokoblak.chatbackup.data.model.Contact
import com.rokoblak.chatbackup.data.repo.ContactsRepository
import javax.inject.Inject

class RetrieveContactUseCase @Inject constructor(
    private val repo: ContactsRepository,
) {

    suspend fun resolveContact(number: String): Contact? {
        val contacts = repo.retrieveContacts()
        val matching = contacts.firstOrNull { it.number == number }
        if (matching != null) {
            return matching
        }
        val resolvedName = repo.resolveContactName(number)
        if (resolvedName != null) {
            val contact =  Contact(
                name = resolvedName,
                orgNumber = number,
            ).let { if (AppConstants.OBFUSCATE) it.obfuscate() else it }
            return contact
        }
        return null
    }
}