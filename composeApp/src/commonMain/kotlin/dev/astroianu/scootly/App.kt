package dev.astroianu.scootly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import dev.astroianu.scootly.navigation.Screen
import dev.astroianu.scootly.screens.onboarding.OnboardingScreen
import dev.astroianu.scootly.screens.scootermap.ScooterMapScreen
import dev.astroianu.scootly.screens.settings.SettingsScreen
import dev.astroianu.scootly.storage.SettingsStorage
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.core.screen.Screen as VoyagerScreen
import dev.astroianu.scootly.navigation.OnboardingVoyagerScreen
import dev.astroianu.scootly.navigation.MapTab
import dev.astroianu.scootly.navigation.SettingsTab
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
@Preview
fun App() {
    // Inject dependencies
    val settingsStorage = remember { object : KoinComponent {
        val storage: SettingsStorage by inject()
    }.storage }
    
    var showOnboarding by remember { mutableStateOf(true) }
    
    // Check if onboarding is completed
    LaunchedEffect(Unit) {
        showOnboarding = !settingsStorage.isOnboardingCompleted()
    }
    
    MaterialTheme {
        Navigator(OnboardingVoyagerScreen) { navigator ->
            if (showOnboarding) {
                CurrentScreen()
            } else {
                TabNavigator(MapTab) {
                    Scaffold(
                        bottomBar = {
                            BottomNavigation {
                                TabNavigationItem(MapTab)
                                TabNavigationItem(SettingsTab)
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            CurrentTab()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab
    BottomNavigationItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon ?: rememberVectorPainter(Icons.Default.Home),
                contentDescription = tab.options.title
            )
        },
        label = { Text(tab.options.title) }
    )
}