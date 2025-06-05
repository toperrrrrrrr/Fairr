package com.example.fairr.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
    
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
        
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
} 