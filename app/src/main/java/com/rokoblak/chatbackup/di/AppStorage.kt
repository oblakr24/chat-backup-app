package com.rokoblak.chatbackup.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.rokoblak.chatbackup.services.JsonSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStorage @Inject constructor(
    @ApplicationContext context: Context,
    private val serializer: JsonSerializer
) {

    private val store = CacheDataStore(context)

    fun prefsFlow(): Flow<Prefs> {
        return flow {
            ensurePopulated()
            emitAll(store.flow(KEY_SETTINGS).map {
                serializer.decode(Prefs.serializer(), it)
            })
        }
    }

    @kotlinx.serialization.Serializable
    data class Prefs(
        val darkMode: Boolean
    )

    suspend fun updateDarkMode(enabled: Boolean) {
        update {
            copy(darkMode = enabled)
        }

    }

    private suspend fun ensurePopulated() {
        if (!store.isKeyStored(KEY_SETTINGS)) {
            defaultSettings.store()
        }
    }

    private suspend fun Prefs.store() {
        store.store(KEY_SETTINGS, serializer.encode(Prefs.serializer(), this))
    }

    private suspend fun update(block: suspend Prefs.() -> Prefs) {
        ensurePopulated()
        val encoded = store.retrieve(KEY_SETTINGS) ?: return
        val current = serializer.decode(Prefs.serializer(), encoded)
        val new = block(current)
        new.store()
    }

    companion object {
        // Ensure that the key is updated when the model is updated. We do not need to handle migrations as this is non-critical data.
        private const val KEY_SETTINGS = "settings_v1"

        val defaultSettings = Prefs(
            darkMode = false,
        )
    }

}

class CacheDataStore(private val appContext: Context) {

    private val store = PreferenceDataStoreFactory.create(
        produceFile = {
            appContext.preferencesDataStoreFile(Names.DATASTORE_PREFS)
        }
    )

    private fun String.key() = stringPreferencesKey(this)

    suspend fun retrieve(key: String): String? {
        return store.data.firstOrNull()?.get(key.key())
    }

    suspend fun isKeyStored(key: String) = store.data.firstOrNull()?.contains(key.key()) ?: false

    fun flow(key: String) = store.data.mapNotNull { it[key.key()] }

    suspend fun store(key: String, data: String) {
        store.edit {
            it[key.key()] = data
        }
    }

    suspend fun remove(key: String) {
        store.edit {
            it.remove(key.key())
        }
    }

    suspend fun clear() {
        store.edit {
            it.clear()
        }
    }
}