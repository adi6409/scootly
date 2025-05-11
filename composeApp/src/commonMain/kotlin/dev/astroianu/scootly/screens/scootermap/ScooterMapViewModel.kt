package dev.astroianu.scootly.screens.scootermap

import androidx.lifecycle.ViewModel
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*

class ScooterMapViewModel(
    private val scooterRepository: ScooterRepository,
    private val providerRepository: ProviderRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
): ViewModel() {
    // backing state
    private val _allProviders = MutableStateFlow<List<Provider>>(emptyList())
    private val _allScooters  = MutableStateFlow<List<Scooter>>(emptyList())

    // exposed state
    val providers: StateFlow<List<Provider>> = _allProviders.asStateFlow()
    val scooters:  StateFlow<List<Scooter>>  = _allScooters.asStateFlow()

    init {
        // initial load
        coroutineScope.launch {
            _allProviders.value = providerRepository.getProviders()
            _allScooters.value  = scooterRepository.getScooters()
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
}
