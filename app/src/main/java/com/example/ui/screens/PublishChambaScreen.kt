package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.DefaultCategories
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishChambaScreen(viewModel: MainViewModel) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var categoriaDropdownExpanded by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf("Santo Domingo, Distrito Nacional") }
    var fecha by remember { mutableStateOf("Mañana") }
    var hora by remember { mutableStateOf("8:30 AM") }
    var precioStr by remember { mutableStateOf("") }
    var materialesResponsable by remember { mutableStateOf("Cliente") }
    var costoMaterialesStr by remember { mutableStateOf("0") }
    var fotos by remember { mutableStateOf(listOf("https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600")) }
    var isLoading by remember { mutableStateOf(false) }

    val categories = DefaultCategories.list
    val materialesOpciones = listOf("Cliente", "Trabajador", "Ambos", "Se acordará")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.Home) },
                modifier = Modifier.testTag("publish_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
            Column {
                Text(
                    text = "Publicar Nueva Chamba",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChambaNavyPrimary
                    )
                )
                Text(
                    text = "Describe el trabajo y fija tu presupuesto",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Basic Info Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. DETALLES DEL TRABAJO",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChambaNavyPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la chamba *") },
                    placeholder = { Text("Ej. Pintar una habitación, Instalar abanico...") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción completa *") },
                    placeholder = { Text("Explica qué hay que hacer, medidas, detalles y requerimientos...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_description_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoriaDropdownExpanded,
                    onExpandedChange = { categoriaDropdownExpanded = !categoriaDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = categories[selectedCategoryIndex].nombre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("publish_category_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = categoriaDropdownExpanded,
                        onDismissRequest = { categoriaDropdownExpanded = false }
                    ) {
                        categories.forEachIndexed { index, cat ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(cat.nombre, fontWeight = FontWeight.Bold)
                                        Text(cat.subcategorias.take(3).joinToString(", "), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedCategoryIndex = index
                                    categoriaDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location & Time Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. UBICACIÓN Y FECHA",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChambaNavyPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación / Sector / Ciudad *") },
                    placeholder = { Text("Ej. Naco, Santo Domingo Este, Santiago...") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = DominicanRed) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_location_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sugerencias rápidas de zonas:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(com.example.data.models.DominicanLocations.popularSectors) { sector ->
                        AssistChip(
                            onClick = { ubicacion = sector },
                            label = { Text(sector, fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = DominicanRed
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha estimada") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("publish_date_input")
                    )

                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("publish_time_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price and Materials Card (Requirements #8 and #9)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. PRECIO Y MATERIALES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChambaNavyPrimary
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ChambaAmberLight
                    ) {
                        Text(
                            text = "EL CLIENTE FIJA EL PRECIO",
                            color = ChambaAmberDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = precioStr,
                    onValueChange = { precioStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Mano de obra (Pesos Dominicanos RD$) *") },
                    placeholder = { Text("Ej. 3500") },
                    prefix = { Text("RD$ ", fontWeight = FontWeight.Bold, color = DominicanRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_price_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Quién proporciona los materiales?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Por regla general son del cliente, pero puedes acordar con el trabajador.",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    materialesOpciones.forEach { opcion ->
                        val isSel = materialesResponsable == opcion
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ChambaNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { materialesResponsable = opcion }
                        ) {
                            Text(
                                text = opcion,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                if (materialesResponsable == "Trabajador" || materialesResponsable == "Ambos") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = costoMaterialesStr,
                        onValueChange = { costoMaterialesStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Presupuesto estimado para materiales (RD$)") },
                        prefix = { Text("RD$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photographs section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. FOTOGRAFÍAS DEL LUGAR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChambaNavyPrimary
                    )
                )
                Text(
                    text = "Sube fotos del área para que los trabajadores entiendan mejor la labor",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(fotos) { fotoUrl ->
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto de chamba",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(90.dp)
                                .clickable {
                                    fotos = fotos + "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600"
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Agregar foto", tint = ChambaNavyPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Añadir", fontSize = 11.sp, color = ChambaNavyPrimary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                val precioDouble = precioStr.toDoubleOrNull() ?: 0.0
                val costoMatDouble = costoMaterialesStr.toDoubleOrNull() ?: 0.0
                val selectedCat = categories[selectedCategoryIndex]

                if (titulo.trim().isEmpty() || descripcion.trim().isEmpty() || ubicacion.trim().isEmpty() || precioDouble <= 0) {
                    viewModel.showMessage("Por favor completa los campos obligatorios y un precio válido.")
                    return@Button
                }

                isLoading = true
                viewModel.publishChamba(
                    titulo = titulo,
                    descripcion = descripcion,
                    categoriaId = selectedCat.id,
                    categoriaNombre = "${selectedCat.nombre} / General",
                    ubicacion = ubicacion,
                    fecha = fecha,
                    hora = hora,
                    precio = precioDouble,
                    materialesResponsable = materialesResponsable,
                    costoMateriales = costoMatDouble,
                    fotos = fotos
                ) { createdId ->
                    isLoading = false
                    viewModel.navigateTo(Screen.ChambaDetail(createdId))
                }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_submit_button")
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "PUBLICAR CHAMBA AHORA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
