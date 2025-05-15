package dev.astroianu.scootly.screens.scootermap

import androidx.lifecycle.ViewModel
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlin.native.concurrent.ThreadLocal

class ScooterMapViewModel(
    private val scooterRepository: ScooterRepository,
    private val providerRepository: ProviderRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
): ViewModel() {
    // backing state
    private val _allProviders = MutableStateFlow<List<Provider>>(emptyList())
    private val _allScooters  = MutableStateFlow<List<Scooter>>(emptyList())
    private val _selectedScooter = MutableStateFlow<Scooter?>(null)

    // exposed state
    val providers: StateFlow<List<Provider>> = _allProviders.asStateFlow()
    val scooters:  StateFlow<List<Scooter>>  = _allScooters.asStateFlow()
    val selectedScooter: StateFlow<Scooter?> = _selectedScooter.asStateFlow()

    init {
        // initial load
        coroutineScope.launch {
            val providers = providerRepository.getProviders("Tel Aviv")
            _allProviders.value = providers
            Napier.d("Fetched ${providers.size} providers")
            
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
}
