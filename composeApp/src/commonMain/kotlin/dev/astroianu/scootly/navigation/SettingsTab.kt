package dev.astroianu.scootly.navigation

import androidx.compose.runtime.Composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.astroianu.scootly.screens.settings.SettingsScreen

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = Icons.Default.Settings
            return TabOptions(
                index = 1u,
                title = "Settings",
                icon = rememberVectorPainter(icon)
            )
        }
    @Composable
    override fun Content() {
        SettingsScreen()
    }
} 