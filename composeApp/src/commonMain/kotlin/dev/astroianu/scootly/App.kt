package dev.astroianu.scootly

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import dev.astroianu.scootly.navigation.MapTab
import dev.astroianu.scootly.navigation.OnboardingVoyagerScreen
import dev.astroianu.scootly.navigation.SettingsTab
import dev.astroianu.scootly.storage.SettingsStorage
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBar
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBarItem
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.CupertinoThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.MaterialThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.Theme
import io.github.alexzhirkevich.cupertino.theme.ColorScheme
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme
import io.github.alexzhirkevich.cupertino.theme.Shapes
import io.github.alexzhirkevich.cupertino.theme.Typography
import io.github.alexzhirkevich.cupertino.theme.darkColorScheme
import io.github.alexzhirkevich.cupertino.theme.lightColorScheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalAdaptiveApi::class)
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

    val platform = remember { getPlatform() }

    AppTheme(platform = platform) {
        Navigator(OnboardingVoyagerScreen) { navigator ->
            if (showOnboarding) {
                CurrentScreen()
            } else {
                TabNavigator(MapTab) {
                    Scaffold(
                        bottomBar = {
                            AdaptiveNavigationBar {
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


@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AppTheme(
    platform: Platform,
    content: @Composable () -> Unit
) {
    // Theme.Material3 on Android, Theme.Cupertino on iOS.
    val theme = remember {
        if (platform.name.lowercase().contains("ios")) {
            Theme.Cupertino
        } else {
            Theme.Material3
        }
    }
    AdaptiveTheme(theme, MaterialThemeSpec.Default(), CupertinoThemeSpec.Default(), content)
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab
    AdaptiveNavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon ?:
                    rememberVectorPainter(Icons.Default.Home),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        label = { Text(tab.options.title) }
    )
}