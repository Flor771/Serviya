package com.example.ui.components

import com.example.R



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.Chamba
import com.example.data.models.ChambaState
import com.example.data.models.User
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodels.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChambaTopAppBar(
    currentUser: User?,
    unreadCount: Int,
    onNavigate: (Screen) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onSearchClick: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Slogan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onNavigate(Screen.Home) }
                        .testTag("app_logo_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chamba_emblem_icon),
                            contentDescription = "CHAMBA RD",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CHAMBA RD",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = "«Publica tu chamba. Encuentra quién la haga.»",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Actions: Search, Role Switcher, Notifications, Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (currentUser?.getRoleEnum()) {
                            UserRole.TRABAJADOR -> ChambaAmber
                            UserRole.ADMIN -> DominicanRed
                            else -> ChambaBlueAccent
                        },
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = when (currentUser?.getRoleEnum()) {
                                    UserRole.TRABAJADOR -> "TÉCNICO"
                                    UserRole.ADMIN -> "ADMINISTRADOR"
                                    else -> "CLIENTE"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Search button
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("top_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar chambas",
                            tint = Color.White
                        )
                    }

                    // Notifications button with badge
                    IconButton(
                        onClick = { onNavigate(Screen.Notifications) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = DominicanRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(unreadCount.toString(), fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White
                            )
                        }
                    }

                    // User avatar
                    if (currentUser != null) {
                        AsyncImage(
                            model = currentUser.fotoPerfil.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120" },
                            contentDescription = "Perfil",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onNavigate(Screen.Profile) }
                                .testTag("top_avatar_button"),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChambaBottomNavigationBar(
    currentUser: User?,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val isWorker = currentUser?.esTrabajador == true

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Inicio
        NavigationBarItem(
            selected = currentScreen is Screen.Home,
            onClick = { onNavigate(Screen.Home) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_home_button")
        )

        if (isWorker) {
            // Trabajador Tab 2: Buscar
            NavigationBarItem(
                selected = currentScreen is Screen.Search,
                onClick = { onNavigate(Screen.Search) },
                icon = {
                    Icon(
                        imageVector = if (currentScreen is Screen.Search) Icons.Filled.Search else Icons.Outlined.Search,
                        contentDescription = "Buscar"
                    )
                },
                label = { Text("Buscar", fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_search_button")
            )
        }

        // Tab: Mis Chambas / Postulaciones
        NavigationBarItem(
            selected = currentScreen is Screen.MyChambas,
            onClick = { onNavigate(Screen.MyChambas) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.MyChambas) Icons.Filled.Work else Icons.Outlined.WorkOutline,
                    contentDescription = "Mis Chambas"
                )
            },
            label = { Text("Mis chambas", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_my_chambas_button")
        )

        if (!isWorker) {
            // Cliente Tab 3: Publicar (+)
            NavigationBarItem(
                selected = currentScreen is Screen.PublishChamba,
                onClick = { onNavigate(Screen.PublishChamba) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Publicar Chamba",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = { Text("Publicar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary) },
                modifier = Modifier.testTag("nav_publish_button")
            )
        }

        // Tab: Mensajes
        NavigationBarItem(
            selected = currentScreen is Screen.Messages || currentScreen is Screen.ChatConversation,
            onClick = { onNavigate(Screen.Messages) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Messages) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Mensajes"
                )
            },
            label = { Text("Mensajes", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_messages_button")
        )

        // Tab: Perfil
        NavigationBarItem(
            selected = currentScreen is Screen.Profile,
            onClick = { onNavigate(Screen.Profile) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Profile) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_profile_button")
        )
    }
}

@Composable
fun StateBadge(state: ChambaState, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = state.badgeColor.copy(alpha = 0.14f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(state.badgeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = state.displayName,
                color = state.badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ChambaCard(
    chamba: Chamba,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("chamba_card_${chamba.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Photo & State Badge Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = chamba.fotos.firstOrNull() ?: "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600",
                    contentDescription = chamba.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Category Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChambaNavyPrimary.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = chamba.categoriaNombre,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // State Badge
                StateBadge(
                    state = chamba.estadoEnum,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )

                // Price Tag Floating Bottom
                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp),
                    color = DominicanRed,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = chamba.precioFormateado,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Info Details
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = chamba.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = chamba.descripcion,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = DominicanRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chamba.ubicacion,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Fecha",
                            tint = ChambaBlueAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chamba.fechaTrabajo,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

val WhatsAppGreen = Color(0xFF25D366)

fun openWhatsApp(context: android.content.Context, phone: String, message: String) {
    val cleanPhone = phone.replace(Regex("[^0-9]"), "").let {
        if (it.startsWith("809") || it.startsWith("829") || it.startsWith("849")) "1$it" else it
    }
    val encodedMsg = java.net.URLEncoder.encode(message, "UTF-8")
    val url = if (cleanPhone.isNotEmpty()) {
        "https://wa.me/$cleanPhone?text=$encodedMsg"
    } else {
        "https://wa.me/?text=$encodedMsg"
    }
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No se pudo abrir WhatsApp: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
