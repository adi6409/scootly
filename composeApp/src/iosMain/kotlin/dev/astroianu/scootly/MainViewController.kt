// iosMain/kotlin/dev/astroianu/scootly/MainViewController.kt
package dev.astroianu.scootly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

// 1️⃣ The lateinit callback that MapComponent will invoke:
lateinit var mapViewController: (providers: List<Provider>, scooters: List<Scooter>) -> UIViewController

/**
 * This factory gets called by Swift—with your Swift closure—so that
 * `mapViewController` is wired up before any Composable tries to use it.
 */
fun MainViewController(
    mapUIViewController: (providers: List<Provider>, scooters: List<Scooter>) -> UIViewController
): UIViewController =
    ComposeUIViewController {
        // assign the Swift closure into our lateinit var
        mapViewController = mapUIViewController
        // now start our shared Compose App
        App()
    }

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapComponent(
    providers: List<Provider>,
    scooters: List<Scooter>
) {
    // this will now safely call the closure you passed in
    UIKitViewController(
        factory = { mapViewController(providers, scooters) },
        modifier = Modifier.fillMaxSize(),
    )
}
