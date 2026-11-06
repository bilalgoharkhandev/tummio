package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesRepository(private val context: Context) {

    companion object {
        val KEY_IS_METRIC = booleanPreferencesKey("is_metric")
        val KEY_USE_AAP = booleanPreferencesKey("use_aap")
        val KEY_SELECTED_BABY_ID = longPreferencesKey("selected_baby_id")
        val KEY_CLINICIAN_TARGET_ML = doublePreferencesKey("clinician_target_ml")
        val KEY_DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode") // SYSTEM, LIGHT, DARK
    }

    val isMetric: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_METRIC] ?: true
    }

    val useAap: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_USE_AAP] ?: false // Default to NHS
    }

    val selectedBabyId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_BABY_ID]
    }

    val clinicianTargetMl: Flow<Double?> = context.dataStore.data.map { preferences ->
        preferences[KEY_CLINICIAN_TARGET_ML]
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_THEME_MODE] ?: "LIGHT"
    }

    suspend fun setMetric(isMetric: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_METRIC] = isMetric
        }
    }

    suspend fun setUseAap(useAap: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USE_AAP] = useAap
        }
    }

    suspend fun setSelectedBabyId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_BABY_ID] = id
        }
    }

    suspend fun setClinicianTargetMl(target: Double?) {
        context.dataStore.edit { preferences ->
            if (target != null) {
                preferences[KEY_CLINICIAN_TARGET_ML] = target
            } else {
                preferences.remove(KEY_CLINICIAN_TARGET_ML)
            }
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME_MODE] = mode
        }
    }
}
