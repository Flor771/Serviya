package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DominicanPriceGuideData
import com.example.data.models.PriceGuideItem
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceEstimatorScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val items = DominicanPriceGuideData.items
    var selectedCategory by remember { mutableStateOf("Todas") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemForCalculator by remember { mutableStateOf<PriceGuideItem?>(null) }
    var quantity by remember { mutableStateOf(1) }

    val categories = listOf("Todas") + items.map { it.categoria }.distinct()

    val filteredItems = items.filter { item ->
        val matchesCat = selectedCategory == "Todas" || item.categoria.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isEmpty() ||
                item.titulo.contains(searchQuery, ignoreCase = true) ||
                item.descripcion.contains(searchQuery, ignoreCase = true) ||
                item.categoria.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "DO")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Estimador de Tarifas RD$",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "🇩🇴", fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("price_estimator_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChambaNavyPrimary)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(ChambaNavyPrimary, DominicanBlue)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = DominicanGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = DominicanGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Guía Oficial de Precios en RD$",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Conoce las tarifas justas de mano de obra en Santo Domingo, Santiago y todo el país. Cotiza con confianza y evita cobros excesivos.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Search Bar
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("price_estimator_search_input"),
                        placeholder = { Text("Buscar servicio (ej. aire, inodoro, pintura, inversor)...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = ChambaNavyPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DominicanBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Categories Horizontal Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChambaNavyPrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Price Calculator Modal / Expandable Card if item selected
            selectedItemForCalculator?.let { calcItem ->
                item {
                    val totalMin = calcItem.precioMinimoRD * quantity
                    val totalAvg = calcItem.precioPromedioRD * quantity
                    val totalMax = calcItem.precioMaximoRD * quantity

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("calculator_result_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = ChambaEmeraldDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Calculadora Rápida",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = ChambaNavyPrimary
                                    )
                                }
                                IconButton(
                                    onClick = { selectedItemForCalculator = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar calculadora", modifier = Modifier.size(18.dp))
                                }
                            }

                            Text(
                                text = calcItem.titulo,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quantity Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cantidad de ${calcItem.unidadTexto}:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "$quantity",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    IconButton(
                                        onClick = { quantity++ },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Sumar", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Calculated Result Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Presupuesto Estimado Sugerido:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "RD$ ${String.format(Locale.US, "%,.0f", totalAvg)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = ChambaEmeraldDark
                                    )
                                    Text(
                                        text = "Rango de mercado: RD$ ${String.format(Locale.US, "%,.0f", totalMin)} - RD$ ${String.format(Locale.US, "%,.0f", totalMax)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.navigateTo(Screen.PublishChamba)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("publish_with_estimate_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Publicar Chamba con esta Tarifa",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tarifario Promedio RD (${filteredItems.size} servicios)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ChambaNavyPrimary
                    )
                }
            }

            // List of items
            items(filteredItems, key = { it.id }) { item ->
                PriceGuideCard(
                    item = item,
                    onCalculateClick = {
                        selectedItemForCalculator = item
                        quantity = 1
                    }
                )
            }

            // Market Reference Notice Footer
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = DominicanBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Notas sobre Tarifas en República Dominicana",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DominicanBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Las tarifas corresponden a mano de obra estándar de técnicos independientes.\n" +
                                    "• En el Distrito Nacional, Piantini, Bella Vista y Zonas Turísticas (Punta Cana / Las Terrenas) los precios pueden variar un 15% - 25% por costos de traslado.\n" +
                                    "• Siempre acuerda con el trabajador si los materiales están incluidos antes de iniciar la labor.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PriceGuideCard(
    item: PriceGuideItem,
    onCalculateClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { isExpanded = !isExpanded }
            .testTag("price_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category Badge & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DominicanBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.categoria.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = DominicanBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = item.tipoCobro,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ChambaNavyPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.descripcion,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Price highlight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tarifa Promedio",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "RD$ ${String.format(Locale.US, "%,.0f", item.precioPromedioRD)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = ChambaEmeraldDark
                        )
                        Text(
                            text = " / ${item.unidadTexto}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Text(
                        text = "Rango: RD$ ${String.format(Locale.US, "%,.0f", item.precioMinimoRD)} - ${String.format(Locale.US, "%,.0f", item.precioMaximoRD)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCalculateClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calcular", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Expandable details (Consejo & Materiales)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Tiempo Estimado
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ChambaAmber, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Tiempo estimado: ${item.tiempoEstimado}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Consejo Dominicano
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DominicanGold.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Text("💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Consejo Práctico RD:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ChambaNavyPrimary)
                                Text(item.consejoDominicano, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }

                    if (item.materialesSugeridos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Materiales típicos sugeridos:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        item.materialesSugeridos.forEach { mat ->
                            Text(" • $mat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
