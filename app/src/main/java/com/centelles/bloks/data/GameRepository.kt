package com.centelles.bloks.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class GameRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val HIGHSCORE = intPreferencesKey("high_score")
    private val COINS = intPreferencesKey("coins")
    private val REMOVE_ADS = booleanPreferencesKey("remove_ads")
    private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    private val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
    private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    private val ACTIVE_THEME = intPreferencesKey("active_theme")
    private val UNLOCKED_THEMES = intPreferencesKey("unlocked_themes")

    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HIGHSCORE] ?: 0
    }

    val coinsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[COINS] ?: 0
    }

    val removeAdsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMOVE_ADS] ?: false
    }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED] ?: true
    }

    val musicEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MUSIC_ENABLED] ?: true
    }

    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val activeThemeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_THEME] ?: 0
    }

    val unlockedThemesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[UNLOCKED_THEMES] ?: 1 // Bitmask, theme 0 is always unlocked (1)
    }

    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[HIGHSCORE] ?: 0
            if (score > current) {
                preferences[HIGHSCORE] = score
            }
        }
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[COINS] ?: 0
            preferences[COINS] = current + amount
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val current = preferences[COINS] ?: 0
            if (current >= amount) {
                preferences[COINS] = current - amount
                success = true
            } else {
                success = false
            }
        }
        return success
    }

    suspend fun setRemoveAds(purchased: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMOVE_ADS] = purchased
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setActiveTheme(themeId: Int) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_THEME] = themeId
        }
    }

    suspend fun unlockTheme(themeId: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[UNLOCKED_THEMES] ?: 1
            preferences[UNLOCKED_THEMES] = current or (1 shl themeId)
        }
    }
}
