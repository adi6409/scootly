package dev.astroianu.scootly.screens.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.storage.SettingsStorage
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    // Inject dependencies
    val viewModel: OnboardingViewModel = remember { object : KoinComponent {
        val vm: OnboardingViewModel by inject()
    }.vm }
    
    // Collect state
    val cities by viewModel.cities.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isLocationPermissionGranted by viewModel.isLocationPermissionGranted.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Local UI state
    var expanded by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Welcome to Scootly",
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Step indicator
        LinearProgressIndicator(
            progress = (currentStep + 1) / 2f,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (currentStep) {
            0 -> {
                // Location permission step
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colors.primary
                )
                
                Text(
                    text = "Scootly needs location permission to show nearby scooters",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.requestLocationPermission()
                            currentStep = 1
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Grant Location Permission")
                }
            }
            
            1 -> {
                // City selection step
                Text(
                    text = "Select your city",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
                
                // City dropdown
                Box(modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text(
                        text = selectedCity.ifEmpty { "Select a city" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = .5f), MaterialTheme.shapes.small)
                            .padding(16.dp)
                    )
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.completeOnboarding()
                            onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = selectedCity.isNotEmpty()
                ) {
                    Text("Get Started")
                }
            }
        }
    }
}
