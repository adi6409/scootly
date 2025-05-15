package dev.astroianu.scootly.screens.scootermap

import androidx.lifecycle.ViewModel
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterRepository
import dev.astroianu.scootly.storage.SettingsStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlin.native.concurrent.ThreadLocal

class ScooterMapViewModel(
    private val scooterRepository: ScooterRepository,
    private val providerRepository: ProviderRepository,
    private val settingsStorage: SettingsStorage,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
): ViewModel() {
    // backing state
    private val _allProviders = MutableStateFlow<List<Provider>>(emptyList())
    private val _allScooters  = MutableStateFlow<List<Scooter>>(emptyList())
    private val _selectedScooter = MutableStateFlow<Scooter?>(null)
    private val _selectedCity = MutableStateFlow<String>("")

    // exposed state
    val providers: StateFlow<List<Provider>> = _allProviders.asStateFlow()
    val scooters:  StateFlow<List<Scooter>>  = _allScooters.asStateFlow()
    val selectedScooter: StateFlow<Scooter?> = _selectedScooter.asStateFlow()
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    init {
        // Load selected city from settings
        coroutineScope.launch {
            // Get the selected city from storage
            val city = settingsStorage.getSelectedCity()
            _selectedCity.value = city
            
            // Load providers for the selected city
            loadProvidersForCity(city)
            
            // Observe city changes
            settingsStorage.getSelectedCityFlow().collect { newCity ->
                _selectedCity.value = newCity
                loadProvidersForCity(newCity)
            }
        }
    }
    
    /**
     * Load providers and scooters for the given city
     */
    private suspend fun loadProvidersForCity(city: String) {
        val providers = providerRepository.getProviders(city)
        _allProviders.value = providers
        Napier.d("Fetched ${providers.size} providers for city: $city")
        
        Napier.d("Fetching scooters for ${providers.size} providers")
        val scooters = scooterRepository.getScooters()
        _allScooters.value = scooters
        Napier.d("Fetched ${scooters.size} scooters")
        
        // Log the first few scooters for debugging
        if (scooters.isNotEmpty()) {
            scooters.take(3).forEach { scooter ->
                Napier.d("Scooter: ${scooter.id}, provider: ${scooter.providerName}, lat: ${scooter.latitude}, lng: ${scooter.longitude}")
            }
        } else {
            Napier.d("No scooters found!")
        }
    }

    /**
     * Filter the scooter list by a provider (or show all if null).
     */
    fun filterByProvider(provider: Provider?) {
        coroutineScope.launch {
            val fullList = scooterRepository.getScooters()
            _allScooters.value = if (provider == null) {
                fullList
            } else {
                fullList.filter { it.providerName == provider.name }
            }
        }
    }

    fun refreshScooters(provider: Provider?) {
        coroutineScope.launch {
            val fullList = scooterRepository.getScooters(provider)
            _allScooters.value = fullList
        }
    }
    
    /**
     * Select a scooter and log its details
     */
    fun selectScooter(scooter: Scooter?) {
        _selectedScooter.value = scooter
        scooter?.let {
            Napier.d("Scooter selected: ${it.providerName} (ID: ${it.id})")
        }
    }
    
    /**
     * Update the selected city
     */
    fun updateSelectedCity(city: String) {
        coroutineScope.launch {
            settingsStorage.setSelectedCity(city)
        }
    }
}
