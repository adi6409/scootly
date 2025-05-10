package dev.astroianu.scootly.data

interface ProviderRepository {
    suspend fun getProviders(): List<Provider>
    suspend fun getProviderById(id: String): Provider?
}
