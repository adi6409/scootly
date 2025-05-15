package dev.astroianu.scootly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.astroianu.scootly.navigation.Screen
import dev.astroianu.scootly.screens.onboarding.OnboardingScreen
import dev.astroianu.scootly.screens.scootermap.ScooterMapScreen
import dev.astroianu.scootly.screens.settings.SettingsScreen
import dev.astroianu.scootly.storage.SettingsStorage
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
@Preview
fun App() {
    // Inject dependencies
    val settingsStorage = remember { object : KoinComponent {
        val storage: SettingsStorage by inject()
    }.storage }
    
    // State
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Map) }
    var showOnboarding by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check if onboarding is completed
    LaunchedEffect(Unit) {
        showOnboarding = !settingsStorage.isOnboardingCompleted()
    }
    
    MaterialTheme {
        if (showOnboarding) {
            // Show onboarding screen
            OnboardingScreen(
                onComplete = {
                    showOnboarding = false
                    currentScreen = Screen.Map
                }
            )
        } else {
            // Main app with bottom navigation
            Scaffold(
                bottomBar = {
                    BottomNavigation {
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Map") },
                            label = { Text("Map") },
                            selected = currentScreen == Screen.Map,
                            onClick = { currentScreen = Screen.Map }
                        )
                        
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") },
                            selected = currentScreen == Screen.Settings,
                            onClick = { currentScreen = Screen.Settings }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentScreen) {
                        Screen.Map -> ScooterMapScreen()
                        Screen.Settings -> SettingsScreen(
                            onNavigateBack = {
                                currentScreen = Screen.Map
                            }
                        )
                        else -> ScooterMapScreen() // Fallback
                    }
                }
            }
        }
    }
}
