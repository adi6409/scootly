package dev.astroianu.scootly.screens.list

import androidx.lifecycle.ViewModel
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterRepository
import dev.astroianu.scootly.data.mock.MockProviderRepository
import dev.astroianu.scootly.data.mock.MockScooterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScooterListViewModel(
    private val providerRepository: ProviderRepository,
    private val scooterRepository: ScooterRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
): ViewModel() {
    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers: StateFlow<List<Provider>> = _providers.asStateFlow()

    private val _selectedProvider = MutableStateFlow<Provider?>(null)
    val selectedProvider: StateFlow<Provider?> = _selectedProvider.asStateFlow()

    private val _scooters = MutableStateFlow<List<Scooter>>(emptyList())
    val scooters: StateFlow<List<Scooter>> = _scooters.asStateFlow()

    init {
        coroutineScope.launch {
            val providerList = providerRepository.getProviders()
            _providers.value = providerList
            if (providerList.isNotEmpty()) {
                selectProvider(providerList.first().id)
            }
        }
    }

    fun selectProvider(providerId: String) {
        coroutineScope.launch {
            if (providerId == "") {
                // Select all providers
                _selectedProvider.value = null
                _scooters.value = scooterRepository.getScooters()
            } else {
                val provider = providerRepository.getProviderById(providerId)
                _selectedProvider.value = provider
                if (provider != null) {
                    _scooters.value = scooterRepository.getScooters(provider)
                } else {
                    _scooters.value = emptyList()
                }
            }
        }
    }
}
