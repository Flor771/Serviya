package com.example.data.network

import com.example.data.models.Chamba
import com.example.data.models.User
import com.example.data.models.Postulacion

fun UserDto.toDomain(): User {
    return User(
        uid = id,
        nombre = fullName,
        email = email,
        telefono = phone ?: "",
        rol = role,
        descripcion = description ?: "",
        verificacionEstado = verificationStatus ?: "sin_solicitar",
        verificado = isVerified ?: false,
        trabajosCompletados = completedJobs ?: 0,
        zona = province ?: "Santo Domingo"
    )
}

fun ChambaDto.toDomain(): Chamba {
    return Chamba(
        id = id,
        clienteId = clientId,
        clienteNombre = clientName ?: "",
        titulo = title,
        descripcion = description,
        categoriaId = categoryId ?: "",
        categoriaNombre = categoryName ?: "General",
        ubicacion = province ?: "República Dominicana",
        precio = budgetRd,
        fotos = photos ?: emptyList(),
        estado = status, // El status de FastAPI mapea a los states de Android? 
        trabajadorSeleccionadoId = workerId ?: "",
        trabajadorSeleccionadoNombre = workerName ?: "",
        fechaTrabajo = scheduledDate ?: ""
    )
}

fun PostulacionDto.toDomain(): Postulacion {
    return Postulacion(
        id = id,
        chambaId = chambaId,
        trabajadorId = workerId,
        trabajadorNombre = workerName ?: "",
        mensaje = message ?: "",
        precioPropuesto = proposedPriceRd,
        estado = status
    )
}
