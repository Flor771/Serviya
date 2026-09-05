package com.example.data.repository

import android.util.Log
import com.example.data.models.Review
import com.example.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class ReviewRepository(private val authRepository: AuthRepository) {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _reviewsState = MutableStateFlow<List<Review>>(emptyList())
    val reviewsState = _reviewsState.asStateFlow()

    init {
        seedInitialReviews()
    }

    private fun seedInitialReviews() {
        val list = listOf(
            Review(
                id = "rev_1",
                chambaId = "chamba_5",
                chambaTitulo = "Reparación de filtración de tubería en lavamanos",
                autorId = "cliente_4",
                autorNombre = "Dra. Carmen Nuñez",
                autorFoto = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400",
                autorRol = "cliente",
                receptorId = "demo_trabajador_1",
                puntuacion = 5.0,
                comentario = "Excelente servicio. José llegó a tiempo, trajo los materiales acordados y dejó todo impecable sin filtración. Muy recomendado en Santo Domingo.",
                fecha = System.currentTimeMillis() - 86400000L * 4
            ),
            Review(
                id = "rev_2",
                chambaId = "chamba_prev_1",
                chambaTitulo = "Instalación de tomacorrientes e interruptores inteligentes",
                autorId = "cliente_5",
                autorNombre = "Ing. Marcos Díaz",
                autorFoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                autorRol = "cliente",
                receptorId = "demo_trabajador_1",
                puntuacion = 4.9,
                comentario = "Gran dominio en electricidad. Conectó todo el sistema domótico con rapidez y profesionalismo.",
                fecha = System.currentTimeMillis() - 86400000L * 15
            ),
            Review(
                id = "rev_3",
                chambaId = "chamba_5",
                chambaTitulo = "Reparación de filtración de tubería",
                autorId = "demo_trabajador_1",
                autorNombre = "José Alberto Peralta",
                autorFoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                autorRol = "trabajador",
                receptorId = "cliente_4",
                puntuacion = 5.0,
                comentario = "Excelente cliente, trato muy educado y pago puntual acordado.",
                fecha = System.currentTimeMillis() - 86400000L * 4
            )
        )
        _reviewsState.value = list
    }

    suspend fun createReview(
        chambaId: String,
        chambaTitulo: String,
        autor: User,
        receptorId: String,
        puntuacion: Double,
        comentario: String
    ): Result<Review> {
        // Evitar calificaciones duplicadas para la misma chamba y autor
        val alreadyReviewed = _reviewsState.value.any { it.chambaId == chambaId && it.autorId == autor.uid }
        if (alreadyReviewed) {
            return Result.failure(Exception("Ya has calificado esta chamba."))
        }

        val id = "rev_${System.currentTimeMillis()}"
        val newRev = Review(
            id = id,
            chambaId = chambaId,
            chambaTitulo = chambaTitulo,
            autorId = autor.uid,
            autorNombre = autor.nombre,
            autorFoto = autor.fotoPerfil,
            autorRol = autor.rol,
            receptorId = receptorId,
            puntuacion = puntuacion.coerceIn(1.0, 5.0),
            comentario = comentario.trim(),
            fecha = System.currentTimeMillis()
        )

        val list = _reviewsState.value.toMutableList()
        list.add(0, newRev)
        _reviewsState.value = list

        // Actualizar promedio del usuario calificado
        val userReviews = list.filter { it.receptorId == receptorId }
        val newAvg = if (userReviews.isNotEmpty()) userReviews.map { it.puntuacion }.average() else 5.0
        val targetUser = authRepository.getUserById(receptorId)
        if (targetUser != null) {
            val updated = targetUser.copy(
                calificacionPromedio = Math.round(newAvg * 10.0) / 10.0,
                totalCalificaciones = userReviews.size
            )
            authRepository.updateUserProfile(updated)
        }

        if (firestore != null) {
            try {
                firestore.collection("reviews").document(id)
                    .set(newRev)
                    .await()
            } catch (e: Exception) {
                Log.w("ReviewRepo", "Firestore review error: ${e.message}")
            }
        }

        return Result.success(newRev)
    }

    fun getReviewsForUser(userId: String): List<Review> {
        return _reviewsState.value.filter { it.receptorId == userId }
    }
}
