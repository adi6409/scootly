package dev.astroianu.scootly

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
actual fun MapComponent(
    providers: List<Provider>,
    scooters: List<Scooter>
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(32.0853, 34.7818), 10f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            scooters.forEach { scooter ->
                val scooterCoordinates = LatLng(scooter.latitude, scooter.longitude)

                val iconFlow = remember(scooter.providerName) {
                    MapIconLoader.getIconFlow(
                        context = context,
                        providerName = scooter.providerName,
                        url = providers
                            .firstOrNull { it.id == scooter.providerId }
                            ?.icon
                            ?: "",
                        placeHolder = R.drawable.ic_launcher_foreground
                    )
                }

                val iconState = iconFlow.collectAsState(initial = null)

                Marker(
                    state = rememberUpdatedMarkerState(position = scooterCoordinates),
                    title = scooter.providerName,
                    snippet = scooter.id,
                    icon = iconState.value,
                )
            }
        }
    }
}