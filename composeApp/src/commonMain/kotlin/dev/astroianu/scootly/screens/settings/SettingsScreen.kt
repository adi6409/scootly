package dev.astroianu.scootly.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.storage.SettingsStorage
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit = {}) {
    // Inject dependencies
    val viewModel: SettingsViewModel = remember { object : KoinComponent {
        val vm: SettingsViewModel by inject()
    }.vm }
    
    // Collect state
    val cities by viewModel.cities.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Local UI state for dropdown
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.h5
        )
        
        Divider()
        
        Text(
            text = "City",
            style = MaterialTheme.typography.subtitle1
        )
        
        // City dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = selectedCity,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = .5f), MaterialTheme.shapes.small)
                    .padding(16.dp)
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                cities.forEach { city ->
                    DropdownMenuItem(onClick = {
                        coroutineScope.launch {
                            viewModel.updateSelectedCity(city)
                        }
                        expanded = false
                    }) {
                        Text(city)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // App info
        Text(
            text = "Scootly v1.0",
            style = MaterialTheme.typography.caption,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
