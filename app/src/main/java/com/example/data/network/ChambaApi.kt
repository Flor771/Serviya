package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class ChambaDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "category_id") val categoryId: String?,
    @Json(name = "category_name") val categoryName: String?,
    @Json(name = "client_id") val clientId: String,
    @Json(name = "worker_id") val workerId: String?,
    @Json(name = "client_name") val clientName: String?,
    @Json(name = "worker_name") val workerName: String?,
    @Json(name = "province") val province: String?,
    @Json(name = "municipality") val municipality: String?,
    @Json(name = "budget_rd") val budgetRd: Double,
    @Json(name = "status") val status: String,
    @Json(name = "photos") val photos: List<String>?,
    @Json(name = "scheduled_date") val scheduledDate: String?,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class ChambaCreateRequest(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "category_id") val categoryId: String?,
    @Json(name = "category_name") val categoryName: String?,
    @Json(name = "province") val province: String?,
    @Json(name = "municipality") val municipality: String?,
    @Json(name = "budget_rd") val budgetRd: Double,
    @Json(name = "scheduled_date") val scheduledDate: String? = null,
    @Json(name = "photos") val photos: List<String>? = emptyList()
)

interface ChambaApi {
    @GET("chambas/")
    suspend fun getChambas(
        @Query("status_filter") statusFilter: String = "disponibles",
        @Query("category") category: String? = null,
        @Query("province") province: String? = null
    ): Response<List<ChambaDto>>

    @GET("chambas/{chamba_id}")
    suspend fun getChambaById(@Path("chamba_id") chambaId: String): Response<ChambaDto>

    @GET("chambas/user/my-chambas")
    suspend fun getMyChambas(): Response<List<ChambaDto>>

    @POST("chambas/")
    suspend fun createChamba(@Body request: ChambaCreateRequest): Response<ChambaDto>

    @PUT("chambas/{chamba_id}/cancel")
    suspend fun cancelChamba(@Path("chamba_id") chambaId: String): Response<ChambaDto>
}
