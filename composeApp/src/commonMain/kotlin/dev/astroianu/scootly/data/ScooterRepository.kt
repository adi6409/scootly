package dev.astroianu.scootly.data

interface ScooterRepository {
    suspend fun getScooters(provider: Provider? = null): List<Scooter>
}