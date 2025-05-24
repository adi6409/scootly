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
import com.mmk.kmpauth.firebase.apple.AppleButtonUiContainer
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import io.github.aakira.napier.Napier
import com.mmk.kmpauth.uihelper.apple.AppleSignInButton
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton

@OptIn(ExperimentalPermissionsApi::class)
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
    val authState by viewModel.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Local UI state
    var expanded by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }
    val permissionState = rememberPermissionState(permission = Permission.CoarseLocation)
    
    // Add debug logging for permission state changes
    LaunchedEffect(permissionState.status) {
        Napier.d("Location permission status changed to: ${permissionState.status}")
    }
    
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
            progress = (currentStep + 1) / 3f,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (currentStep) {
            0 -> {
                // Authentication step
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
                
                when (authState) {
                    is AuthState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is AuthState.Error -> {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colors.error
                        )
                    }
                    is AuthState.Authenticated -> {
                        currentStep = 1
                    }
                    else -> {
                        // Google Sign-In
                        GoogleButtonUiContainerFirebase(
                            onResult = { result ->
                                result.onSuccess { user ->
                                    if (user != null) {
                                        currentStep = 1
                                    }
                                }
                            }
                        ) {
                            GoogleSignInButton(
                                modifier = Modifier.fillMaxWidth()
                            ) { onClick() }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Apple Sign-In
                        AppleButtonUiContainer(
                            onResult = { result ->
                                Napier.d("Apple Sign-In result: $result")
                                result.fold(
                                    onSuccess = { user ->
                                        Napier.d("Apple Sign-In user: $user")
                                        if (user != null) {
                                            currentStep = 1
                                            Napier.d("Apple Sign-In successful, moving to next step")
                                        } else {
                                            Napier.e("Apple Sign-In failed, user is null")
                                        }
                                    },
                                    onFailure = { exception ->
                                        Napier.e("Apple Sign-In error: ${exception.message}")
                                        Napier.e("Error cause: ${exception.cause}")
                                        Napier.e("Stack trace: ${exception.stackTraceToString()}")
                                    }
                                )
                            }
                        ) {
                            AppleSignInButton(
                                modifier = Modifier.fillMaxWidth()
                            ) { onClick() }
                        }
                    }
                }
            }
            1 -> {
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
                if (permissionState.status == PermissionStatus.Granted) {
                    Text(
                        text = "Location permission granted",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.primary
                    )
                    Button(
                        onClick = {
                            currentStep = 2
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Continue")
                    }
                } else {
                    Text(
                        text = "Location permission not granted",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.error
                    )
                    Button(
                        onClick = {
                            permissionState.launchPermissionRequest()
                            Napier.d("Requesting location permission")
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Grant Location Permission")
                    }
                }
            }
            2 -> {
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
