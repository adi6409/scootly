package dev.astroianu.scootly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import dev.astroianu.scootly.data.Scooter
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

fun MainViewController(
    mapUIViewController: () -> UIViewController
) = ComposeUIViewController {
    mapViewController = mapUIViewController
    App()
}

lateinit var mapViewController: () -> UIViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapComponent(scooters: List<Scooter>) {
    UIKitViewController(
        factory = mapViewController,
        modifier = Modifier.fillMaxSize(),
    )
}
