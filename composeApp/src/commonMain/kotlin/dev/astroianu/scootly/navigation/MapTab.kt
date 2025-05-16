package dev.astroianu.scootly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.astroianu.scootly.screens.scootermap.ScooterMapScreen

object MapTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = Icons.Default.Home
            return TabOptions(
                index = 0u,
                title = "Map",
                icon = rememberVectorPainter(icon)
            )
        }
    @Composable
    override fun Content() {
        ScooterMapScreen()
    }
} 