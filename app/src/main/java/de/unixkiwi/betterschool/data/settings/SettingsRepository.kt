package de.unixkiwi.betterschool.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.unixkiwi.betterschool.core.models.AppTheme
import de.unixkiwi.betterschool.core.models.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("app_theme")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
    }

    val settingsFlow: Flow<Settings> = dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME] ?: AppTheme.SYSTEM.name
            val theme = try {
                AppTheme.valueOf(themeName)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse theme: $themeName")
                AppTheme.SYSTEM
            }
            val useDynamicColors = preferences[PreferencesKeys.USE_DYNAMIC_COLORS] ?: true

            Settings(
                theme = theme,
                useDynamicColors = useDynamicColors
            )
        }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun setUseDynamicColors(useDynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLORS] = useDynamicColors
        }
    }
}
