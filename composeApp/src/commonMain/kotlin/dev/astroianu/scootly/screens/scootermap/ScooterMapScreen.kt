package dev.astroianu.scootly.screens.scootermap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.astroianu.scootly.MapComponent
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter
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
    val scooters by viewModel.scooters.collectAsState()
    val selectedScooter by viewModel.selectedScooter.collectAsState()

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
            MapComponent(
                providers = providers,
                scooters = scooters,
                onScooterClick = { scooter ->
                    println("ScooterMapScreen: onScooterClick called with scooter: ${scooter?.providerName} (ID: ${scooter?.id})")
                    viewModel.selectScooter(scooter)
                }
            )
            
            // Bottom overlay when a scooter is selected
            this@Column.AnimatedVisibility(
                visible = selectedScooter != null,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(16.dp)
                        .background(MaterialTheme.colors.surface)
                        .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
                ) {
                    // This is an empty rectangle overlay as requested
                    // You can add content here later if needed
                }
            }
        }
    }
}
