package dev.astroianu.scootly.data.remote

import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderAPI
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

class RemoteProviderAPI(
    private val ktorClient: HttpClient
): ProviderAPI {

    companion object {
        private const val BASE_URL = "https://astroianu.hackclub.app/api" // Replace with actual base URL
        private const val CITIES_ENDPOINT = "$BASE_URL/getCities"
        private const val PROVIDERS_ENDPOINT = "$BASE_URL/getProviders"
        private const val PROVIDER_ENDPOINT = "$BASE_URL/getProvider"
    }

    @Serializable
    private data class CitiesResponse(
        val statusCode: Int,
        val data: List<String>
    )

    @Serializable
    private data class ProvidersResponse(
        val statusCode: Int,
        val data: List<Provider>
    )

    @Serializable
    private data class ProviderResponse(
        val statusCode: Int,
        val data: Provider
    )


    override suspend fun getCities(): List<String> {
        val response: CitiesResponse = ktorClient.get(CITIES_ENDPOINT).body()
        return if (response.statusCode == 200) {
            response.data
        } else {
            emptyList() // Handle error case
        }
    }

    override suspend fun getProviders(city: String): List<Provider> {
        val response: ProvidersResponse = ktorClient.get("$PROVIDERS_ENDPOINT?city=$city").body()
        return if (response.statusCode == 200) {
            response.data
        } else {
            emptyList() // Handle error case
        }
    }

    override suspend fun getProvider(id: String): Provider? {
        val response: ProviderResponse = ktorClient.get("$PROVIDER_ENDPOINT?id=$id").body()
        return if (response.statusCode == 200) {
            response.data
        } else {
            null // Handle error case
        }
    }
}