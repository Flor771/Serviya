package com.example.data.models

data class PriceGuideItem(
    val id: String,
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    val precioMinimoRD: Double,
    val precioPromedioRD: Double,
    val precioMaximoRD: Double,
    val tipoCobro: String, // "Por punto/instalación", "Por m²", "Por unidad", "Por día/jornada", "Por servicio"
    val unidadTexto: String, // "puntos", "m²", "aires", "días", "visitas"
    val tiempoEstimado: String,
    val consejoDominicano: String,
    val incluyeMateriales: Boolean = false,
    val materialesSugeridos: List<String> = emptyList()
)

object DominicanPriceGuideData {
    val items: List<PriceGuideItem> = listOf(
        // Electricidad
        PriceGuideItem(
            id = "elec_1",
            categoria = "Electricidad",
            titulo = "Instalación de Punto Eléctrico / Tomacorriente",
            descripcion = "Cableado, colocación de caja 2x4, interruptor o tomacorriente doble con puesta a tierra.",
            precioMinimoRD = 500.0,
            precioPromedioRD = 800.0,
            precioMaximoRD = 1200.0,
            tipoCobro = "Por punto",
            unidadTexto = "puntos",
            tiempoEstimado = "1 - 2 horas por punto",
            consejoDominicano = "Verifica que el cable sea calibre 12 AWG para tomas y 14 AWG para iluminación. Pide al electricista balancear los breakers.",
            materialesSugeridos = listOf("Cable #12 THHN", "Tomacorrientes polarizados", "Cajas plásticas/metálicas", "Cinta 3M Super 33+")
        ),
        PriceGuideItem(
            id = "elec_2",
            categoria = "Electricidad",
            titulo = "Instalación de Lámpara o Abanico de Techo",
            descripcion = "Fijación a placa de techo, conexionado eléctrico y prueba de balance y velocidades.",
            precioMinimoRD = 1000.0,
            precioPromedioRD = 1500.0,
            precioMaximoRD = 2500.0,
            tipoCobro = "Por unidad",
            unidadTexto = "lámparas/abanicos",
            tiempoEstimado = "1 - 3 horas",
            consejoDominicano = "Si es abanico de techo con lámpara pesada, exige que la caja octagonal esté anclada firmemente a la losa o viga.",
            materialesSugeridos = listOf("Tarugos y tornillos de expansión", "Conectores de resorte", "Cinta aislante")
        ),
        PriceGuideItem(
            id = "elec_3",
            categoria = "Electricidad",
            titulo = "Instalación de Inversor y Baterías",
            descripcion = "Montaje de inversor de 1.5 a 3.5 kW, conexionado de banco de 2 a 4 baterías, breaker de transferencia y calibración de carga.",
            precioMinimoRD = 2500.0,
            precioPromedioRD = 4000.0,
            precioMaximoRD = 6500.0,
            tipoCobro = "Por instalación",
            unidadTexto = "sistemas",
            tiempoEstimado = "3 - 5 horas",
            consejoDominicano = "Asegúrate de colocar las baterías en lugar ventilado y utilizar terminales de plomo/cobre bien estañados para evitar sulfatación.",
            materialesSugeridos = listOf("Cables de batería calibre 2 o 4", "Breaker de transferencia 30A/50A", "Base plástica para baterías")
        ),

        // Plomería
        PriceGuideItem(
            id = "plom_1",
            categoria = "Plomería",
            titulo = "Reparación o Cambio de Mezcladora / Llave",
            descripcion = "Desmonte de mezcladora vieja en fregadero, lavamanos o ducha, teflón y sellado hermético.",
            precioMinimoRD = 600.0,
            precioPromedioRD = 1000.0,
            precioMaximoRD = 1600.0,
            tipoCobro = "Por servicio",
            unidadTexto = "llaves/mezcladoras",
            tiempoEstimado = "1 hora",
            consejoDominicano = "Compra mangueras flexibles de acero inoxidable para mayor durabilidad contra el agua dura o salobre.",
            materialesSugeridos = listOf("Teflón de alta densidad", "Mangueras flexibles 1/2\"", "Masilla de plomero o silicón antihongos")
        ),
        PriceGuideItem(
            id = "plom_2",
            categoria = "Plomería",
            titulo = "Instalación de Inodoro (Sanitario)",
            descripcion = "Nivelación, cuello de cera nuevo, fijación al piso con pernos de bronce, conexión de agua y silicón sanitario perimetral.",
            precioMinimoRD = 1200.0,
            precioPromedioRD = 1800.0,
            precioMaximoRD = 2800.0,
            tipoCobro = "Por unidad",
            unidadTexto = "inodoros",
            tiempoEstimado = "2 - 3 horas",
            consejoDominicano = "Siempre exige un cuello de cera nuevo con guía plástica para evitar filtraciones y malos olores en el baño.",
            materialesSugeridos = listOf("Cuello de cera con guía", "Pernos de anclaje de bronce", "Llave de paso angular", "Silicón antihongos")
        ),
        PriceGuideItem(
            id = "plom_3",
            categoria = "Plomería",
            titulo = "Destape de Tubería o Drenaje Principal",
            descripcion = "Sondeo con cinta desatascadora o guaya manual/mecánica de trampas, desagües de piso o tubería sanitaria.",
            precioMinimoRD = 1500.0,
            precioPromedioRD = 2500.0,
            precioMaximoRD = 4500.0,
            tipoCobro = "Por servicio",
            unidadTexto = "destapes",
            tiempoEstimado = "1 - 3 horas",
            consejoDominicano = "Evita usar ácido muriático concentrado en exceso si tus tuberías son de PVC delgado o viejas.",
            materialesSugeridos = listOf("Desengrasante industrial", "Trampa de grasa si es en cocina")
        ),

        // Climatización y Refrigeración
        PriceGuideItem(
            id = "refri_1",
            categoria = "Refrigeración",
            titulo = "Mantenimiento Preventivo de Aire Acondicionado Split (12k a 24k BTU)",
            descripcion = "Limpieza profunda de evaporador, turbina, bandeja de condensado, serpentín de condensador con hidrolavadora y chequeo de amperaje y gas refrigerante.",
            precioMinimoRD = 1200.0,
            precioPromedioRD = 1800.0,
            precioMaximoRD = 2800.0,
            tipoCobro = "Por unidad",
            unidadTexto = "aires",
            tiempoEstimado = "1.5 - 2 horas por aire",
            consejoDominicano = "En RD con el calor y polvo, se recomienda cada 4 a 6 meses para reducir el consumo en la factura de EDESUR/EDEESTE/EDENORTE.",
            materialesSugeridos = listOf("Limpiador químico alcalino para serpentines", "Bolsa protectora de lavado", "Filtro anti-bacterias")
        ),
        PriceGuideItem(
            id = "refri_2",
            categoria = "Refrigeración",
            titulo = "Instalación Básica de Aire Inverter Nuevo",
            descripcion = "Perforación de pared, montaje de ménsula externa, interconexión de tubería de cobre hasta 3 metros, vacío con bomba y prueba eléctrica.",
            precioMinimoRD = 2800.0,
            precioPromedioRD = 4200.0,
            precioMaximoRD = 6500.0,
            tipoCobro = "Por instalación",
            unidadTexto = "equipos",
            tiempoEstimado = "3 - 5 horas",
            consejoDominicano = "Exige que hagan vacío con bomba de vacío de mínimo 15 minutos; si solo purgan con el mismo gas, se anula la garantía del compresor.",
            materialesSugeridos = listOf("Ménsulas galvanizadas para exterior", "Breaker bifásico 20A/30A", "Tubo aislante armaflex", "Cinta vinil")
        ),

        // Pintura
        PriceGuideItem(
            id = "pint_1",
            categoria = "Pintura",
            titulo = "Pintura de Interiores (Mano de Obra por m²)",
            descripcion = "Resanado menor de grietas con masilla, lijado suave y aplicación de dos manos de pintura acrílica/látex.",
            precioMinimoRD = 80.0,
            precioPromedioRD = 140.0,
            precioMaximoRD = 220.0,
            tipoCobro = "Por m²",
            unidadTexto = "m²",
            tiempoEstimado = "Depende del metraje (~35 m² por día)",
            consejoDominicano = "Para salas y pasillos con alto tráfico, la pintura semigloss o satinada es mucho más fácil de limpiar que la mate.",
            materialesSugeridos = listOf("Pintura Acrílica de calidad (Tropical, Popular, Tucán)", "Masilla acrílica para grietas", "Lija #150 y #220", "Tape azul de pintor")
        ),
        PriceGuideItem(
            id = "pint_2",
            categoria = "Pintura",
            titulo = "Pintura Completa de Apartamento / Casa (Mano de Obra)",
            descripcion = "Pintura general de techos en blanco mate y paredes en color a elección, incluyendo resanes y empapelado protector de rodapiés.",
            precioMinimoRD = 7000.0,
            precioPromedioRD = 13500.0,
            precioMaximoRD = 25000.0,
            tipoCobro = "Por proyecto",
            unidadTexto = "apartamentos",
            tiempoEstimado = "2 - 4 días",
            consejoDominicano = "Acuerda por escrito si el pintor se encarga de mover y tapar muebles y la limpieza final de pisos.",
            materialesSugeridos = listOf("Cubetas de pintura látex", "Plásticos protectores", "Rodillos antigota", "Brochas de cerda fina")
        ),

        // Albañilería & Construcción
        PriceGuideItem(
            id = "alban_1",
            categoria = "Albañilería",
            titulo = "Instalación de Cerámica o Porcelanato (Piso o Pared)",
            descripcion = "Nivelación de superficie, mezcla de pegamento de porcelanato (Bondex/Capa gruesa), colocación con crucetas y emboquillado con fragua.",
            precioMinimoRD = 350.0,
            precioPromedioRD = 550.0,
            precioMaximoRD = 900.0,
            tipoCobro = "Por m²",
            unidadTexto = "m²",
            tiempoEstimado = "10 - 15 m² por jornada",
            consejoDominicano = "Si el formato es mayor a 60x60 cm, se requiere doble encolado y pegamento flexible tipo C2 para evitar que se despeguen.",
            materialesSugeridos = listOf("Pegamento Bondex Plus / Pegamas", "Crucetas niveladoras", "Fragua anti-humedad", "Disco de diamante para corte")
        ),
        PriceGuideItem(
            id = "alban_2",
            categoria = "Albañilería",
            titulo = "Jornada de Maestro Constructor / Albañil (Día)",
            descripcion = "Día completo de trabajo (8:00 AM a 5:00 PM) para reparaciones generales, empañete, zapatas, muros o vaciados.",
            precioMinimoRD = 1800.0,
            precioPromedioRD = 2600.0,
            precioMaximoRD = 3800.0,
            tipoCobro = "Por día/jornada",
            unidadTexto = "días de trabajo",
            tiempoEstimado = "8 horas por jornada",
            consejoDominicano = "Si el trabajo requiere ayudante (peón), el costo promedio del ayudante ronda entre RD$ 1,000 y RD$ 1,400 por día.",
            materialesSugeridos = listOf("Cemento Cibao/Titán", "Arena lavada", "Gravilla", "Varillas de acero si aplica")
        ),

        // Cerrajería & Seguridad
        PriceGuideItem(
            id = "cerr_1",
            categoria = "Cerrajería",
            titulo = "Instalación o Cambio de Cerradura de Pomo / Manija",
            descripcion = "Desmonte de cerradura anterior, ajuste de cajeado en madera o metal, instalación de contra y calibración de pestillo.",
            precioMinimoRD = 700.0,
            precioPromedioRD = 1200.0,
            precioMaximoRD = 1800.0,
            tipoCobro = "Por unidad",
            unidadTexto = "cerraduras",
            tiempoEstimado = "1 hora",
            consejoDominicano = "Para puertas principales de exterior, complementa con un cerrojo de seguridad auxiliar (deadbolt).",
            materialesSugeridos = listOf("Cerradura de marca certificada (Yale, Kwikset, Schlage)", "Tornillos largos de refuerzo para el marco")
        ),
        PriceGuideItem(
            id = "cerr_2",
            categoria = "Cerrajería",
            titulo = "Apertura de Puerta Trancada de Emergencia",
            descripcion = "Servicio de apertura no destructiva mediante ganzúa o técnica profesional para residencias o vehículos.",
            precioMinimoRD = 1500.0,
            precioPromedioRD = 2400.0,
            precioMaximoRD = 4000.0,
            tipoCobro = "Por servicio",
            unidadTexto = "aperturas",
            tiempoEstimado = "30 - 60 minutos",
            consejoDominicano = "El cerrajero siempre solicitará verificar tu cédula de identidad y comprobar que resides en el inmueble por seguridad.",
            materialesSugeridos = listOf("Lubricante de grafito para cilindros")
        ),

        // Limpieza Profunda
        PriceGuideItem(
            id = "limp_1",
            categoria = "Limpieza",
            titulo = "Limpieza Profunda Post-Construcción o Mudanza",
            descripcion = "Desmanche de pisos de cemento/pintura, lavado a presión de cristales, desengrase de gabinetes de cocina y desinfección de baños.",
            precioMinimoRD = 3500.0,
            precioPromedioRD = 6000.0,
            precioMaximoRD = 11000.0,
            tipoCobro = "Por servicio completo",
            unidadTexto = "limpiezas",
            tiempoEstimado = "4 - 8 horas",
            consejoDominicano = "Ideal contratar cuando los pintores y albañiles hayan finalizado completamente para evitar retrabajos.",
            materialesSugeridos = listOf("Desincrustante para cemento", "Limpiador multiuso alcalino", "Pulidora de pisos si aplica")
        )
    )
}
