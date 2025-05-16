package dev.astroianu.scootly.data.mock

import com.russhwolf.settings.Settings
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterAPI
import dev.astroianu.scootly.data.ScooterRepository
import dev.astroianu.scootly.storage.SettingsStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MockScooterRepository(
    private val providerRepository: ProviderRepository,
    private val settingsStorage: SettingsStorage,
    private val scooterAPI: ScooterAPI
) : ScooterRepository {

    private var providers: List<Provider>? = emptyList()

    private val scope = CoroutineScope(SupervisorJob())

    fun initialize() {
        scope.launch {
            providers = providerRepository.getProviders(settingsStorage.getSelectedCity())
            Napier.d("Fetched ${providers?.size} providers")
        }
    }

    override suspend fun getScooters(provider: Provider?): List<Scooter> {
        // If provider is null, call getScooters() for every provider and return the merged list
        provider?.let {
            // If provider is not null, call getScooters() for the given provider
            Napier.d("Fetching scooters for provider: ${provider.name}")
            val scooters = scooterAPI.getScooters(provider)
            Napier.d("Fetched ${scooters.size} scooters for provider: ${provider.name}")
            return scooters
        } ?: run {
            // If provider is null, call getScooters() for every provider and return the merged list
            val scooters = mutableListOf<Scooter>()
            Napier.d("Fetching scooters for ${providers?.size} providers")
            providers?.forEach { provider ->
                Napier.d("Fetching scooters for provider: ${provider.name}")
                val providerScooters = scooterAPI.getScooters(provider)
                scooters.addAll(providerScooters)
                Napier.d("Fetched ${providerScooters.size} scooters for provider: ${provider.name}")
                Napier.d("New number of scooters: ${scooters.size}")
            }
            Napier.d("Fetched ${scooters.size} scooters")
            return scooters
        }
    }
}
