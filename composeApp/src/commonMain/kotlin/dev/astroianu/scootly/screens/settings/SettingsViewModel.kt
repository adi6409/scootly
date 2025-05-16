package dev.astroianu.scootly.screens.settings

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

class SettingsViewModel(
    private val providerRepository: ProviderRepository,
    private val settingsStorage: SettingsStorage,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : ViewModel() {
    
    // Backing state
    private val _cities = MutableStateFlow<List<String>>(emptyList())
    private val _selectedCity = MutableStateFlow<String>("")
    
    // Exposed state
    val cities: StateFlow<List<String>> = _cities.asStateFlow()
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()
    
    init {
        // Load cities and selected city
        coroutineScope.launch {
            try {
                val availableCities = providerRepository.getCities()
                _cities.value = availableCities.sorted()
                Napier.d("Loaded ${availableCities.size} cities")
                
                // Get selected city from storage
                val city = settingsStorage.getSelectedCity()
                _selectedCity.value = city
                Napier.d("Current selected city: $city")
                
                // Observe city changes
                settingsStorage.getSelectedCityFlow().collect { newCity ->
                    _selectedCity.value = newCity
                    Napier.d("Selected city changed to: $newCity")
                }
            } catch (e: Exception) {
                Napier.e("Error loading cities", e)
            }
        }
    }
    
    /**
     * Update the selected city
     */
    suspend fun updateSelectedCity(city: String) {
        try {
            settingsStorage.setSelectedCity(city)
            Napier.d("Updated selected city to: $city")
        } catch (e: Exception) {
            Napier.e("Error updating selected city", e)
        }
    }
}
