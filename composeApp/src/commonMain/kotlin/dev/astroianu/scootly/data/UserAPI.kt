package dev.astroianu.scootly.data

import kotlinx.serialization.Serializable

interface UserAPI {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(email: String, password: String): Result<AuthResponse>
    suspend fun createUser(userId: String, email: String): Result<User>
    suspend fun getUser(userId: String): Result<User>
    suspend fun updateUserCity(userId: String, city: String): Result<User>
    suspend fun refreshToken(refreshToken: String): Result<AuthResponse>
}

@Serializable
data class User(
    val id: String,
    val email: String,
    val city: String? = null,
    val createdAt: Long = 0
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
) 