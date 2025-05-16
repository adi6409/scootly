package dev.astroianu.scootly.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen as VoyagerScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.astroianu.scootly.screens.onboarding.OnboardingScreen

object OnboardingVoyagerScreen : VoyagerScreen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        OnboardingScreen(
            onComplete = {
                navigator.replace(MapTab)
            }
        )
    }
} 