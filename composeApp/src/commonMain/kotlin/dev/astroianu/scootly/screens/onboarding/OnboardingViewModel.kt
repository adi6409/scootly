package dev.astroianu.scootly.screens.onboarding

import androidx.lifecycle.ViewModel
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.storage.SettingsStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val providerRepository: ProviderRepository,
    private val settingsStorage: SettingsStorage,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : ViewModel() {
    
    // Backing state
    private val _cities = MutableStateFlow<List<String>>(emptyList())
    private val _selectedCity = MutableStateFlow<String>("")
    private val _isLocationPermissionGranted = MutableStateFlow(false)
    
    // Exposed state
    val cities: StateFlow<List<String>> = _cities.asStateFlow()
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()
    val isLocationPermissionGranted: StateFlow<Boolean> = _isLocationPermissionGranted.asStateFlow()
    
    init {
        // Load cities
        coroutineScope.launch {
            try {
                val availableCities = providerRepository.getCities()
                _cities.value = availableCities
                Napier.d("Loaded ${availableCities.size} cities for onboarding")
                
                // If there's only one city, select it automatically
                if (availableCities.size == 1) {
                    _selectedCity.value = availableCities.first()
                }
            } catch (e: Exception) {
                Napier.e("Error loading cities for onboarding", e)
            }
        }
    }
    
    /**
     * Request location permission
     * In a real app, this would trigger platform-specific permission requests
     */
    suspend fun requestLocationPermission() {
        // In a real app, this would be platform-specific code to request permissions
        // For now, we'll just simulate it being granted
        _isLocationPermissionGranted.value = true
        Napier.d("Location permission granted")
    }
    
    /**
     * Update the selected city
     */
    suspend fun updateSelectedCity(city: String) {
        try {
            _selectedCity.value = city
            settingsStorage.setSelectedCity(city)
            Napier.d("Updated selected city to: $city")
        } catch (e: Exception) {
            Napier.e("Error updating selected city", e)
        }
    }
    
    /**
     * Mark onboarding as completed
     */
    suspend fun completeOnboarding() {
        try {
            settingsStorage.completeOnboarding()
            Napier.d("Onboarding completed")
        } catch (e: Exception) {
            Napier.e("Error completing onboarding", e)
        }
    }
}
