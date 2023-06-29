package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.data.model.Contact
import com.rokoblak.chatbackup.data.model.OperationResult
import com.rokoblak.chatbackup.data.repo.ContactsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ContactsFilteringUseCase @Inject constructor(
    private val contactsRepo: ContactsRepository,
) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val searchQueriesDebounced = searchQuery.debounce { q ->
        if (q.isBlank()) 0L else 100L
    }

    fun filteredContacts(): Flow<OperationResult<List<Contact>>?> {
        return contactsRepo.contactsFlow.flatMapLatest { res ->
            when (res) {
                is OperationResult.Done -> {
                    searchQueriesDebounced.map { query ->
                        OperationResult.Done(contactsRepo.filter(res.data, query))
                    }
                }

                is OperationResult.Error -> flowOf(res)
                null -> flowOf(null)
            }
        }
    }

    fun updateQuery(query: String) {
        _searchQuery.update { query }
    }
}
