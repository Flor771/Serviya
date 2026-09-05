package com.example.data.repository

import android.util.Log
import com.example.data.models.ChatMessage
import com.example.data.models.ChatRoom
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _messagesState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messagesState = _messagesState.asStateFlow()

    init {
        seedInitialMessages()
    }

    private fun seedInitialMessages() {
        val msgs = listOf(
            ChatMessage(
                id = "msg_1",
                chambaId = "chamba_3",
                senderId = "demo_cliente_1",
                senderNombre = "Carlos Manuel Rosario",
                receiverId = "demo_trabajador_1",
                mensaje = "Hola José, te seleccioné para el armado del escritorio. ¿Tienes disponibilidad mañana a las 2:00 PM?",
                fecha = System.currentTimeMillis() - 86400000L,
                leido = true
            ),
            ChatMessage(
                id = "msg_2",
                chambaId = "chamba_3",
                senderId = "demo_trabajador_1",
                senderNombre = "José Alberto Peralta",
                receiverId = "demo_cliente_1",
                mensaje = "¡Hola Don Carlos! Excelente. Sí, estaré puntual con mis herramientas en Evaristo Morales.",
                fecha = System.currentTimeMillis() - 80000000L,
                leido = true
            ),
            ChatMessage(
                id = "msg_3",
                chambaId = "chamba_3",
                senderId = "demo_cliente_1",
                senderNombre = "Carlos Manuel Rosario",
                receiverId = "demo_trabajador_1",
                mensaje = "Perfecto, el edificio cuenta con parqueo para visitas en el frente.",
                fecha = System.currentTimeMillis() - 72000000L,
                leido = true
            )
        )
        _messagesState.value = msgs
    }

    suspend fun sendMessage(
        chambaId: String,
        senderId: String,
        senderNombre: String,
        receiverId: String,
        mensaje: String
    ): Result<ChatMessage> {
        val cleanMsg = mensaje.trim()
        if (cleanMsg.isEmpty()) return Result.failure(Exception("El mensaje no puede estar vacío"))

        val id = "msg_${System.currentTimeMillis()}"
        val chatMsg = ChatMessage(
            id = id,
            chambaId = chambaId,
            senderId = senderId,
            senderNombre = senderNombre,
            receiverId = receiverId,
            mensaje = cleanMsg,
            fecha = System.currentTimeMillis(),
            leido = false
        )

        val list = _messagesState.value.toMutableList()
        list.add(chatMsg)
        _messagesState.value = list

        if (firestore != null) {
            try {
                firestore.collection("chats").document(chambaId)
                    .collection("messages").document(id)
                    .set(chatMsg)
                    .await()
            } catch (e: Exception) {
                Log.w("ChatRepo", "Firestore send message error: ${e.message}")
            }
        }

        return Result.success(chatMsg)
    }

    fun getMessagesForChamba(chambaId: String): List<ChatMessage> {
        return _messagesState.value.filter { it.chambaId == chambaId }.sortedBy { it.fecha }
    }
}
