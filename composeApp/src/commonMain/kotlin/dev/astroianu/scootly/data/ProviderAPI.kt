package dev.astroianu.scootly.data

interface ProviderAPI {
    suspend fun getCities(): List<String>
    suspend fun getProviders(city: String): List<Provider>
    suspend fun getProvider(id: String): Provider?
}