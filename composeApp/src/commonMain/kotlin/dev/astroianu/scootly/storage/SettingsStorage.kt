package dev.astroianu.scootly.storage

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Storage class for app settings and preferences
 */
class SettingsStorage(private val settings: FlowSettings) {

    companion object {
        private const val KEY_SELECTED_CITY = "selected_city"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val DEFAULT_CITY = "Tel Aviv"
    }

    /**
     * Get the currently selected city
     */
    suspend fun getSelectedCity(): String {
        return settings.getStringOrNull(KEY_SELECTED_CITY) ?: DEFAULT_CITY
    }

    /**
     * Set the selected city
     */
    suspend fun setSelectedCity(city: String) {
        settings.putString(KEY_SELECTED_CITY, city)
    }

    /**
     * Check if onboarding has been completed
     */
    suspend fun isOnboardingCompleted(): Boolean {
        return settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Mark onboarding as completed
     */
    suspend fun completeOnboarding() {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, true)
    }

    /**
     * Get a flow of the selected city that updates when the city changes
     */
    fun getSelectedCityFlow(): Flow<String> {
        return settings.getStringFlow(KEY_SELECTED_CITY, DEFAULT_CITY)
    }

    /**
     * Get a flow of the onboarding completion status
     */
    fun getOnboardingCompletedFlow(): Flow<Boolean> {
        return settings.getBooleanFlow(KEY_ONBOARDING_COMPLETED, false)
    }
}

/**
 * Factory function to create a SettingsStorage instance
 */
@OptIn(ExperimentalSettingsApi::class)
fun createSettingsStorage(): SettingsStorage {
    // Create platform-specific settings
    val settings = Settings() as ObservableSettings
    return SettingsStorage(settings.toFlowSettings())
}
