package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.Review
import com.example.data.models.User
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    workerId: String,
    viewModel: MainViewModel
) {
    var worker by remember { mutableStateOf<User?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val reviews by viewModel.reviewRepo.reviewsState.collectAsState()
    val workerReviews = reviews.filter { it.receptorId == workerId }

    LaunchedEffect(workerId) {
        worker = viewModel.authRepo.getUserById(workerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del Trabajador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (worker == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val u = worker!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("worker_profile_scroll"),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Header Profile Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box {
                                AsyncImage(
                                    model = u.fotoPerfil.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400" },
                                    contentDescription = u.nombre,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                if (u.verificado) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ChambaBlueAccent,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Verificado",
                                            tint = Color.White,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = u.nombre,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = ChambaNavyPrimary
                                )
                            )

                            Text(
                                text = u.zona.ifEmpty { "Santo Domingo, República Dominicana" },
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = ChambaAmber, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${u.calificacionPromedio}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Text("Calificación", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Divider(modifier = Modifier.height(30.dp).width(1.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${u.trabajosCompletados}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ChambaEmeraldDark)
                                    Text("Chambas hechas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Divider(modifier = Modifier.height(30.dp).width(1.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (u.verificado) "Verificado" else "En revisión", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (u.verificado) ChambaBlueAccent else ChambaAmber)
                                    Text("Identidad", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    val telefono = u.telefono.ifEmpty { "8095550199" }
                                    val msg = "Hola ${u.nombre}, vi tu perfil en CHAMBA RD 🇩🇴 y me gustaría cotizar un servicio contigo."
                                    com.example.ui.components.openWhatsApp(context, telefono, msg)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.components.WhatsAppGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("whatsapp_worker_button")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CONTACTAR POR WHATSAPP 💬",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Verification & Certifications Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().testTag("worker_verification_badges_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ChambaEmeraldDark, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verificación & Certificados Oficiales",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ChambaNavyPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Cédula Verification Item
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (u.cedulaVerificada) ChambaEmeraldDark.copy(alpha = 0.08f) else Color.Gray.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🇩🇴", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (u.cedulaVerificada) "Cédula Dominicana Verificada" else "Cédula en proceso de validación",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (u.cedulaVerificada) ChambaEmeraldDark else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (u.cedulaVerificada) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChambaEmeraldDark, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(
                                            text = if (u.cedulaVerificada && u.numeroCedula.isNotEmpty()) "Doc: ${u.numeroCedula.take(3)}-******-${u.numeroCedula.takeLast(1)} (Validada en Padrón JCE)" else "Documento de identidad no adjuntado aún",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // INFOTEP Certification Item
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (u.infotepCertificado) DominicanBlue.copy(alpha = 0.08f) else Color.Gray.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏅", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (u.infotepCertificado) "Acreditación Técnica INFOTEP" else "Certificado Técnico Adicional",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (u.infotepCertificado) DominicanBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (u.infotepCertificado) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = DominicanBlue, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(
                                            text = if (u.infotepCertificado && u.certificadoInfotepNombre.isNotEmpty()) u.certificadoInfotepNombre else "No ha registrado diploma técnico INFOTEP",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bio Description Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Acerca de mí", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ChambaNavyPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = u.descripcion.ifEmpty { "Técnico profesional comprometido con la calidad y puntualidad." },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Skills Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Habilidades y Oficios", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ChambaNavyPrimary)
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val skills = if (u.habilidades.isNotEmpty()) u.habilidades else listOf("Electricidad", "Pintura", "Plomería", "Mantenimiento", "Montaje")
                                items(skills) { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ChambaNavyPrimary.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = skill,
                                            color = ChambaNavyPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Rating Breakdown Card
                item {
                    val totalRev = workerReviews.size
                    val fiveStars = workerReviews.count { it.puntuacion >= 4.5 }
                    val fourStars = workerReviews.count { it.puntuacion >= 3.5 && it.puntuacion < 4.5 }
                    val threeStars = workerReviews.count { it.puntuacion >= 2.5 && it.puntuacion < 3.5 }
                    val lowStars = workerReviews.count { it.puntuacion < 2.5 }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen de Calificaciones", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ChambaNavyPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text(
                                        text = "${u.calificacionPromedio}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ChambaNavyPrimary
                                    )
                                    Row {
                                        (1..5).forEach { star ->
                                            Icon(
                                                imageVector = if (star <= u.calificacionPromedio) Icons.Default.Star else Icons.Default.StarHalf,
                                                contentDescription = null,
                                                tint = ChambaAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$totalRev reseñas",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    RatingBarRow(label = "5★", count = fiveStars, total = totalRev)
                                    RatingBarRow(label = "4★", count = fourStars, total = totalRev)
                                    RatingBarRow(label = "3★", count = threeStars, total = totalRev)
                                    RatingBarRow(label = "1-2★", count = lowStars, total = totalRev)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Reviews Section Header & Leave Review Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reseñas de Clientes (${workerReviews.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ChambaNavyPrimary
                        )

                        OutlinedButton(
                            onClick = { showReviewDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_worker_review_dialog")
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp), tint = DominicanRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calificar ⭐", fontSize = 11.sp, color = DominicanRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (workerReviews.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Aún no tiene reseñas públicas. ¡Sé el primero en calificar su trabajo!",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(workerReviews) { rev ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = rev.autorFoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400" },
                                            contentDescription = rev.autorNombre,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(rev.autorNombre, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(rev.fecha))
                                            Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        (1..5).forEach { star ->
                                            Icon(
                                                imageVector = if (star <= rev.puntuacion) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = if (star <= rev.puntuacion) ChambaAmber else Color.LightGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(" ${rev.puntuacion}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(rev.comentario, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            if (showReviewDialog) {
                var selectedStars by remember { mutableStateOf(5.0) }
                var reviewText by remember { mutableStateOf("") }
                val quickTags = listOf("⚡ Rápido", "💯 Calidad 1A", "🤝 Muy Educado", "🛠️ Limpio", "💰 Buen Precio", "🇩🇴 100% Recomendado")

                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    title = { Text("Calificar a ${u.nombre}", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Toca las estrellas para calificar:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.Center) {
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = { selectedStars = star.toDouble() },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$star estrellas",
                                            tint = if (star <= selectedStars) ChambaAmber else Color.Gray,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = when (selectedStars.toInt()) {
                                    5 -> "⭐ Excelente servicio (5.0)"
                                    4 -> "👍 Muy buen trabajo (4.0)"
                                    3 -> "👌 Aceptable (3.0)"
                                    2 -> "⚠️ Regular (2.0)"
                                    else -> "❌ Mal servicio (1.0)"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedStars >= 4) ChambaEmeraldDark else DominicanRed
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Etiquetas rápidas:", fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(quickTags) { tag ->
                                    AssistChip(
                                        onClick = {
                                            if (!reviewText.contains(tag)) {
                                                reviewText = if (reviewText.isEmpty()) tag else "$reviewText - $tag"
                                            }
                                        },
                                        label = { Text(tag, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                label = { Text("Escribe tu opinión del servicio...") },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("worker_review_comment_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.submitReview(
                                    chambaId = "direct_${System.currentTimeMillis()}",
                                    chambaTitulo = "Servicio Profesional Directo",
                                    receptorId = u.uid,
                                    puntuacion = selectedStars,
                                    comentario = reviewText.ifEmpty { "Excelente servicio y trato muy profesional." }
                                ) {
                                    showReviewDialog = false
                                    scope.launch {
                                        worker = viewModel.authRepo.getUserById(workerId)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                            modifier = Modifier.testTag("submit_worker_review_button")
                        ) {
                            Text("PUBLICAR RESEÑA", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RatingBarRow(label: String, count: Int, total: Int) {
    val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 10.sp, modifier = Modifier.width(28.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ChambaAmber,
            trackColor = Color.LightGray.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("$count", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
