package dev.astroianu.scootly

import androidx.compose.runtime.Composable
import dev.astroianu.scootly.data.Scooter

@Composable
expect fun MapComponent(scooters: List<Scooter>)