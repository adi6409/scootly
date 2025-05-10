package dev.astroianu.scootly.data

import kotlinx.serialization.Serializable

@Serializable
data class Scooter(
    val id: String,
    val providerId: String,
    val latitude: Double,
    val longitude: Double,
    val range: Double, // in meters
    val reserved: Boolean,
    val disabled: Boolean,
    val lastUpdated: Long
)
