package com.example.data.repository

import android.util.Log
import com.example.data.models.Chamba
import com.example.data.models.ChambaState
import com.example.data.models.Postulacion
import com.example.data.models.PostulacionState
import com.example.data.models.User
import com.example.data.network.RetrofitClient
import com.example.data.network.toDomain
import com.example.data.network.PostulacionCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostulacionRepository(
    private val chambaRepository: ChambaRepository,
    private val notificationRepository: NotificationRepository
) {
    private val _postulacionesState = MutableStateFlow<List<Postulacion>>(emptyList())
    val postulacionesState = _postulacionesState.asStateFlow()

    fun fetchPostulacionesPorChamba(chambaId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = RetrofitClient.postulacionApi.getPostulacionesPorChamba(chambaId)
                if (res.isSuccessful && res.body() != null) {
                    val list = res.body()!!.map { it.toDomain() }
                    val currentMap = _postulacionesState.value.associateBy { it.id }.toMutableMap()
                    list.forEach { currentMap[it.id] = it }
                    _postulacionesState.value = currentMap.values.toList()
                }
            } catch (e: Exception) {
                Log.e("PostulacionRepo", "Error fetching postulaciones", e)
            }
        }
    }

    suspend fun createPostulacion(
        chamba: Chamba,
        trabajador: User,
        mensaje: String,
        precioPropuesto: Double
    ): Result<Unit> {
        return try {
            val req = PostulacionCreateRequest(
                chambaId = chamba.id,
                message = mensaje,
                proposedPriceRd = precioPropuesto
            )
            val res = RetrofitClient.postulacionApi.createPostulacion(req)
            if (res.isSuccessful) {
                fetchPostulacionesPorChamba(chamba.id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al postularse"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostulacionesPorChambaSync(chambaId: String): List<Postulacion> {
        return try {
            val res = RetrofitClient.postulacionApi.getPostulacionesPorChamba(chambaId)
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.map { it.toDomain() }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPostulacionesPorTrabajadorSync(trabajadorId: String): List<Postulacion> {
        return emptyList()
    }

    suspend fun getPostulacionAprobadaParaChamba(chambaId: String): Postulacion? {
        val list = getPostulacionesPorChambaSync(chambaId)
        return list.find { it.estadoEnum == PostulacionState.SELECCIONADA }
    }

    suspend fun selectTrabajador(
        chamba: Chamba,
        postulacion: Postulacion
    ): Result<Unit> {
        return try {
            val res = RetrofitClient.postulacionApi.selectPostulacion(postulacion.id)
            if (res.isSuccessful) {
                fetchPostulacionesPorChamba(chamba.id)
                chambaRepository.fetchChambas()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al aceptar postulacion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazarPostulacion(postulacion: Postulacion): Result<Unit> {
        return Result.success(Unit)
    }
}
