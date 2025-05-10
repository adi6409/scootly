package dev.astroianu.scootly

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

object MapIconLoader {
    private val iconCache = mutableMapOf<String, MutableStateFlow<BitmapDescriptor?>>()

    private const val MARKER_ICON_SIZE_PX = 96  // reasonable fixed size for markers

    fun getIconFlow(
        context: Context,
        providerName: String,
        url: String?,
        @DrawableRes placeHolder: Int
    ): StateFlow<BitmapDescriptor?> {
        return iconCache.getOrPut(providerName) {
            MutableStateFlow<BitmapDescriptor?>(null).also { flow ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val originalBitmap = Glide.with(context)
                            .asBitmap()
                            .load(url)
                            .error(placeHolder)
                            .submit()
                            .get()

                        val resizedBitmap = originalBitmap.scaleToMaxSize(MARKER_ICON_SIZE_PX)

                        flow.value = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        flow.value = null
                    }
                }
            }
        }
    }

    private fun Bitmap.scaleToMaxSize(maxSize: Int): Bitmap {
        val ratio = max(width, height).toFloat() / maxSize
        val newWidth = (width / ratio).toInt()
        val newHeight = (height / ratio).toInt()
        return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
    }
}
