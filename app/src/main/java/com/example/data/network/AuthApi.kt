package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "role") val role: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "province") val province: String? = "Santo Domingo",
    @Json(name = "municipality") val municipality: String? = "Distrito Nacional"
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "user") val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "role") val role: String,
    @Json(name = "province") val province: String?,
    @Json(name = "municipality") val municipality: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "experience_years") val experienceYears: Int?,
    @Json(name = "hourly_rate_rd") val hourlyRateRd: Double?,
    @Json(name = "verification_status") val verificationStatus: String?,
    @Json(name = "is_verified") val isVerified: Boolean?,
    @Json(name = "completed_jobs") val completedJobs: Int?
)

@JsonClass(generateAdapter = true)
data class UserUpdateRequest(
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "province") val province: String? = null,
    @Json(name = "municipality") val municipality: String? = null,
    @Json(name = "description") val description: String? = null
)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserDto>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UserUpdateRequest): Response<UserDto>
}
