package com.example.data.repository

import android.util.Log
import com.example.data.models.NotificationItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _notificationsState = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notificationsState = _notificationsState.asStateFlow()

    init {
        seedInitialNotifications()
    }

    private fun seedInitialNotifications() {
        val list = listOf(
            NotificationItem(
                id = "notif_1",
                userId = "demo_cliente_1",
                titulo = "Postulación recibida",
                mensaje = "José Alberto Peralta se postuló para tu chamba «Pintar sala y habitación».",
                tipo = "postulacion",
                chambaId = "chamba_1",
                leida = false,
                fecha = System.currentTimeMillis() - 3600000L
            ),
            NotificationItem(
                id = "notif_2",
                userId = "demo_trabajador_1",
                titulo = "¡Fuiste seleccionado!",
                mensaje = "Carlos Manuel Rosario te seleccionó para la chamba «Montaje de escritorio».",
                tipo = "seleccion",
                chambaId = "chamba_3",
                leida = true,
                fecha = System.currentTimeMillis() - 86400000L * 2
            ),
            NotificationItem(
                id = "notif_3",
                userId = "demo_cliente_1",
                titulo = "Chamba en proceso",
                mensaje = "José Alberto ha marcado el inicio del trabajo en «Montaje de escritorio».",
                tipo = "estado",
                chambaId = "chamba_3",
                leida = false,
                fecha = System.currentTimeMillis() - 1800000L
            )
        )
        _notificationsState.value = list
    }

    suspend fun sendNotification(
        userId: String,
        titulo: String,
        mensaje: String,
        tipo: String,
        chambaId: String
    ): Result<NotificationItem> {
        val id = "notif_${System.currentTimeMillis()}"
        val item = NotificationItem(
            id = id,
            userId = userId,
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipo,
            chambaId = chambaId,
            leida = false,
            fecha = System.currentTimeMillis()
        )

        val list = _notificationsState.value.toMutableList()
        list.add(0, item)
        _notificationsState.value = list

        if (firestore != null) {
            try {
                firestore.collection("notifications").document(id)
                    .set(item)
                    .await()
            } catch (e: Exception) {
                Log.w("NotificationRepo", "Firestore error: ${e.message}")
            }
        }

        return Result.success(item)
    }

    fun markAsRead(notificationId: String) {
        _notificationsState.value = _notificationsState.value.map {
            if (it.id == notificationId) it.copy(leida = true) else it
        }
    }

    fun getNotificationsForUser(userId: String): List<NotificationItem> {
        return _notificationsState.value.filter { it.userId == userId || it.userId.isEmpty() }
    }
}
