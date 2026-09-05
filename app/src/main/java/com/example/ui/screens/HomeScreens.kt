package com.example.ui.screens

import com.example.R



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Category
import com.example.data.models.Chamba
import com.example.data.models.ChambaState
import com.example.data.models.User
import com.example.ui.components.ChambaCard
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen

@Composable
fun ClientHomeScreen(
    viewModel: MainViewModel,
    currentUser: User?
) {
    val chambas by viewModel.filteredChambas.collectAsState()
    val categories by viewModel.categoryRepo.categoriesState.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("client_home_list"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Section with Dominican Banner & Call-to-Action to Publish Chamba
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ChambaNavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background Image
                    Image(
                        painter = painterResource(id = R.drawable.hero_chamba_banner),
                        contentDescription = "Chamba RD",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        ChambaNavyDark.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = "Hola, ${currentUser?.nombre?.substringBefore(" ") ?: "Cliente"} 👋",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "¿Qué trabajo necesitas resolver hoy?",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.navigateTo(Screen.PublishChamba) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("hero_publish_chamba_button")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+ PUBLICAR CHAMBA",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Search Bar Shortcut
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.Search) }
                    .testTag("home_search_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Buscar plomero, pintor, electricista...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Estimator Banner
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DominicanBlue.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.PriceEstimator) }
                    .testTag("home_price_estimator_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DominianBlueGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🇩🇴", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calculadora & Estimador de Precios RD$",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ChambaNavyPrimary
                        )
                        Text(
                            text = "Conoce las tarifas sugeridas de mano de obra en RD",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = DominicanBlue, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Categories Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categorías de Servicios",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChambaNavyPrimary
                    )
                )
                if (selectedCatId != null) {
                    TextButton(onClick = { viewModel.setSelectedCategory(null) }) {
                        Text("Ver todas", color = DominicanRed, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    CategoryFilterChip(
                        name = "Todas",
                        isSelected = selectedCatId == null,
                        icon = Icons.Default.GridView,
                        onClick = { viewModel.setSelectedCategory(null) }
                    )
                }
                items(categories) { cat ->
                    val icon = when (cat.id) {
                        "hogar" -> Icons.Default.Home
                        "transporte" -> Icons.Default.LocalShipping
                        "tecnologia" -> Icons.Default.Computer
                        else -> Icons.Default.Handyman
                    }
                    CategoryFilterChip(
                        name = cat.nombre,
                        isSelected = selectedCatId == cat.id,
                        icon = icon,
                        onClick = {
                            viewModel.setSelectedCategory(if (selectedCatId == cat.id) null else cat.id)
                        }
                    )
                }
            }
        }

        // Dominican Province Filter
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DominicanRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ubicación en RD",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChambaNavyPrimary
                        )
                    )
                }
                if (selectedProvince != null && selectedProvince != "Todas") {
                    TextButton(
                        onClick = { viewModel.setSelectedProvince("Todas") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Ver todo el país", color = DominicanRed, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(com.example.data.models.DominicanLocations.provinces) { prov ->
                    val isSelected = (selectedProvince == prov) || (prov == "Todas" && (selectedProvince == null || selectedProvince == "Todas"))
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedProvince(prov) },
                        label = { Text(prov, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (prov != "Todas") {
                            { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChambaNavyPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = ChambaAmber
                        )
                    )
                }
            }
        }

        // Active Chambas Section Header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chambas en la Comunidad",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChambaNavyPrimary
                        )
                    )
                    Text(
                        text = "Conectando demanda y talento dominicano",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Text(
                    text = "${chambas.size} disponibles",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ChambaBlueAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Chambas List
        if (chambas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = ChambaAmber,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron chambas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sé el primero en publicar una chamba en esta categoría.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(chambas) { chamba ->
                ChambaCard(
                    chamba = chamba,
                    onClick = { viewModel.navigateTo(Screen.ChambaDetail(chamba.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun WorkerHomeScreen(
    viewModel: MainViewModel,
    currentUser: User?
) {
    val chambas by viewModel.filteredChambas.collectAsState()
    val categories by viewModel.categoryRepo.categoriesState.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val availableCount = chambas.count {
        it.estado == ChambaState.PUBLICADA.key || it.estado == ChambaState.RECIBIENDO_POSTULACIONES.key
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("worker_home_list"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Worker Dashboard Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ChambaNavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Panel de Trabajo 🛠️",
                                color = ChambaAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Hola, ${currentUser?.nombre?.substringBefore(" ") ?: "Trabajador"}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Rating badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ChambaAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser?.calificacionPromedio ?: 5.0}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Worker stats summary row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(Screen.Incomes) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Mis Ingresos", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text("Ver balance 💵", color = ChambaEmeraldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.navigateTo(Screen.MyChambas) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Trabajos Realizados", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text("${currentUser?.trabajosCompletados ?: 0} completados", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Estimator Banner for Workers
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DominicanBlue.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.PriceEstimator) }
                    .testTag("worker_price_estimator_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DominianBlueGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🇩🇴", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Guía de Precios y Tarifas RD$",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ChambaNavyPrimary
                        )
                        Text(
                            text = "Consulta cuánto cobrar por plomería, electricidad, pintura...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = DominicanBlue, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Categories filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtrar por Especialidad",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChambaNavyPrimary
                    )
                )
                if (selectedCatId != null) {
                    TextButton(onClick = { viewModel.setSelectedCategory(null) }) {
                        Text("Limpiar", color = DominicanRed, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    CategoryFilterChip(
                        name = "Todas ($availableCount)",
                        isSelected = selectedCatId == null,
                        icon = Icons.Default.Work,
                        onClick = { viewModel.setSelectedCategory(null) }
                    )
                }
                items(categories) { cat ->
                    val countInCat = chambas.count { it.categoriaId.equals(cat.id, ignoreCase = true) }
                    val icon = when (cat.id) {
                        "hogar" -> Icons.Default.Home
                        "transporte" -> Icons.Default.LocalShipping
                        "tecnologia" -> Icons.Default.Computer
                        else -> Icons.Default.Handyman
                    }
                    CategoryFilterChip(
                        name = "${cat.nombre} ($countInCat)",
                        isSelected = selectedCatId == cat.id,
                        icon = icon,
                        onClick = {
                            viewModel.setSelectedCategory(if (selectedCatId == cat.id) null else cat.id)
                        }
                    )
                }
            }
        }

        // Dominican Province Filter for Workers
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DominicanRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zona de Trabajo (RD)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChambaNavyPrimary
                        )
                    )
                }
                if (selectedProvince != null && selectedProvince != "Todas") {
                    TextButton(
                        onClick = { viewModel.setSelectedProvince("Todas") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Todo el país", color = DominicanRed, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(com.example.data.models.DominicanLocations.provinces) { prov ->
                    val isSelected = (selectedProvince == prov) || (prov == "Todas" && (selectedProvince == null || selectedProvince == "Todas"))
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedProvince(prov) },
                        label = { Text(prov, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (prov != "Todas") {
                            { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChambaNavyPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = ChambaAmber
                        )
                    )
                }
            }
        }

        // Feed Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chambas Disponibles para Postularte",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChambaNavyPrimary
                        )
                    )
                    Text(
                        text = "El cliente fija el precio. ¡Gana ingresos ahora!",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // List
        if (chambas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = ChambaAmber,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay chambas disponibles en esta categoría",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            items(chambas) { chamba ->
                ChambaCard(
                    chamba = chamba,
                    onClick = { viewModel.navigateTo(Screen.ChambaDetail(chamba.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryFilterChip(
    name: String,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ChambaNavyPrimary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else ChambaNavyPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
