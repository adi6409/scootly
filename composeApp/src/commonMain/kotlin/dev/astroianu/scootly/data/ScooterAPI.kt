package dev.astroianu.scootly.data

// TODO: Determine if this is actually necessary, because each scooter provider has its own API but they all use GBFS
interface ScooterAPI {
    suspend fun getScooters(): List<Scooter>
}