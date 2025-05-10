package dev.astroianu.scootly

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import dev.astroianu.scootly.screens.list.ScooterListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        ScooterListScreen()
    }
}