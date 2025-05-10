package dev.astroianu.scootly.screens.scootermap

import androidx.lifecycle.ViewModel
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScooterMapViewModel(
    private val scooterRepository: ScooterRepository,
    private val providerRepository: ProviderRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
): ViewModel() {
//    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
//    val providers: StateFlow<List<Provider>> = _providers.asStateFlow()
//
//    private val _selectedProvider = MutableStateFlow<Provider?>(null)
//    val selectedProvider: StateFlow<Provider?> = _selectedProvider.asStateFlow()
    // TODO: Add providers filtering using a funnel button


    private val _scooters = MutableStateFlow<List<Scooter>>(emptyList())
    val scooters: StateFlow<List<Scooter>> = _scooters.asStateFlow()

    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers: StateFlow<List<Provider>> = _providers.asStateFlow()

    init {
        coroutineScope.launch {
            val providerList = providerRepository.getProviders()
            _providers.value = providerList
            val scooterList = scooterRepository.getScooters()
            _scooters.value = scooterList
        }
    }
}