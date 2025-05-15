package dev.astroianu.scootly.data

interface ProviderRepository {
    suspend fun getCities(): List<String>
    suspend fun getProviders(city: String): List<Provider>
    suspend fun getProviderById(id: String): Provider?
}
