package dev.astroianu.scootly

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.Scooter
import kotlinx.coroutines.launch

@OptIn(MapsComposeExperimentalApi::class)
@Composable
actual fun MapComponent(
    providers: List<Provider>,
    scooters: List<Scooter>,
    onScooterClick: (Scooter?) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var visibleScooters by remember { mutableStateOf<List<Scooter>>(emptyList()) }
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    // Function to find the nearest scooter to user's location
    fun findNearestScooter(userLatLng: LatLng, scooters: List<Scooter>): Scooter? {
        if (scooters.isEmpty()) return null
        
        return scooters.minByOrNull { scooter ->
            val scooterLatLng = LatLng(scooter.latitude, scooter.longitude)
            val distance = FloatArray(1)
            Location.distanceBetween(
                userLatLng.latitude,
                userLatLng.longitude,
                scooterLatLng.latitude,
                scooterLatLng.longitude,
                distance
            )
            distance[0]
        }
    }

    // Get user's location and move camera to it
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        userLocation = latLng
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(latLng, 15f)
                                )
                            )
                        }
                    }
                }
        }
    }

    // Update camera position when scooters list changes
    LaunchedEffect(scooters, userLocation) {
        userLocation?.let { userLatLng ->
            val nearestScooter = findNearestScooter(userLatLng, scooters)
            nearestScooter?.let { scooter ->
                val scooterLatLng = LatLng(scooter.latitude, scooter.longitude)
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(scooterLatLng, 15f)
                        )
                    )
                }
            }
        }
    }

    var lastCameraPosition by remember { mutableStateOf<CameraPosition?>(null) }

    LaunchedEffect(cameraPositionState.position) {
        val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
        if (bounds != null && cameraPositionState.position != lastCameraPosition) {
            lastCameraPosition = cameraPositionState.position
            val expanded = expandBounds(bounds, 0.001)
            visibleScooters = scooters.filter {
                expanded.contains(LatLng(it.latitude, it.longitude))
            }
            Log.d("MapComponent", "Visible scooters updated: ${visibleScooters.size}")
        }
    }

    // Update visible scooters based on expanded visible region
//    LaunchedEffect(cameraPositionState.isMoving) {
//        Log.d("MapComponent", "Camera is moving: ${cameraPositionState.isMoving}")
//        if (!cameraPositionState.isMoving) {
//            cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
//                val expanded = expandBounds(bounds, 0.001)
//                visibleScooters = scooters.filter {
//                    expanded.contains(LatLng(it.latitude, it.longitude))
//                }
//                Log.d("MapComponent", "Visible scooters: ${visibleScooters.size}")
//            }
//        }
//    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true,
                mapToolbarEnabled = false // Disable the default toolbar
            )
        ) {
            // Hoist the map reference so we can remember the cluster manager
            var mapRef by remember { mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null) }

            MapEffect { map ->
                mapRef = map
            }

            mapRef?.let { map ->
                val clusterAlgorithm = remember(screenWidthDp, screenHeightDp) {
                    NonHierarchicalViewBasedAlgorithm<ScooterClusterItem>(
                        screenWidthDp,
                        screenHeightDp
                    ).apply {
                        setMaxDistanceBetweenClusteredItems(20) // Tune this for cluster tightness
                    }
                }
                val clusterManager = remember(map) {
                    ClusterManager<ScooterClusterItem>(context, map).apply {
                        renderer = ScooterClusterRenderer(context, map, this)
                        algorithm = clusterAlgorithm
                        map.setOnCameraIdleListener(this)
                        map.setOnMarkerClickListener(this)
                        
                        // Set up the click listener for cluster items
                        setOnClusterItemClickListener { clusterItem ->
                            onScooterClick(clusterItem.scooter)
                            false // Return false to allow the marker to show info window
                        }
                        
                        // Set up the click listener for clusters
                        setOnClusterClickListener { cluster ->
                            // If there's only one item in the cluster, select it
                            if (cluster.size == 1) {
                                onScooterClick(cluster.items.first().scooter)
                                false
                            } else {
                                true // Return true to allow default behavior for clusters
                            }
                        }
                    }
                }


                LaunchedEffect(visibleScooters) {
                    clusterManager.clearItems()
                    clusterManager.addItems(visibleScooters.map { ScooterClusterItem(it) })
                    clusterManager.cluster()
                }
            }
        }
    }
}

private fun expandBounds(bounds: LatLngBounds, buffer: Double): LatLngBounds {
    return LatLngBounds(
        LatLng(bounds.southwest.latitude - buffer, bounds.southwest.longitude - buffer),
        LatLng(bounds.northeast.latitude + buffer, bounds.northeast.longitude + buffer)
    )
}
