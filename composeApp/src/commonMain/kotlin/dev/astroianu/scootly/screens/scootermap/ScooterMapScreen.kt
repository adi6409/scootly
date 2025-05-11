package dev.astroianu.scootly.screens.scootermap

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.astroianu.scootly.MapComponent
import dev.astroianu.scootly.data.Provider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
fun ScooterMapScreen() {
    // Inject the shared ViewModel
    val viewModel: ScooterMapViewModel = remember { object : KoinComponent {
        val vm: ScooterMapViewModel by inject()
    }.vm }

    // Collect state
    val providers by viewModel.providers.collectAsState()
    val scooters  by viewModel.scooters.collectAsState()

    // Local UI state for dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Dropdown for provider filter
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            Text(
                text = selectedProvider?.name ?: "All providers",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(12.dp)
                    .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = .5f), MaterialTheme.shapes.small)
                    .padding(8.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    selectedProvider = null
                    expanded = false
                    viewModel.refreshScooters(null)
                }) {
                    Text("All providers")
                }
                providers.forEach { provider ->
                    DropdownMenuItem(onClick = {
                        selectedProvider = provider
                        expanded = false
                        viewModel.refreshScooters(provider)
                    }) {
                        Text(provider.name)
                    }
                }
            }
        }

        // The map view (hosts your SwiftUI map under the hood)
        Box(modifier = Modifier.weight(1f)) {
            MapComponent(providers, scooters)
        }
    }
}
