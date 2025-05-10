package dev.astroianu.scootly.data

interface ScooterRepository {
    suspend fun getScooters(providerId: String): List<Scooter>
}
