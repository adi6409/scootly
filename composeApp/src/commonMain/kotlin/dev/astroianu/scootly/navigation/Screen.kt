package dev.astroianu.scootly.navigation

/**
 * Represents the different screens in the app
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Map : Screen("map")
    object Settings : Screen("settings")
}
