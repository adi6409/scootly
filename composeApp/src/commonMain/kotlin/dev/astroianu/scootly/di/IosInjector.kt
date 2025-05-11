package dev.astroianu.scootly.di

import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.ScooterRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Allows Kotlin-native (iOS) code to pull repos out of Koin without
 * having to reference GlobalContext or startKoin themselves.
 */
object IosInjector : KoinComponent {
    val providerRepo: ProviderRepository by inject()
    val scooterRepo:  ScooterRepository  by inject()
}
