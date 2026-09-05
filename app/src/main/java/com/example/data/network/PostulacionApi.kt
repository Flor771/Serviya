package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class PostulacionDto(
    @Json(name = "id") val id: String,
    @Json(name = "chamba_id") val chambaId: String,
    @Json(name = "worker_id") val workerId: String,
    @Json(name = "worker_name") val workerName: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "proposed_price_rd") val proposedPriceRd: Double,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class PostulacionCreateRequest(
    @Json(name = "chamba_id") val chambaId: String,
    @Json(name = "message") val message: String?,
    @Json(name = "proposed_price_rd") val proposedPriceRd: Double
)

interface PostulacionApi {
    @GET("postulaciones/chamba/{chamba_id}")
    suspend fun getPostulacionesPorChamba(@Path("chamba_id") chambaId: String): Response<List<PostulacionDto>>

    @POST("postulaciones/")
    suspend fun createPostulacion(@Body request: PostulacionCreateRequest): Response<PostulacionDto>

    @POST("postulaciones/{postulacion_id}/select")
    suspend fun selectPostulacion(@Path("postulacion_id") postulacionId: String): Response<PostulacionDto>
}
