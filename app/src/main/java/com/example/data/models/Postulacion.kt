package com.example.data.models

enum class PostulacionState(val key: String, val displayName: String) {
    PENDIENTE("pendiente", "Pendiente"),
    SELECCIONADA("seleccionada", "Seleccionada"),
    RECHAZADA("rechazada", "No seleccionada"),
    RETIRADA("retirada", "Retirada");

    companion object {
        fun fromKey(key: String?): PostulacionState {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: PENDIENTE
        }
    }
}

data class Postulacion(
    val id: String = "",
    val chambaId: String = "",
    val chambaTitulo: String = "",
    val trabajadorId: String = "",
    val trabajadorNombre: String = "",
    val trabajadorFoto: String = "",
    val trabajadorTelefono: String = "",
    val trabajadorCalificacion: Double = 5.0,
    val trabajadorTrabajos: Int = 0,
    val mensaje: String = "",
    val precioPropuesto: Double = 0.0,
    val canPerform: Boolean = true,
    val hasTools: Boolean = true,
    val availableOnDate: Boolean = true,
    val needsClientSupplies: Boolean = false,
    val confirmDetailsRead: Boolean = true,
    val estado: String = PostulacionState.PENDIENTE.key,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    val estadoEnum: PostulacionState get() = PostulacionState.fromKey(estado)
}
