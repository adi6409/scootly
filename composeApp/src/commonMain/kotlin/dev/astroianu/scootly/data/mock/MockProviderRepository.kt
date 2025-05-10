package dev.astroianu.scootly.data.mock

import dev.astroianu.scootly.data.GbfsEndpoints
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderFeature
import dev.astroianu.scootly.data.ProviderRepository

class MockProviderRepository : ProviderRepository {
    private val providers = listOf(
        Provider(
            id = "lime_tel_aviv",
            name = "Lime",
            gbfsEndpoint = "https://data.lime.bike/api/partners/v2/gbfs/tel_aviv/",
            gbfsEndpoints = listOf(
                GbfsEndpoints.SYSTEM_INFORMATION,
                GbfsEndpoints.STATION_INFORMATION,
                GbfsEndpoints.STATION_STATUS,
                GbfsEndpoints.FREE_BIKE_STATUS,
                GbfsEndpoints.VEHICLE_TYPES
            ),
            features = listOf(ProviderFeature.RESERVATION, ProviderFeature.QR_UNLOCK),
            addJson = false
        ),
        Provider(
            id = "bird_tel_aviv",
            name = "Bird",
            gbfsEndpoint = "https://mds.bird.co/gbfs/v2/public/tel-aviv/",
            gbfsEndpoints = listOf(
                GbfsEndpoints.FREE_BIKE_STATUS,
                GbfsEndpoints.GBFS_VERSIONS,
                GbfsEndpoints.GEOFENCING_ZONES,
                GbfsEndpoints.STATION_INFORMATION,
                GbfsEndpoints.STATION_STATUS,
                GbfsEndpoints.SYSTEM_INFORMATION,
                GbfsEndpoints.SYSTEM_PRICING_PLANS,
                GbfsEndpoints.SYSTEM_REGIONS,
                GbfsEndpoints.VEHICLE_TYPES
            ),
            features = listOf(ProviderFeature.BOOKING)
        ),
        Provider(
            id = "dott_tel_aviv",
            name = "Dott",
            gbfsEndpoint = "https://gbfs.api.ridedott.com/public/v2/tel-aviv/",
            gbfsEndpoints = listOf(
                GbfsEndpoints.GBFS_VERSIONS,
                GbfsEndpoints.FREE_BIKE_STATUS,
                GbfsEndpoints.GEOFENCING_ZONES,
                GbfsEndpoints.SYSTEM_INFORMATION,
                GbfsEndpoints.SYSTEM_PRICING_PLANS,
                GbfsEndpoints.VEHICLE_TYPES,
                GbfsEndpoints.STATION_INFORMATION,
                GbfsEndpoints.STATION_STATUS
            ),
            features = emptyList()
        )
    )


    override suspend fun getProviders(): List<Provider> = providers

    override suspend fun getProviderById(id: String): Provider? =
        providers.find { it.id == id }
}
