package dev.astroianu.scootly

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform