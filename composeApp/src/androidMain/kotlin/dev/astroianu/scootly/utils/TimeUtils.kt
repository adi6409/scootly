package dev.astroianu.scootly.utils

actual object TimeUtils {
    actual fun systemTimeMs(): Long = System.currentTimeMillis()
}