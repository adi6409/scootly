package dev.astroianu.scootly

import androidx.compose.runtime.Composable
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter

@Composable
expect fun MapComponent(
    providers: List<Provider>,
    scooters: List<Scooter>
)