package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Chamba
import com.example.data.models.ChambaState
import com.example.data.models.Postulacion
import com.example.data.models.UserRole
import com.example.ui.components.ChambaCard
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen

@Composable
fun MyChambasScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allChambas by viewModel.chambaRepo.chambasState.collectAsState()
    val allPostulaciones by viewModel.postulacionRepo.postulacionesState.collectAsState()

    val isWorker = currentUser?.esTrabajador == true
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isWorker) "Mis Postulaciones y Trabajos" else "Mis Chambas Publicadas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = ChambaNavyPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isWorker) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Mis Postulaciones", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Trabajos Asignados", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                } else {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Activas", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("En Proceso", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Historial", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // Content
        if (isWorker) {
            if (selectedTab == 0) {
                // Mis Postulaciones
                val myPosts = allPostulaciones.filter { it.trabajadorId == currentUser?.uid }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("worker_applications_list"),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (myPosts.isEmpty()) {
                        item {
                            EmptyChambasPlaceholder(
                                title = "No te has postulado a ninguna chamba aún",
                                subtitle = "Explora las chambas disponibles y postula tu servicio para empezar a ganar ingresos."
                            )
                        }
                    } else {
                        items(myPosts) { post ->
                            val associatedChamba = allChambas.firstOrNull { it.id == post.chambaId }
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = post.chambaTitulo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ChambaNavyPrimary.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = post.estadoEnum.displayName,
                                                color = ChambaNavyPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tu propuesta: «${post.mensaje}»",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Precio: RD$ ${String.format("%,.0f", post.precioPropuesto)}",
                                            fontWeight = FontWeight.Bold,
                                            color = DominicanRed,
                                            fontSize = 13.sp
                                        )
                                        if (associatedChamba != null) {
                                            TextButton(onClick = { viewModel.navigateTo(Screen.ChambaDetail(associatedChamba.id)) }) {
                                                Text("Ver Chamba 👉", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Trabajos asignados al trabajador
                val myAssigned = allChambas.filter { it.trabajadorSeleccionadoId == currentUser?.uid }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("worker_assigned_jobs_list"),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (myAssigned.isEmpty()) {
                        item {
                            EmptyChambasPlaceholder(
                                title = "No tienes trabajos asignados en curso",
                                subtitle = "Cuando un cliente te seleccione para su chamba, aparecerá aquí."
                            )
                        }
                    } else {
                        items(myAssigned) { chamba ->
                            ChambaCard(
                                chamba = chamba,
                                onClick = { viewModel.navigateTo(Screen.ChambaDetail(chamba.id)) },
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Cliente Tabs
            val clientChambas = allChambas.filter { it.clienteId == currentUser?.uid }
            val filtered = when (selectedTab) {
                0 -> clientChambas.filter { it.estado == ChambaState.PUBLICADA.key || it.estado == ChambaState.RECIBIENDO_POSTULACIONES.key }
                1 -> clientChambas.filter { it.estado == ChambaState.TRABAJADOR_SELECCIONADO.key || it.estado == ChambaState.EN_PROCESO.key || it.estado == ChambaState.TRABAJO_TERMINADO.key }
                else -> clientChambas.filter { it.estado == ChambaState.COMPLETADA.key || it.estado == ChambaState.CANCELADA.key || it.estado == ChambaState.EN_DISPUTA.key }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("client_my_chambas_list"),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        EmptyChambasPlaceholder(
                            title = "No hay chambas en esta sección",
                            subtitle = "Publica una nueva chamba para encontrar a los mejores trabajadores de tu zona."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.PublishChamba) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DominicanRed)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+ PUBLICAR CHAMBA", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    items(filtered) { chamba ->
                        ChambaCard(
                            chamba = chamba,
                            onClick = { viewModel.navigateTo(Screen.ChambaDetail(chamba.id)) },
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChambasPlaceholder(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📋", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
