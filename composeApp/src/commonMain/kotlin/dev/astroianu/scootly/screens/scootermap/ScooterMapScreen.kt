package dev.astroianu.scootly.screens.scootermap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.astroianu.scootly.MapComponent
import org.koin.compose.koinInject

@Composable
fun ScooterMapScreen(
    scooterMapViewModel: ScooterMapViewModel = koinInject()
) {
    val scooters by scooterMapViewModel.scooters.collectAsState()
    val providers by scooterMapViewModel.providers.collectAsState()
    MapComponent(providers, scooters)
}