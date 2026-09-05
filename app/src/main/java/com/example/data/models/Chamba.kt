package com.example.data.models

data class Chamba(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val clienteFoto: String = "",
    val clienteTelefono: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoriaId: String = "",
    val categoriaNombre: String = "",
    val ubicacion: String = "",
    val latitud: Double = 18.4861,
    val longitud: Double = -69.9312,
    val fechaTrabajo: String = "",
    val horaTrabajo: String = "",
    val precio: Double = 0.0,
    val materialesResponsable: String = "Cliente", // Cliente, Trabajador, Ambos, Se acordará
    val costoMateriales: Double = 0.0,
    val fotos: List<String> = emptyList(),
    val estado: String = ChambaState.PUBLICADA.key,
    val trabajadorSeleccionadoId: String = "",
    val trabajadorSeleccionadoNombre: String = "",
    val trabajadorSeleccionadoFoto: String = "",
    val evidenciaFotos: List<String> = emptyList(),
    val notasTerminado: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
) {
    val estadoEnum: ChambaState get() = ChambaState.fromKey(estado)
    val precioTotalEstimado: Double get() = precio + costoMateriales
    val precioFormateado: String get() = "RD$" + String.format("%,.0f", precio)
    val totalFormateado: String get() = "RD$" + String.format("%,.0f", precioTotalEstimado)
}
