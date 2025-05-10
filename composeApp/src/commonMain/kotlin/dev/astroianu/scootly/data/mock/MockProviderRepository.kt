package dev.astroianu.scootly.data.mock

import dev.astroianu.scootly.data.GbfsEndpoints
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderAPI
import dev.astroianu.scootly.data.ProviderFeature
import dev.astroianu.scootly.data.ProviderRepository

class MockProviderRepository(
    private val providerAPI: ProviderAPI
) : ProviderRepository {

    override suspend fun getProviders(): List<Provider> = providerAPI.getProviders()

    override suspend fun getProviderById(id: String): Provider? {
        return providerAPI.getProvider(id)
    }
}
