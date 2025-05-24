package dev.astroianu.scootly.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.astroianu.scootly.auth.FirebaseAuthService
import dev.astroianu.scootly.data.ProviderRepository
import dev.astroianu.scootly.storage.SettingsStorage
import dev.gitlive.firebase.auth.FirebaseUser
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val providerRepository: ProviderRepository,
    private val settingsStorage: SettingsStorage,
    private val firebaseAuthService: FirebaseAuthService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : ViewModel() {
    
    // Backing state
    private val _cities = MutableStateFlow<List<String>>(emptyList())
    private val _selectedCity = MutableStateFlow<String>("")
    private val _isLocationPermissionGranted = MutableStateFlow(false)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    
    // Exposed state
    val cities: StateFlow<List<String>> = _cities.asStateFlow()
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Load cities
        coroutineScope.launch {
            try {
                val availableCities = providerRepository.getCities()
                _cities.value = availableCities.sorted()
                Napier.d("Loaded ${availableCities.size} cities for onboarding")
                
                // If there's only one city, select it automatically
                if (availableCities.size == 1) {
                    _selectedCity.value = availableCities.first()
                }
            } catch (e: Exception) {
                Napier.e("Error loading cities for onboarding", e)
            }
        }
        
        // Listen to auth state changes from FirebaseAuthService
        viewModelScope.launch {
            firebaseAuthService.currentUser.collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Initial
                }
            }
        }
    }
    
    suspend fun updateSelectedCity(city: String) {
        try {
            _selectedCity.value = city
            settingsStorage.setSelectedCity(city)
            // Update user profile with selected city using FirebaseAuthService
            firebaseAuthService.updateUserProfile(city)
            Napier.d("Updated selected city to: $city")
        } catch (e: Exception) {
            Napier.e("Error updating selected city", e)
        }
    }
    
    suspend fun completeOnboarding() {
        try {
            settingsStorage.completeOnboarding()
            Napier.d("Onboarding completed")
        } catch (e: Exception) {
            Napier.e("Error completing onboarding", e)
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
