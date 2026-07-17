package dev.stefano.enuventory.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.stefano.enuventory.domain.model.AppThemeMode
import dev.stefano.enuventory.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    override fun getThemeMode(): Flow<AppThemeMode> {
        return dataStore.data.map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.Light.name
            try {
                AppThemeMode.valueOf(themeName)
            } catch (e: Exception) {
                AppThemeMode.Light
            }
        }
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }
}
