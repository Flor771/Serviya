package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class ReportCreateRequest(
    @Json(name = "reported_user_id") val reportedUserId: String,
    @Json(name = "chamba_id") val chambaId: String?,
    @Json(name = "reason") val reason: String,
    @Json(name = "description") val description: String,
    @Json(name = "evidence_url") val evidenceUrl: String?
)

@JsonClass(generateAdapter = true)
data class DisputeCreateRequest(
    @Json(name = "chamba_id") val chambaId: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "description") val description: String,
    @Json(name = "evidence_url") val evidenceUrl: String?
)

@JsonClass(generateAdapter = true)
data class ReportResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "reporter_id") val reporterId: String,
    @Json(name = "reported_user_id") val reportedUserId: String,
    @Json(name = "chamba_id") val chambaId: String?,
    @Json(name = "reason") val reason: String,
    @Json(name = "description") val description: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class DisputeResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "chamba_id") val chambaId: String,
    @Json(name = "creator_id") val creatorId: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "description") val description: String,
    @Json(name = "status") val status: String,
    @Json(name = "resolution_notes") val resolutionNotes: String?
)

interface ReportesApi {
    @POST("reportes/crear")
    suspend fun createReport(@Body request: ReportCreateRequest): Response<Map<String, String>>

    @POST("reportes/disputa")
    suspend fun createDispute(@Body request: DisputeCreateRequest): Response<Map<String, String>>
}
