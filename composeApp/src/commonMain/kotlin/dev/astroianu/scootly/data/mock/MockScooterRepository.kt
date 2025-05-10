package dev.astroianu.scootly.data.mock

import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterAPI
import dev.astroianu.scootly.data.ScooterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MockScooterRepository(
    private val providerRepository: ProviderRepository,
    private val scooterAPI: ScooterAPI
) : ScooterRepository {

    private var providers: List<Provider>? = emptyList()

    private val scope = CoroutineScope(SupervisorJob())

    fun initialize() {
        scope.launch {
            providers = providerRepository.getProviders()
        }
    }

    override suspend fun getScooters(provider: Provider?): List<Scooter> {
        // If provider is null, call getScooters() for every provider and return the merged list
        provider?.let {
            // If provider is not null, call getScooters() for the given provider
            return scooterAPI.getScooters(provider)
        } ?: run {
            // If provider is null, call getScooters() for every provider and return the merged list
            val scooters = mutableListOf<Scooter>()
            providers?.forEach { provider ->
                scooters.addAll(scooterAPI.getScooters(provider))
            }
            return scooters
        }
    }
}
