package dev.astroianu.scootly.data.remote

import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderAPI
import io.ktor.client.HttpClient

class RemoteProviderAPI(
    private val ktorClient: HttpClient
): ProviderAPI {

    override suspend fun getCities(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getProviders(city: String): List<Provider> {
        TODO("Not yet implemented")
    }

    override suspend fun getProvider(id: String): Provider? {
        TODO("Not yet implemented")
    }
}