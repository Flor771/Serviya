package com.example.data.models

data class Subcategory(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = ""
)

data class Category(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val icono: String = "", // Material icon name or identifier
    val grupo: String = "", // Hogar, Transporte, Tecnología, Servicios
    val subcategorias: List<String> = emptyList()
)

object DefaultCategories {
    val list = listOf(
        Category(
            id = "hogar",
            nombre = "Hogar",
            descripcion = "Servicios y mantenimiento para tu casa o apartamento",
            icono = "Home",
            grupo = "Hogar",
            subcategorias = listOf(
                "Limpieza",
                "Pintura",
                "Plomería",
                "Electricidad",
                "Jardinería",
                "Reparaciones",
                "Montaje de muebles"
            )
        ),
        Category(
            id = "transporte",
            nombre = "Transporte",
            descripcion = "Carga, mudanzas y envíos en toda la isla",
            icono = "LocalShipping",
            grupo = "Transporte",
            subcategorias = listOf(
                "Mudanzas",
                "Carga y descarga",
                "Mensajería"
            )
        ),
        Category(
            id = "tecnologia",
            nombre = "Tecnología",
            descripcion = "Soporte técnico, redes y configuración",
            icono = "Computer",
            grupo = "Tecnología",
            subcategorias = listOf(
                "Computadoras",
                "Cámaras de seguridad",
                "Redes e internet",
                "Soporte tecnológico"
            )
        ),
        Category(
            id = "servicios",
            nombre = "Servicios Generales",
            descripcion = "Asistencia para eventos, montaje y tareas",
            icono = "Handyman",
            grupo = "Servicios",
            subcategorias = listOf(
                "Ayudante",
                "Eventos",
                "Montaje de estructuras",
                "Mantenimiento preventivo"
            )
        )
    )
}

object DominicanLocations {
    val provinces = listOf(
        "Todas",
        "Distrito Nacional",
        "Santo Domingo Este",
        "Santo Domingo Norte",
        "Santo Domingo Oeste",
        "Santiago",
        "La Vega",
        "San Cristóbal",
        "Puerto Plata",
        "San Pedro de Macorís",
        "La Romana",
        "Higüey / Bávaro",
        "San Francisco de Macorís",
        "Baní",
        "Bonao",
        "Azua",
        "Barahona"
    )

    val popularSectors = listOf(
        "Piantini (D.N.)",
        "Naco (D.N.)",
        "Bella Vista (D.N.)",
        "Gazcue (D.N.)",
        "Alma Rosa (SDE)",
        "Los Mina (SDE)",
        "Villa Mella (SDN)",
        "Herrera (SDO)",
        "Los Jardines (Santiago)",
        "Cerros de Gurabo (Santiago)"
    )
}
