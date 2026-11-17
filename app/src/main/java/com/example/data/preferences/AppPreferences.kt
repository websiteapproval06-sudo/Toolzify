package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.toolzifyDataStore: DataStore<Preferences> by preferencesDataStore(name = "toolzify_prefs")

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeAccent: ThemeAccent = ThemeAccent.FOREST_GOLD,
    val appLanguage: String = "en",
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val showIslamicTools: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val dailyWaterTargetMl: Int = 2500,
    val monthlyBudgetLimit: Double = 2000.0,
    val selectedCityId: String = "makkah",
    val prayerMethodId: String = "mwl",
    val tasbeehCount: Int = 0,
    val tasbeehTarget: Int = 33,
    val tasbeehPhraseIndex: Int = 0
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_ACCENT = stringPreferencesKey("theme_accent")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val SHOW_ISLAMIC_TOOLS = booleanPreferencesKey("show_islamic_tools")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DAILY_WATER_TARGET = intPreferencesKey("daily_water_target_ml")
        val MONTHLY_BUDGET_LIMIT = doublePreferencesKey("monthly_budget_limit")
        val SELECTED_CITY_ID = stringPreferencesKey("selected_city_id")
        val PRAYER_METHOD_ID = stringPreferencesKey("prayer_method_id")
        val TASBEEH_COUNT = intPreferencesKey("tasbeeh_count")
        val TASBEEH_TARGET = intPreferencesKey("tasbeeh_target")
        val TASBEEH_PHRASE_INDEX = intPreferencesKey("tasbeeh_phrase_index")
    }

    val preferencesFlow: Flow<UserPreferences> = context.toolzifyDataStore.data.map { prefs ->
        UserPreferences(
            themeMode = ThemeMode.fromId(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.id),
            themeAccent = ThemeAccent.fromId(prefs[Keys.THEME_ACCENT] ?: ThemeAccent.FOREST_GOLD.id),
            appLanguage = prefs[Keys.APP_LANGUAGE] ?: "en",
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: true,
            showIslamicTools = prefs[Keys.SHOW_ISLAMIC_TOOLS] ?: true,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            dailyWaterTargetMl = prefs[Keys.DAILY_WATER_TARGET] ?: 2500,
            monthlyBudgetLimit = prefs[Keys.MONTHLY_BUDGET_LIMIT] ?: 2000.0,
            selectedCityId = prefs[Keys.SELECTED_CITY_ID] ?: "makkah",
            prayerMethodId = prefs[Keys.PRAYER_METHOD_ID] ?: "mwl",
            tasbeehCount = prefs[Keys.TASBEEH_COUNT] ?: 0,
            tasbeehTarget = prefs[Keys.TASBEEH_TARGET] ?: 33,
            tasbeehPhraseIndex = prefs[Keys.TASBEEH_PHRASE_INDEX] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.toolzifyDataStore.edit { it[Keys.THEME_MODE] = mode.id }
    }

    suspend fun setThemeAccent(accent: ThemeAccent) {
        context.toolzifyDataStore.edit { it[Keys.THEME_ACCENT] = accent.id }
    }

    suspend fun setAppLanguage(code: String) {
        context.toolzifyDataStore.edit { it[Keys.APP_LANGUAGE] = code }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.toolzifyDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.toolzifyDataStore.edit { it[Keys.ANIMATIONS_ENABLED] = enabled }
    }

    suspend fun setShowIslamicTools(show: Boolean) {
        context.toolzifyDataStore.edit { it[Keys.SHOW_ISLAMIC_TOOLS] = show }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.toolzifyDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDailyWaterTarget(ml: Int) {
        context.toolzifyDataStore.edit { it[Keys.DAILY_WATER_TARGET] = ml.coerceIn(500, 6000) }
    }

    suspend fun setMonthlyBudgetLimit(limit: Double) {
        context.toolzifyDataStore.edit { it[Keys.MONTHLY_BUDGET_LIMIT] = limit.coerceAtLeast(0.0) }
    }

    suspend fun setSelectedCityId(cityId: String) {
        context.toolzifyDataStore.edit { it[Keys.SELECTED_CITY_ID] = cityId }
    }

    suspend fun setPrayerMethodId(methodId: String) {
        context.toolzifyDataStore.edit { it[Keys.PRAYER_METHOD_ID] = methodId }
    }

    suspend fun updateTasbeeh(count: Int, target: Int, phraseIndex: Int) {
        context.toolzifyDataStore.edit {
            it[Keys.TASBEEH_COUNT] = count
            it[Keys.TASBEEH_TARGET] = target
            it[Keys.TASBEEH_PHRASE_INDEX] = phraseIndex
        }
    }

    suspend fun clearAllPreferences() {
        context.toolzifyDataStore.edit { prefs ->
            prefs.clear()
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}
