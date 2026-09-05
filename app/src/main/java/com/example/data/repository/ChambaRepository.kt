package com.example.data.repository

import android.util.Log
import com.example.data.models.Chamba
import com.example.data.models.ChambaState
import com.example.data.models.User
import com.example.data.network.RetrofitClient
import com.example.data.network.toDomain
import com.example.data.network.ChambaCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChambaRepository {

    private val _chambasState = MutableStateFlow<List<Chamba>>(emptyList())
    val chambasState = _chambasState.asStateFlow()

    init {
        listenToChambasFirestore()
    }

    private fun listenToChambasFirestore() {
        fetchChambas()
    }

    fun fetchChambas() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = RetrofitClient.chambaApi.getChambas()
                if (res.isSuccessful && res.body() != null) {
                    _chambasState.value = res.body()!!.map { it.toDomain() }
                }
            } catch (e: Exception) {
                Log.e("ChambaRepository", "Error fetching chambas", e)
            }
        }
    }

    suspend fun createChamba(
        cliente: User,
        titulo: String,
        descripcion: String,
        categoriaId: String,
        categoriaNombre: String,
        ubicacion: String,
        latitud: Double = 18.4861,
        longitud: Double = -69.9312,
        fechaTrabajo: String,
        horaTrabajo: String,
        precio: Double,
        materialesResponsable: String,
        costoMateriales: Double,
        fotos: List<String>
    ): Result<Chamba> {
        return try {
            val req = ChambaCreateRequest(
                title = titulo,
                description = descripcion,
                categoryId = categoriaId,
                categoryName = categoriaNombre,
                province = ubicacion,
                municipality = "Distrito Nacional",
                budgetRd = precio,
                scheduledDate = "$fechaTrabajo $horaTrabajo",
                photos = fotos
            )
            val res = RetrofitClient.chambaApi.createChamba(req)
            if (res.isSuccessful && res.body() != null) {
                fetchChambas()
                Result.success(res.body()!!.toDomain())
            } else {
                Result.failure(Exception("Creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChambaById(chambaId: String): Chamba? {
        return try {
            val res = RetrofitClient.chambaApi.getChambaById(chambaId)
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.toDomain()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateChambaState(
        chambaId: String, 
        newState: ChambaState, 
        workerId: String? = null,
        workerName: String? = null,
        workerFoto: String? = null,
        notasTerminado: String? = null,
        evidenciaFotos: List<String>? = null
    ): Result<Unit> {
        return try {
            if (newState.key.equals("cancelada", ignoreCase = true)) {
                val res = RetrofitClient.chambaApi.cancelChamba(chambaId)
                if (res.isSuccessful) {
                    fetchChambas()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Cancel failed"))
                }
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
