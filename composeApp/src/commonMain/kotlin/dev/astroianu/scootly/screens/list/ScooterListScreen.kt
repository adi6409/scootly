package dev.astroianu.scootly.screens.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter

@Composable
fun ScooterListScreen(
    scooterListViewModel: ScooterListViewModel,
) {
    val providers by scooterListViewModel.providers.collectAsState()
    val selectedProvider by scooterListViewModel.selectedProvider.collectAsState()
    val scooters by scooterListViewModel.scooters.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Provider selector
        if (providers.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedProvider?.name ?: "Select Provider")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            onClick = {
                                scooterListViewModel.selectProvider(provider.id)
                                expanded = false
                            }
                        ) {
                            Text(provider.name)
                        }
                    }
                    DropdownMenuItem(
                        onClick = {
                            scooterListViewModel.selectProvider("")
                            expanded = false
                        }
                    ) {
                        Text("All Providers")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scooter list
        if (scooters.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(scooters) { scooter ->
                    ScooterListItem(scooter, providers.find { it.id == scooter.providerId } ?: null)
                    Divider()
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No scooters available")
            }
        }
    }
}

@Composable
private fun ScooterListItem(scooter: Scooter, provider: Provider?) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text("ID: ${scooter.id}", style = MaterialTheme.typography.subtitle1)
        Text("Provider: ${provider?.name ?: "Unknown"}", style = MaterialTheme.typography.body2)
        Text("Location: ${scooter.latitude}, ${scooter.longitude}", style = MaterialTheme.typography.body2)
        Text("Range: ${scooter.range.toInt()} m", style = MaterialTheme.typography.body2)
        Text("Reserved: ${if (scooter.reserved) "Yes" else "No"}", style = MaterialTheme.typography.body2)
        Text("Disabled: ${if (scooter.disabled) "Yes" else "No"}", style = MaterialTheme.typography.body2)
    }
}
