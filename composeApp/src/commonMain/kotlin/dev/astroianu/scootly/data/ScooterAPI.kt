package dev.astroianu.scootly.data

interface ScooterAPI {
    suspend fun getScooters(provider: Provider): List<Scooter>
}