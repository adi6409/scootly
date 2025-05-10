package dev.astroianu.scootly.data.mock

import dev.astroianu.scootly.data.Provider
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.Scooter
import dev.astroianu.scootly.data.ScooterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MockScooterRepository(
    private val providerRepository: ProviderRepository
) : ScooterRepository {

    private var providers: List<Provider>? = emptyList()

    private val scope = CoroutineScope(SupervisorJob())

    fun initialize() {
        scope.launch {
            providers = providerRepository.getProviders()
        }
    }

    private val scootersByProvider = mapOf(
        "lime_tel_aviv" to listOf(
            Scooter(
                id = "2b4c161b-1fa0-40bc-8d55-0fe6760c19e4",
                providerId = "lime_tel_aviv",
                providerName = "Lime",
                providerIcon = "https://www.li.me/favicon.ico",
                latitude = 31.71708,
                longitude = 35.999396,
                range = 13953.0,
                reserved = false,
                disabled = false,
                lastUpdated = 1746869928
            ),
            Scooter(
                id = "fe3f8ae1-7b02-43e9-b57b-7fe300b84461",
                providerId = "lime_tel_aviv",
                providerName = "Lime",
                providerIcon = "https://www.li.me/favicon.ico",
                latitude = 32.070295,
                longitude = 34.918643,
                range = 19344.0,
                reserved = true,
                disabled = false,
                lastUpdated = 1746869928
            )
        ),
        "bird_tel_aviv" to listOf(
            Scooter(
                id = "a7dd5d78-af52-423e-804d-2f87db846b26",
                providerId = "bird_tel_aviv",
                providerName = "Bird",
                providerIcon = "https://ridedott.com/wp-content/uploads/2024/04/cropped-dottlogo90x90-entrance-logo-200x100cm-32x32.png",
                latitude = 32.1150055,
                longitude = 34.812313083333336,
                range = 5790.0,
                reserved = false,
                disabled = false,
                lastUpdated = 1746869962
            )
        ),
        "dott_tel_aviv" to listOf(
            Scooter(
                id = "28d90100-3098-4a02-9cad-f32c3295c6c4",
                providerId = "dott_tel_aviv",
                providerName = "Dott",
                providerIcon = "https://ridedott.com/wp-content/uploads/2024/04/cropped-dottlogo90x90-entrance-logo-200x100cm-32x32.png",
                latitude = 32.082117,
                longitude = 34.785287,
                range = 7452.99999,
                reserved = false,
                disabled = true,
                lastUpdated = 1746870087
            ),
            Scooter(
                id = "91215d2d-aebf-4860-ab7d-85df6ced85f6",
                providerId = "dott_tel_aviv",
                providerName = "Dott",
                providerIcon = "https://ridedott.com/wp-content/uploads/2024/04/cropped-dottlogo90x90-entrance-logo-200x100cm-32x32.png",
                latitude = 32.081501,
                longitude = 34.809431,
                range = 24509.99999,
                reserved = false,
                disabled = false,
                lastUpdated = 1746869494
            )
        )
    )

    override suspend fun getScooters(providerId: String): List<Scooter> {
        return if (providerId == "") {
            scootersByProvider.values.flatten()
        } else {
            scootersByProvider[providerId] ?: emptyList()
        }
    }
}
