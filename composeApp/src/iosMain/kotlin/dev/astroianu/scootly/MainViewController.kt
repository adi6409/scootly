// iosMain/kotlin/dev/astroianu/scootly/MainViewController.kt
package dev.astroianu.scootly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

// 1️⃣ The lateinit callback that MapComponent will invoke:
lateinit var mapViewController: (providers: List<Provider>, scooters: List<Scooter>) -> UIViewController

// Track the last created view controller to avoid recreating it on every recomposition
private var lastViewController: UIViewController? = null
private var lastProvidersHash: Int = 0
private var lastScootersHash: Int = 0

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

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun MapComponent(
    providers: List<Provider>,
    scooters: List<Scooter>
) {
    // Calculate hashes to detect changes
    val providersHash = providers.hashCode()
    val scootersHash = scooters.hashCode()
    
    // Create or reuse the view controller
    val viewController = if (lastViewController == null || 
                            providersHash != lastProvidersHash || 
                            scootersHash != lastScootersHash) {
        // Create a new view controller with updated data
        val newController = mapViewController(providers, scooters)
        lastViewController = newController
        lastProvidersHash = providersHash
        lastScootersHash = scootersHash
        newController
    } else {
        // Reuse the existing view controller
        lastViewController!!
    }
    
    // Use the view controller
    UIKitViewController(
        factory = { viewController },
        modifier = Modifier.fillMaxSize(),
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative
        )
    )
}
