package dev.astroianu.scootly.data.remote

import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterAPI
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GbfsScooterAPI(
    private val ktorClient: HttpClient
) : ScooterAPI {

    @Serializable
    private data class FreeBikeStatusResponse(
        @SerialName("last_updated") val lastUpdated: Long,
        val ttl: Int,
        val data: BikesData
    )

    @Serializable
    private data class BikesData(
        val bikes: List<Bike>
    )

    @Serializable
    private data class Bike(
        @SerialName("bike_id") val bikeId: String,
        val lat: Double,
        val lon: Double,
        @SerialName("is_reserved") val isReserved: Boolean = false,
        @SerialName("is_disabled") val isDisabled: Boolean = false,
        @SerialName("current_range_meters") val range: Double? = null,
        @SerialName("last_reported") val lastReported: Long? = null
    )

    override suspend fun getScooters(provider: Provider): List<Scooter> {
        try {
            val endpoint = if (provider.addJson) {
                "${provider.gbfsEndpoint}/free_bike_status.json"
            } else {
                "${provider.gbfsEndpoint}/free_bike_status"
            }

            val response: FreeBikeStatusResponse = ktorClient.get(endpoint).body()
            return response.data.bikes.map { bike ->
                Scooter(
                    id = bike.bikeId,
                    providerId = provider.id,
                    providerName = provider.name,
                    providerIcon = provider.icon, // No icon in Provider, set as empty or provide logic if available
                    latitude = bike.lat,
                    longitude = bike.lon,
                    range = bike.range ?: 0.0,
                    reserved = bike.isReserved,
                    disabled = bike.isDisabled,
                    lastUpdated = bike.lastReported ?: response.lastUpdated
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList() // Handle error case
        }
    }

}
