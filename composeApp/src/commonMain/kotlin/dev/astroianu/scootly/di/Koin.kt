package dev.astroianu.scootly.di

import dev.astroianu.scootly.data.ProviderAPI
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.data.ScooterRepository
import dev.astroianu.scootly.data.mock.MockProviderAPI
import dev.astroianu.scootly.data.mock.MockProviderRepository
import dev.astroianu.scootly.data.mock.MockScooterRepository
import dev.astroianu.scootly.screens.list.ScooterListViewModel
import dev.astroianu.scootly.screens.scootermap.ScooterMapViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
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

    single<ProviderAPI> { MockProviderAPI() }
    single<ProviderRepository> { MockProviderRepository(get()) }

    single<ScooterRepository> { MockScooterRepository(get()).apply{ initialize() } }
}

val viewModelModule = module {
    viewModel { ScooterListViewModel(get(), get()) }
    viewModel { ScooterMapViewModel(get()) }
}

fun initKoin() {
    startKoin {
        modules(
            dataModule,
            viewModelModule,
        )

    }
}
