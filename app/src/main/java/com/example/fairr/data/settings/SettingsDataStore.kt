package com.example.fairr.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    
    private object PreferencesKeys {
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
    }

    val defaultCurrency: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_CURRENCY] ?: "PHP"
        }

    suspend fun setDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CURRENCY] = currency
        }
    }

    /**
     * Clear all settings and reset to defaults
     * This is used for complete sign-out to force fresh configuration
     */
    suspend fun clearAllSettings() {
        context.dataStore.edit { preferences ->
            // Clear all settings - they will revert to defaults
            preferences.remove(PreferencesKeys.DEFAULT_CURRENCY)
        }
    }
} 