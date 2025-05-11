package dev.astroianu.scootly

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import dev.astroianu.scootly.MapIconLoader
import dev.astroianu.scootly.ScooterClusterItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScooterClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<ScooterClusterItem>
) : DefaultClusterRenderer<ScooterClusterItem>(context, map, clusterManager) {

    private val scope = MainScope()

    override fun onBeforeClusterItemRendered(
        item: ScooterClusterItem,
        markerOptions: MarkerOptions
    ) {
        markerOptions.title(item.title)
        markerOptions.snippet(item.snippet)

        val provider = item.scooter.providerName
        val iconFlow = MapIconLoader.getIconFlow(
            context = context,
            providerName = provider,
            url = item.scooter.providerIcon, // you must add this field in Scooter if not present
            placeHolder = com.google.maps.android.R.drawable.amu_bubble_mask // fallback drawable
        )

        scope.launch {
            iconFlow.collectLatest { descriptor ->
                if (descriptor != null) {
                    val marker = getMarker(item)
                    marker?.setIcon(descriptor)
                    Log.d("ClusterRenderer", "Icon loaded for $provider")
                }
            }
        }
    }

    override fun shouldRenderAsCluster(cluster: com.google.maps.android.clustering.Cluster<ScooterClusterItem>) = cluster.size > 1
}
