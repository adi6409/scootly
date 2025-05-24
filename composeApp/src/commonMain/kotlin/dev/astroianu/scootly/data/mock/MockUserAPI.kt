package dev.astroianu.scootly.data.mock

import dev.astroianu.scootly.data.AuthResponse
import dev.astroianu.scootly.data.User
import dev.astroianu.scootly.data.UserAPI

class MockUserAPI: UserAPI {

    val mockAuthToken = ""
    val mockRefreshToken = ""
    val mockUser = User(
        id="mock_user_id",
        email = "mock@example.com",
        city = "Mock City",
        createdAt = 1748096298L
    )
    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return AuthResponse(
            accessToken = mockAuthToken,
            refreshToken = mockRefreshToken,
            user = mockUser
        ).let { Result.success(it) }
    }

    override suspend fun register(email: String, password: String): Result<AuthResponse> {
        return AuthResponse(
            accessToken = mockAuthToken,
            refreshToken = mockRefreshToken,
            user = mockUser
        ).let { Result.success(it) }
    }

    override suspend fun createUser(userId: String, email: String): Result<User> {
        return Result.success(
            User(
                id = userId,
                email = email,
                city = "Mock City",
                createdAt = 1748096298L
            )
        )
    }

    override suspend fun getUser(userId: String): Result<User> {
        return Result.success(mockUser)
    }

    override suspend fun updateUserCity(userId: String, city: String): Result<User> {
        return Result.success(
            mockUser.copy(city = city)
        )
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthResponse> {
        return AuthResponse(
            accessToken = mockAuthToken,
            refreshToken = mockRefreshToken,
            user = mockUser
        ).let { Result.success(it) }
    }
}