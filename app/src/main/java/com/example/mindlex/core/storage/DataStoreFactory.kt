package com.example.mindlex.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object DataStoreFactory {

    private const val DATA_STORE_FILE_NAME = "mindlex_preferences.pb"

    fun create(appContext: Context): DataStore<Preferences> {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        return PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { appContext.preferencesDataStoreFile(DATA_STORE_FILE_NAME) }
        )
    }
}
