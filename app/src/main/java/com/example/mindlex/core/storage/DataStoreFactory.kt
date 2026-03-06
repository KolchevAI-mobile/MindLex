package com.example.mindlex.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import java.io.IOException

object DataStoreFactory {

    private const val DATA_STORE_FILE_NAME = "mindlex_preferences.pb"

    fun create(appContext: Context): DataStore<Preferences> {
        val scope = CoroutineScope(SupervisorJob())

        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile(DATA_STORE_FILE_NAME) }
        )
    }
}