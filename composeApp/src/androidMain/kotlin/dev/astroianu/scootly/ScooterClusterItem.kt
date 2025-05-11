package dev.astroianu.scootly

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import dev.astroianu.scootly.data.Scooter

data class ScooterClusterItem(
    val scooter: Scooter
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(scooter.latitude, scooter.longitude)
    override fun getTitle(): String = scooter.providerName
    override fun getSnippet(): String = scooter.id
    override fun getZIndex(): Float? {
        return null
    }
}
