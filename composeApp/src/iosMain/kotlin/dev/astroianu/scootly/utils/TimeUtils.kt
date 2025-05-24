package dev.astroianu.scootly.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object TimeUtils {
    actual fun systemTimeMs(): Long = NSDate().timeIntervalSince1970.toLong() * 1000
}
