package dev.astroianu.scootly.di

import dev.astroianu.scootly.auth.FirebaseAuthService
import dev.astroianu.scootly.data.ProviderAPI
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.ScooterAPI
import dev.astroianu.scootly.data.ScooterRepository
import dev.astroianu.scootly.data.UserAPI
import dev.astroianu.scootly.data.mock.MockProviderAPI
import dev.astroianu.scootly.data.mock.MockProviderRepository
import dev.astroianu.scootly.data.mock.MockScooterAPI
import dev.astroianu.scootly.data.mock.MockScooterRepository
import dev.astroianu.scootly.data.mock.MockUserAPI
import dev.astroianu.scootly.data.remote.GbfsScooterAPI
import dev.astroianu.scootly.data.remote.RemoteProviderAPI
import dev.astroianu.scootly.data.remote.RemoteUserAPI
import dev.astroianu.scootly.screens.list.ScooterListViewModel
import dev.astroianu.scootly.screens.onboarding.OnboardingViewModel
import dev.astroianu.scootly.screens.scootermap.ScooterMapViewModel
import dev.astroianu.scootly.screens.settings.SettingsViewModel
import dev.astroianu.scootly.storage.SettingsStorage
import dev.astroianu.scootly.storage.createSettingsStorage
import dev.gitlive.firebase.Firebase
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single {
         HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
        }
    }

//    single<ProviderAPI> { MockProviderAPI() }
    single<ProviderAPI> { RemoteProviderAPI(get()) }
    single<ProviderRepository> { MockProviderRepository(get()) }

//    single<ScooterAPI> { MockScooterAPI() }
    single<ScooterAPI> { GbfsScooterAPI(get()) }
    single<ScooterRepository> { MockScooterRepository(get(), get(), get()).apply{ initialize() } }

//    single<UserAPI> { RemoteUserAPI(get()) }
    single<UserAPI> { MockUserAPI() }
    
    // Settings storage
    single<SettingsStorage> { createSettingsStorage() }
    
    // Firebase Auth
    single { Firebase.auth }

    single {FirebaseAuthService(get())}
}

val viewModelModule = module {
    viewModel { ScooterListViewModel(get(), get(), get()) }
    viewModel { ScooterMapViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { OnboardingViewModel(get(), get(), get()) }
}

fun initKoin() {
    Napier.base(DebugAntilog())
    startKoin {
        modules(
            dataModule,
            viewModelModule,
        )
    }
}
