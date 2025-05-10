package dev.astroianu.scootly.data

interface ProviderAPI {
    suspend fun getProviders(): List<Provider>
    suspend fun getProvider(id: String): Provider?
}