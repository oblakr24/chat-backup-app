package com.rokoblak.chatbackup.domain.usecases

import com.rokoblak.chatbackup.di.AppStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DarkModeToggleUseCase @Inject constructor(
    private val storage: AppStorage,
) {

    fun darkModeEnabled(): Flow<Boolean?> = storage.prefsFlow().map { it.darkMode }

    suspend fun setDarkMode(enabled: Boolean) {
        storage.updateDarkMode(enabled)
    }
}
