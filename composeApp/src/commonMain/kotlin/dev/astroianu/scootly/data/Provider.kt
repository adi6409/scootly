package dev.astroianu.scootly.data

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val id: String,
    val name: String,
    val gbfsEndpoint: String,
    val gbfsEndpoints: List<GbfsEndpoints> = emptyList(),
    val features: List<ProviderFeature> = emptyList(),
    val addJson: Boolean = true,
)

@Serializable
enum class GbfsEndpoints (
    val endpoint: String
) {
    FREE_BIKE_STATUS("free_bike_status"),
    VEHICLE_TYPES("vehicle_types"),
    STATION_INFORMATION("station_information"),
    STATION_STATUS("station_status"),
    SYSTEM_INFORMATION("system_information"),
    SYSTEM_PRICING_PLANS("system_pricing_plans"),
    SYSTEM_REGIONS("system_regions"),
    GEOFENCING_ZONES("geofencing_zones"),
    GBFS_VERSIONS("gbfs_versions")
}


@Serializable
enum class ProviderFeature {
    RESERVATION,
    BOOKING,
    QR_UNLOCK,
    // Add more features as needed
}
