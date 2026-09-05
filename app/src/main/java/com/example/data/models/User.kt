package com.example.data.models

enum class UserRole(val value: String, val displayName: String) {
    CLIENTE("cliente", "Cliente"),
    TRABAJADOR("trabajador", "Técnico"),
    ADMIN("admin", "Administrador");

    companion object {
        fun fromString(role: String?): UserRole {
            return when (role?.lowercase()?.trim()) {
                "trabajador", "tecnico" -> TRABAJADOR
                "admin" -> ADMIN
                else -> CLIENTE
            }
        }
    }
}

data class User(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val rol: String = UserRole.CLIENTE.value,
    val fotoPerfil: String = "",
    val descripcion: String = "",
    val fechaRegistro: Long = System.currentTimeMillis(),
    val verificado: Boolean = false,
    val verificacionEstado: String = "sin_solicitar", // "sin_solicitar", "pendiente", "aprobado", "rechazado"
    val cedulaVerificada: Boolean = false,
    val numeroCedula: String = "",
    val infotepCertificado: Boolean = false,
    val certificadoInfotepNombre: String = "",
    val portfolioFotos: List<String> = emptyList(),
    val estado: String = "activo",
    val calificacionPromedio: Double = 5.0,
    val totalCalificaciones: Int = 0,
    val trabajosCompletados: Int = 0,
    val habilidades: List<String> = emptyList(),
    val experiencia: String = "",
    val categorias: List<String> = emptyList(),
    val zona: String = "Santo Domingo, RD"
) {
    fun getRoleEnum(): UserRole = UserRole.fromString(rol)
    val esCliente: Boolean get() = rol == UserRole.CLIENTE.value
    val esTrabajador: Boolean get() = rol == UserRole.TRABAJADOR.value
    val esAdmin: Boolean get() = rol == UserRole.ADMIN.value
    val tieneInsigniaOficial: Boolean get() = (verificado || cedulaVerificada || infotepCertificado) && verificacionEstado != "rechazado"
}
