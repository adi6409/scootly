package dev.astroianu.scootly.auth

import dev.astroianu.scootly.data.AuthResponse
import dev.astroianu.scootly.data.User
import dev.astroianu.scootly.data.UserAPI
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FirebaseAuthService(
    private val userAPI: UserAPI
) {
    private val auth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    private val _userProfile = MutableStateFlow<User?>(null)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    val userProfile: StateFlow<User?> = _userProfile
    val authState: StateFlow<AuthState> = _authState

    init {
        // Listen for auth state changes
        CoroutineScope(Dispatchers.IO).launch {
            auth.authStateChanged.collect { user ->
                _currentUser.value = user
                if (user != null) {
                    // Load user profile from backend when auth state changes
                    userAPI.getUser(user.uid).onSuccess { profile ->
                        _userProfile.value = profile
                        _authState.value = AuthState.Authenticated(user)
                    }.onFailure {
                        _authState.value = AuthState.Error("Failed to load user profile")
                    }
                } else {
                    _userProfile.value = null
                    _authState.value = AuthState.Initial
                }
            }
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        _authState.value = AuthState.Loading
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            result.user?.let { user ->
                // Create user profile in backend
                userAPI.createUser(user.uid, email).onSuccess { profile ->
                    _userProfile.value = profile
                    _authState.value = AuthState.Authenticated(user)
                }
                Result.success(user)
            } ?: Result.failure(Exception("Failed to create user"))
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to sign up")
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        _authState.value = AuthState.Loading
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            result.user?.let { user ->
                // Load user profile from backend
                userAPI.getUser(user.uid).onSuccess { profile ->
                    _userProfile.value = profile
                    _authState.value = AuthState.Authenticated(user)
                }
                Result.success(user)
            } ?: Result.failure(Exception("Failed to sign in"))
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to sign in")
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Initial
    }

    suspend fun updateUserProfile(city: String) {
        currentUser.value?.let { user ->
            userAPI.updateUserCity(user.uid, city).onSuccess { updatedProfile ->
                _userProfile.value = updatedProfile
            }
        }
    }

    suspend fun getUserProfile(): User? {
        return userProfile.value
    }

    // Backend authentication methods
    suspend fun loginWithBackend(email: String, password: String): Result<AuthResponse> {
        _authState.value = AuthState.Loading
        return try {
            val result = userAPI.login(email, password)
            result.onSuccess { authResponse ->
                _userProfile.value = authResponse.user
                _authState.value = AuthState.Authenticated(null) // No Firebase user for backend auth
            }.onFailure {
                _authState.value = AuthState.Error("Failed to login with backend")
            }
            result
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to login with backend")
            Result.failure(e)
        }
    }

    suspend fun registerWithBackend(email: String, password: String): Result<AuthResponse> {
        _authState.value = AuthState.Loading
        return try {
            val result = userAPI.register(email, password)
            result.onSuccess { authResponse ->
                _userProfile.value = authResponse.user
                _authState.value = AuthState.Authenticated(null) // No Firebase user for backend auth
            }.onFailure {
                _authState.value = AuthState.Error("Failed to register with backend")
            }
            result
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to register with backend")
            Result.failure(e)
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}