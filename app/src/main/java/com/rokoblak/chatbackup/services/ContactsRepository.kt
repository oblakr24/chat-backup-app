package com.rokoblak.chatbackup.services

import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.rokoblak.chatbackup.AppConstants
import com.rokoblak.chatbackup.commonui.PreviewDataUtils.obfuscate
import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.data.PhoneType
import com.rokoblak.chatbackup.di.AppScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val appScope: AppScope,
) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    val contactsFlow: StateFlow<OperationResult<List<Contact>>?> = flow {
        emit(loadContacts())
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), OperationResult.Done(emptyList()))

    fun filter(contacts: List<Contact>, query: String) = if (query.isBlank()) {
        contacts
    } else {
        contacts.filter { it.matches(query) }
    }

    suspend fun resolveContact(number: String): Contact? {
        val contacts = retrieveContacts()
        val matching = contacts.firstOrNull { it.number == number }
        if (matching != null) {
            return matching
        }
        val resolvedName = resolveContactName(number)
        if (resolvedName != null) {
            val contact =  Contact(
                name = resolvedName,
                orgNumber = number,
            ).let { if (AppConstants.OBFUSCATE) it.obfuscate() else it }
            return contact
        }
        return null
    }

    private suspend fun retrieveContacts(): List<Contact> {
        return withTimeoutOrNull(6000L) {
            contactsFlow.mapNotNull { op ->
                (op as? OperationResult.Done)?.data.takeIf { it?.isNotEmpty() == true }
            }.firstOrNull()
        } ?: emptyList()
    }

    private fun resolveContactName(number: String): String? {
        val lookupUri =
            Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
        val c = appScope.appContext.contentResolver
            .query(lookupUri, arrayOf(ContactsContract.Data.DISPLAY_NAME), null, null, null)
            ?: return null
        return try {
            c.moveToFirst()
            if (c.count > 0) {
                val displayName = c.getString(0)
                displayName
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            c.close()
        }
    }

    private fun Contact.matches(query: String) =
        name?.contains(query, ignoreCase = true) == true
                || number.contains(query, ignoreCase = true)

    private suspend fun loadContacts(): OperationResult<List<Contact>> =
        withContext(Dispatchers.IO) {
            val cursor =
                createCursor() ?: return@withContext OperationResult.Error("Could not open cursor")
            OperationResult.Done(
                cursor.parseContacts().let { contacts ->
                    if (AppConstants.OBFUSCATE) contacts.map { it.obfuscate() } else contacts
                }
            )
        }

    private fun createCursor(): Cursor? {
        val builder: Uri.Builder = Phone.CONTENT_URI.buildUpon()
        return appScope.appContext.contentResolver.query(
            builder.build(),
            CONTACTS_PROJ,
            ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
            null,
            ContactsContract.Contacts.STARRED + " DESC, " + Phone.DISPLAY_NAME_PRIMARY + " ASC"
        )
    }

    private fun Cursor.parseColumnIndices(): ContactColumnIndices {
        val nameColumnIndex = getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val phoneColumnIndex = getColumnIndex(Phone.NUMBER)
        val phoneTypeColumnIndex =
            getColumnIndex(Phone.TYPE)
        val imageUri = getColumnIndex(Phone.PHOTO_URI)
        return ContactColumnIndices(
            name = nameColumnIndex,
            phone = phoneColumnIndex,
            avatarUri = imageUri,
            phoneTypeColumnIndex = phoneTypeColumnIndex
        )
    }

    private fun Cursor.parseContacts(): List<Contact> {
        val indices = parseColumnIndices()
        val contacts = mutableListOf<Contact>()

        moveToFirst()
        while (!isAfterLast) {
            parseContact(indices)?.let { contacts.add(it) }
            moveToNext()
        }
        return contacts.distinctBy { it.id }
    }

    private fun Cursor.parseContact(
        indices: ContactColumnIndices,
    ): Contact? {
        val contactName = getString(indices.name)
        val phoneNumber = getString(indices.phone)
        val localThumbUri = getString(indices.avatarUri)
        val phoneType: String? = getString(indices.phoneTypeColumnIndex)

        if (contactName != null && phoneNumber != null) {
            return Contact(
                name = contactName,
                orgNumber = phoneNumber,
                avatarUri = localThumbUri,
                phoneType = phoneType?.let { mapPhoneType(it.toInt()) } ?: PhoneType.HOME
            )
        }
        return null
    }

    private data class ContactColumnIndices(
        val name: Int,
        val phone: Int,
        val avatarUri: Int,
        val phoneTypeColumnIndex: Int,
    )

    private fun mapPhoneType(type: Int) = when (type) {
        Phone.TYPE_ASSISTANT -> PhoneType.ASSISTANT
        Phone.TYPE_CALLBACK -> PhoneType.OTHER
        Phone.TYPE_CAR -> PhoneType.CAR
        Phone.TYPE_COMPANY_MAIN -> PhoneType.WORK
        Phone.TYPE_FAX_HOME -> PhoneType.FAX_HOME
        Phone.TYPE_FAX_WORK -> PhoneType.FAX_WORK
        Phone.TYPE_HOME -> PhoneType.HOME
        Phone.TYPE_ISDN -> PhoneType.OTHER
        Phone.TYPE_MAIN -> PhoneType.MAIN
        Phone.TYPE_MMS -> PhoneType.OTHER
        Phone.TYPE_MOBILE -> PhoneType.MOBILE
        Phone.TYPE_OTHER -> PhoneType.OTHER
        Phone.TYPE_OTHER_FAX -> PhoneType.OTHER
        Phone.TYPE_PAGER -> PhoneType.PAGER
        Phone.TYPE_RADIO -> PhoneType.OTHER
        Phone.TYPE_TELEX -> PhoneType.OTHER
        Phone.TYPE_TTY_TDD -> PhoneType.OTHER
        Phone.TYPE_WORK -> PhoneType.WORK
        Phone.TYPE_WORK_MOBILE -> PhoneType.WORK
        Phone.TYPE_WORK_PAGER -> PhoneType.WORK
        Phone.TYPE_CUSTOM -> PhoneType.OTHER
        else -> PhoneType.OTHER
    }

    companion object {

        val CONTACTS_PROJ =
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                Phone.NUMBER,
                ContactsContract.Contacts.STARRED,
                Phone.TYPE,
                ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,
                Phone.PHOTO_URI
            )
    }
}