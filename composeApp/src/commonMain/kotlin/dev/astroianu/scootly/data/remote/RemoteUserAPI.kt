package dev.astroianu.scootly.data.remote

import dev.astroianu.scootly.data.AuthResponse
import dev.astroianu.scootly.data.User
import dev.astroianu.scootly.data.UserAPI
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RemoteUserAPI(
    private val ktorClient: HttpClient
) : UserAPI {

    companion object {
        private const val BASE_URL = "https://astroianu.hackclub.app/api"
        private const val USERS_ENDPOINT = "$BASE_URL/users"
        private const val AUTH_ENDPOINT = "$BASE_URL/auth"
    }

    private var accessToken: String? = null
    private var refreshToken: String? = null
    private val tokenMutex = Mutex()

    @Serializable
    private data class UserResponse(
        val statusCode: Int,
        val data: User
    )

    @Serializable
    private data class AuthResponseWrapper(
        val statusCode: Int,
        val data: AuthResponse
    )

    @Serializable
    private data class CreateUserRequest(
        val userId: String,
        val email: String
    )

    @Serializable
    private data class UpdateCityRequest(
        val city: String
    )

    @Serializable
    private data class LoginRequest(
        val email: String,
        val password: String
    )

    @Serializable
    private data class RegisterRequest(
        val email: String,
        val password: String
    )

    @Serializable
    private data class RefreshTokenRequest(
        val refreshToken: String
    )

    private suspend fun refreshTokenIfNeeded(): Boolean {
        return tokenMutex.withLock {
            refreshToken?.let { token ->
                try {
                    val response: AuthResponseWrapper = ktorClient.post("$AUTH_ENDPOINT/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshTokenRequest(token))
                    }.body()
                    
                    if (response.statusCode == 200) {
                        accessToken = response.data.accessToken
                        refreshToken = response.data.refreshToken
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
    }

    private suspend fun <T> withAuth(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: Exception) {
            if (e.message?.contains("401") == true || e.message?.contains("403") == true) {
                // Token might be expired, try to refresh
                if (refreshTokenIfNeeded()) {
                    return block()
                }
            }
            throw e
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response: AuthResponseWrapper = ktorClient.post("$AUTH_ENDPOINT/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()
            
            if (response.statusCode == 200) {
                tokenMutex.withLock {
                    accessToken = response.data.accessToken
                    refreshToken = response.data.refreshToken
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to login: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<AuthResponse> {
        return try {
            val response: AuthResponseWrapper = ktorClient.post("$AUTH_ENDPOINT/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, password))
            }.body()
            
            if (response.statusCode == 200) {
                tokenMutex.withLock {
                    accessToken = response.data.accessToken
                    refreshToken = response.data.refreshToken
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to register: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthResponse> {
        return try {
            val response: AuthResponseWrapper = ktorClient.post("$AUTH_ENDPOINT/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }.body()
            
            if (response.statusCode == 200) {
                tokenMutex.withLock {
                    accessToken = response.data.accessToken
                    this.refreshToken = response.data.refreshToken
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to refresh token: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(userId: String, email: String): Result<User> {
        return withAuth {
            val response: UserResponse = ktorClient.post(USERS_ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${accessToken}")
                setBody(CreateUserRequest(userId, email))
            }.body()
            
            if (response.statusCode == 200) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to create user: ${response.statusCode}"))
            }
        }
    }

    override suspend fun getUser(userId: String): Result<User> {
        return withAuth {
            val response: UserResponse = ktorClient.get("$USERS_ENDPOINT/$userId") {
                header("Authorization", "Bearer ${accessToken}")
            }.body()
            
            if (response.statusCode == 200) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to get user: ${response.statusCode}"))
            }
        }
    }

    override suspend fun updateUserCity(userId: String, city: String): Result<User> {
        return withAuth {
            val response: UserResponse = ktorClient.put("$USERS_ENDPOINT/$userId/city") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${accessToken}")
                setBody(UpdateCityRequest(city))
            }.body()
            
            if (response.statusCode == 200) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to update user city: ${response.statusCode}"))
            }
        }
    }
} 