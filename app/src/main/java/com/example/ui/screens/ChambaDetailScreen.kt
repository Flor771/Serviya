package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.*
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
import com.example.data.models.Chamba
import com.example.data.models.ChambaState
import com.example.data.models.Postulacion
import com.example.data.models.PostulacionState
import com.example.data.models.User
import com.example.ui.components.StateBadge
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChambaDetailScreen(
    chambaId: String,
    viewModel: MainViewModel
) {
    val chambas by viewModel.chambaRepo.chambasState.collectAsState()
    val chamba = chambas.firstOrNull { it.id == chambaId }
    val currentUser by viewModel.currentUser.collectAsState()
    val postulaciones by viewModel.postulacionRepo.postulacionesState.collectAsState()
    val chambaPostulaciones = postulaciones.filter { it.chambaId == chambaId }
    val bankAccount by viewModel.bankAccountConfig.collectAsState()
    val commissionRate by viewModel.commissionRate.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val chambaPayment = payments.firstOrNull { it.chambaId == chambaId }

    var showApplyDialog by remember { mutableStateOf(false) }
    var showEvidenceDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showDisputeDialog by remember { mutableStateOf(false) }
    var showUploadReceiptDialog by remember { mutableStateOf(false) }

    if (chamba == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chamba no encontrada")
        }
        return
    }

    val isClientOwner = currentUser?.uid == chamba.clienteId
    val isAssignedWorker = currentUser?.uid == chamba.trabajadorSeleccionadoId
    val isWorker = currentUser?.esTrabajador == true
    val alreadyApplied = chambaPostulaciones.any { it.trabajadorId == currentUser?.uid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chamba.titulo, maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDisputeDialog = true },
                        modifier = Modifier.testTag("detail_report_button")
                    ) {
                        Icon(Icons.Outlined.Flag, contentDescription = "Reportar o Disputa", tint = DominicanRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Action Bar based on State and Role
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isWorker && !isClientOwner) {
                        if (chamba.estadoEnum == ChambaState.PUBLICADA || chamba.estadoEnum == ChambaState.RECIBIENDO_POSTULACIONES) {
                            if (alreadyApplied) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Ya te has postulado (Pendiente)")
                                }
                            } else {
                                Button(
                                    onClick = { showApplyDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("apply_chamba_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DominicanRed)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("POSTULARME A ESTA CHAMBA", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (isAssignedWorker) {
                            when (chamba.estadoEnum) {
                                ChambaState.TRABAJADOR_SELECCIONADO -> {
                                    Button(
                                        onClick = { viewModel.startWork(chamba.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("start_work_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChambaBlueAccent)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("INICIAR TRABAJO", fontWeight = FontWeight.Bold)
                                    }
                                }
                                ChambaState.EN_PROCESO -> {
                                    Button(
                                        onClick = { showEvidenceDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("finish_work_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("TRABAJO TERMINADO", fontWeight = FontWeight.Bold)
                                    }
                                }
                                ChambaState.TRABAJO_TERMINADO -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ChambaAmberLight,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Esperando confirmación del cliente para liberar fondos ⏳",
                                            color = ChambaAmberDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                                ChambaState.COMPLETADA -> {
                                    Button(
                                        onClick = { showReviewDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("CALIFICAR CLIENTE", fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    if (isClientOwner) {
                        val currentPaymentState = chambaPayment?.estado ?: "pendiente"
                        val needsPayment = chamba.trabajadorSeleccionadoId.isNotEmpty() && 
                            (currentPaymentState == "pendiente" || currentPaymentState == "rechazado") &&
                            (chamba.estadoEnum == ChambaState.TRABAJADOR_SELECCIONADO || chamba.estadoEnum == ChambaState.EN_PROCESO)

                        if (needsPayment) {
                            Button(
                                onClick = {
                                    viewModel.initiateChambaPayment(chamba, chamba.precio) {
                                        showUploadReceiptDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("pay_chamba_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DominicanRed)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PAGAR CHAMBA", fontWeight = FontWeight.Bold)
                            }
                        }

                        when (chamba.estadoEnum) {
                            ChambaState.TRABAJO_TERMINADO -> {
                                Button(
                                    onClick = { viewModel.confirmWork(chamba.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("confirm_work_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CONFIRMAR Y LIBERAR PAGO", fontWeight = FontWeight.Bold)
                                }
                            }
                            ChambaState.COMPLETADA -> {
                                Button(
                                    onClick = { showReviewDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("review_worker_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CALIFICAR AL TRABAJADOR", fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }

                    // Chat & WhatsApp Buttons
                    val chatPartnerId = if (isClientOwner) chamba.trabajadorSeleccionadoId else chamba.clienteId
                    val chatPartnerName = if (isClientOwner) chamba.trabajadorSeleccionadoNombre else chamba.clienteNombre
                    val context = androidx.compose.ui.platform.LocalContext.current

                    // Direct WhatsApp Action Button
                    val targetPhone = if (isClientOwner) "" else chamba.clienteTelefono.ifEmpty { "8095550199" }
                    IconButton(
                        onClick = {
                            val msg = if (isClientOwner) {
                                "Hola ${chamba.trabajadorSeleccionadoNombre}, te contacto desde CHAMBA RD por la publicación: ${chamba.titulo}"
                            } else {
                                "Hola ${chamba.clienteNombre}, vi tu publicación en CHAMBA RD: '${chamba.titulo}' y me interesa realizar este trabajo."
                            }
                            com.example.ui.components.openWhatsApp(context, targetPhone, msg)
                        },
                        modifier = Modifier
                            .background(com.example.ui.components.WhatsAppGreen, RoundedCornerShape(12.dp))
                            .size(48.dp)
                            .testTag("whatsapp_action_button")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White)
                    }

                    if (chatPartnerId.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.navigateTo(
                                    Screen.ChatConversation(
                                        chambaId = chamba.id,
                                        otherUserId = chatPartnerId,
                                        otherUserName = chatPartnerName
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("open_chat_button")
                        ) {
                            Icon(Icons.Default.Forum, contentDescription = "Chat Interno")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Photos Carousel
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(chamba.fotos) { fotoUrl ->
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = chamba.titulo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(240.dp, 160.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title & State
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ChambaNavyPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = chamba.categoriaNombre,
                            color = ChambaNavyPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    StateBadge(state = chamba.estadoEnum)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = chamba.titulo,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = ChambaNavyPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Price & Materials Breakdown Card
            item {
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
                            Column {
                                Text(
                                    text = "PRESUPUESTO FIJADO POR CLIENTE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = chamba.precioFormateado,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DominicanRed
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ChambaEmeraldLight
                            ) {
                                Text(
                                    text = "Mano de obra",
                                    color = ChambaEmeraldDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Responsable de materiales:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(chamba.materialesResponsable, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (chamba.costoMateriales > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Costo estimado materiales:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("RD$ ${String.format("%,.0f", chamba.costoMateriales)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Location, Date, Time details
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = DominicanRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = chamba.ubicacion, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ChambaBlueAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${chamba.fechaTrabajo} a las ${chamba.horaTrabajo}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Description
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Descripción del Trabajo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = chamba.descripcion,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Assigned Worker card (if already selected)
            if (chamba.trabajadorSeleccionadoId.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ChambaEmeraldLight.copy(alpha = 0.5f)),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("TRABAJADOR ASIGNADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChambaEmeraldDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(Screen.WorkerProfile(chamba.trabajadorSeleccionadoId)) }
                            ) {
                                AsyncImage(
                                    model = chamba.trabajadorSeleccionadoFoto.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400" },
                                    contentDescription = chamba.trabajadorSeleccionadoNombre,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(chamba.trabajadorSeleccionadoNombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Ver perfil y calificaciones 👉", fontSize = 11.sp, color = ChambaEmeraldDark)
                                }
                            }

                            if (chamba.notasTerminado.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Notas de entrega:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(chamba.notasTerminado, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SISTEMA DE TRANSFERENCIA BANCARIA Y COMISIÓN CHAMBA RD
                item {
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    val precioTrabajo = chamba.precio
                    val comisionMonto = precioTrabajo * commissionRate
                    val totalATransferir = precioTrabajo + comisionMonto
                    val gananciaTecnico = precioTrabajo
                    val estadoPago = chambaPayment?.estado ?: "pendiente"

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_payment_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = ChambaNavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pago por Transferencia Bancaria",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (estadoPago) {
                                        "liberado" -> ChambaBlueAccent
                                        "retenido", "confirmado" -> ChambaEmeraldDark
                                        "comprobante_subido", "en_revision" -> ChambaAmber
                                        "rechazado" -> DominicanRed
                                        else -> Color.Gray
                                    }
                                ) {
                                    Text(
                                        text = when (estadoPago) {
                                            "liberado" -> "PAGO LIBERADO"
                                            "retenido", "confirmado" -> "FONDOS EN CUSTODIA"
                                            "comprobante_subido", "en_revision" -> "EN REVISIÓN"
                                            "rechazado" -> "RECHAZADO"
                                            else -> "PENDIENTE DE PAGO"
                                        },
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Desglose de Pago y Comisión
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Precio del trabajo:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("RD$ ${String.format("%,.0f", precioTrabajo)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Comisión CHAMBA RD (${(commissionRate * 100).toInt()}%):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("RD$ ${String.format("%,.0f", comisionMonto)}", fontSize = 12.sp, color = DominicanRed, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total a transferir:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ChambaNavyPrimary)
                                        Text("RD$ ${String.format("%,.0f", totalATransferir)}", fontSize = 14.sp, color = DominicanRed, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Ganancia del técnico (100%):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("RD$ ${String.format("%,.0f", gananciaTecnico)}", fontSize = 12.sp, color = ChambaEmeraldDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Datos Bancarios Oficiales
                            Text(
                                text = "Cuenta Oficial de Depósito:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChambaNavyPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ChambaNavyPrimary.copy(alpha = 0.05f),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(bankAccount.bankName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Titular: ${bankAccount.accountHolder}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Tipo: ${bankAccount.accountType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (bankAccount.rncOrCedula.isNotEmpty()) {
                                                Text("RNC: ${bankAccount.rncOrCedula}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Número de Cuenta:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = bankAccount.accountNumber,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = ChambaNavyPrimary
                                            )
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(bankAccount.accountNumber))
                                                viewModel.showMessage("¡Número de cuenta copiado al portapapeles!")
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("copy_account_button")
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copiar", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Estado específico y botón para subir comprobante
                            if (isClientOwner) {
                                Spacer(modifier = Modifier.height(12.dp))

                                if (chambaPayment?.rejectionReason?.isNotEmpty() == true && estadoPago == "rechazado") {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DominicanRed.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DominicanRed, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Comprobante rechazado: ${chambaPayment.rejectionReason}",
                                                color = DominicanRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (estadoPago == "comprobante_subido" || estadoPago == "en_revision") {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ChambaAmberLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = ChambaAmberDark, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Comprobante enviado. El administrador está verificando los fondos en cuenta.",
                                                color = ChambaAmberDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else if (estadoPago == "retenido" || estadoPago == "confirmado") {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ChambaEmeraldLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = ChambaEmeraldDark, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Fondos asegurados y retenidos en custodia por CHAMBA RD hasta tu confirmación.",
                                                color = ChambaEmeraldDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else if (estadoPago == "liberado") {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ChambaBlueAccent.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChambaBlueAccent, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Pago completado y transferido al técnico.",
                                                color = ChambaBlueAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else {
                                    // Pendiente o Rechazado -> botón para subir comprobante
                                    Button(
                                        onClick = {
                                            viewModel.initiateChambaPayment(chamba, precioTrabajo) {
                                                showUploadReceiptDialog = true
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("upload_receipt_button")
                                    ) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SUBIR COMPROBANTE DE PAGO", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Postulaciones Section (for Client to review applicants)
            if (isClientOwner) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Postulaciones Recibidas (${chambaPostulaciones.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ChambaNavyPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (chambaPostulaciones.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Aún no hay postulaciones para esta chamba.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Los trabajadores de tu zona la verán y aplicarán pronto.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(chambaPostulaciones) { post ->
                        PostulacionItemCard(
                            post = post,
                            chamba = chamba,
                            onSelect = {
                                viewModel.selectWorker(chamba, post) {
                                    viewModel.initiateChambaPayment(chamba, post.precioPropuesto)
                                }
                            },
                            onViewProfile = {
                                viewModel.navigateTo(Screen.WorkerProfile(post.trabajadorId))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Dialog: Apply to Chamba
    if (showApplyDialog && currentUser != null) {
        ApplyChambaDialog(
            chamba = chamba,
            currentUser = currentUser!!,
            onDismiss = { showApplyDialog = false },
            onSubmit = { mensaje, precioPropuesto ->
                viewModel.applyToChamba(chamba, mensaje, precioPropuesto) {
                    showApplyDialog = false
                }
            }
        )
    }

    // Dialog: Finish Work Evidence
    if (showEvidenceDialog) {
        FinishWorkEvidenceDialog(
            chamba = chamba,
            onDismiss = { showEvidenceDialog = false },
            onSubmit = { notas, fotos ->
                viewModel.finishWork(chamba.id, notas, fotos)
                showEvidenceDialog = false
            }
        )
    }

    // Dialog: Review
    if (showReviewDialog && currentUser != null) {
        val targetId = if (isClientOwner) chamba.trabajadorSeleccionadoId else chamba.clienteId
        val targetName = if (isClientOwner) chamba.trabajadorSeleccionadoNombre else chamba.clienteNombre
        ReviewSubmissionDialog(
            targetName = targetName,
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitReview(chamba.id, chamba.titulo, targetId, rating, comment) {
                    showReviewDialog = false
                }
            }
        )
    }

    // Dialog: Dispute / Report
    if (showDisputeDialog && currentUser != null) {
        DisputeReportDialog(
            chamba = chamba,
            onDismiss = { showDisputeDialog = false },
            onSubmitReport = { motivo, descripcion ->
                val reportedId = if (isClientOwner) chamba.trabajadorSeleccionadoId else chamba.clienteId
                val reportedName = if (isClientOwner) chamba.trabajadorSeleccionadoNombre else chamba.clienteNombre
                viewModel.reportUser(reportedId, reportedName, chamba.id, motivo, descripcion) {
                    showDisputeDialog = false
                }
            },
            onSubmitDispute = { motivo, descripcion ->
                viewModel.openDispute(chamba.id, chamba.titulo, motivo, descripcion) {
                    showDisputeDialog = false
                }
            }
        )
    }

    // Dialog: Upload Payment Receipt
    if (showUploadReceiptDialog && currentUser != null) {
        UploadReceiptDialog(
            chamba = chamba,
            bankAccount = bankAccount,
            onDismiss = { showUploadReceiptDialog = false },
            onSubmit = { receiptUrl, receiptNotes, reference ->
                viewModel.uploadPaymentReceipt(chamba.id, receiptUrl, receiptNotes, reference) {
                    showUploadReceiptDialog = false
                }
            }
        )
    }
}

@Composable
fun PostulacionItemCard(
    post: Postulacion,
    chamba: Chamba,
    onSelect: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (post.estadoEnum == PostulacionState.SELECCIONADA) ChambaEmeraldLight.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewProfile() }
                ) {
                    AsyncImage(
                        model = post.trabajadorFoto.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400" },
                        contentDescription = post.trabajadorNombre,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.trabajadorNombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Técnico Verificado", tint = ChambaEmeraldDark, modifier = Modifier.size(14.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = ChambaAmber, modifier = Modifier.size(14.dp))
                            Text(" ${post.trabajadorCalificacion} (${post.trabajadorTrabajos} chambas)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (post.estadoEnum) {
                        PostulacionState.SELECCIONADA -> ChambaEmeraldDark
                        PostulacionState.RECHAZADA -> Color.Gray
                        else -> ChambaBlueAccent
                    }
                ) {
                    Text(
                        text = post.estadoEnum.displayName,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "«${post.mensaje}»",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Propuesta: RD$ ${String.format("%,.0f", post.precioPropuesto)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DominicanRed
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(
                        onClick = {
                            val msg = "Hola ${post.trabajadorNombre}, vi tu postulación en CHAMBA RD para '${chamba.titulo}' por RD$ ${String.format("%,.0f", post.precioPropuesto)} y me gustaría conversar contigo."
                            com.example.ui.components.openWhatsApp(context, "8095550199", msg)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(com.example.ui.components.WhatsAppGreen, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (post.estadoEnum == PostulacionState.PENDIENTE && (chamba.estadoEnum == ChambaState.PUBLICADA || chamba.estadoEnum == ChambaState.RECIBIENDO_POSTULACIONES)) {
                        Button(
                            onClick = onSelect,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary),
                            modifier = Modifier.testTag("select_worker_button_${post.id}")
                        ) {
                            Text("SELECCIONAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplyChambaDialog(
    chamba: Chamba,
    currentUser: User,
    onDismiss: () -> Unit,
    onSubmit: (String, Double) -> Unit
) {
    var mensaje by remember { mutableStateOf("Hola ${chamba.clienteNombre}, estoy disponible y cuento con las herramientas necesarias para realizar este trabajo.") }
    var precioPropuestoStr by remember { mutableStateOf(chamba.precio.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Postularme a esta Chamba", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "El cliente fijó el precio en RD$ ${String.format("%,.0f", chamba.precio)}.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    label = { Text("Mensaje de propuesta *") },
                    placeholder = { Text("Explica tu experiencia y disponibilidad...") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_message_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = precioPropuestoStr,
                    onValueChange = { precioPropuestoStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Precio propuesto (RD$)") },
                    prefix = { Text("RD$ ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = precioPropuestoStr.toDoubleOrNull() ?: chamba.precio
                    onSubmit(mensaje, price)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                modifier = Modifier.testTag("confirm_apply_button")
            ) {
                Text("ENVIAR POSTULACIÓN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun FinishWorkEvidenceDialog(
    chamba: Chamba,
    onDismiss: () -> Unit,
    onSubmit: (String, List<String>) -> Unit
) {
    var notas by remember { mutableStateOf("Trabajo concluido según los requerimientos solicitados y probado.") }
    val fotos = listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=600")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marcar Trabajo Terminado", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Añade notas y evidencias para que el cliente valide y confirme la liberación de fondos.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas de finalización *") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("finish_notes_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Evidencia fotográfica adjunta:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                AsyncImage(
                    model = fotos.first(),
                    contentDescription = "Evidencia",
                    modifier = Modifier
                        .size(120.dp, 80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(notas, fotos) },
                colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark),
                modifier = Modifier.testTag("confirm_finish_button")
            ) {
                Text("NOTIFICAR AL CLIENTE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ReviewSubmissionDialog(
    targetName: String,
    onDismiss: () -> Unit,
    onSubmit: (Double, String) -> Unit
) {
    var rating by remember { mutableStateOf(5.0) }
    var comment by remember { mutableStateOf("") }
    val quickTags = listOf("⚡ Rápido", "💯 Calidad 1A", "🤝 Muy Educado", "🛠️ Limpio", "💰 Buen Precio", "🇩🇴 100% Recomendado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar a $targetName", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Selecciona una puntuación:", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star.toDouble() }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$star estrellas",
                                tint = if (star <= rating) ChambaAmber else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (rating.toInt()) {
                        5 -> "⭐ Excelente servicio (5.0)"
                        4 -> "👍 Muy buen trabajo (4.0)"
                        3 -> "👌 Aceptable (3.0)"
                        2 -> "⚠️ Regular (2.0)"
                        else -> "❌ Mal servicio (1.0)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rating >= 4) ChambaEmeraldDark else DominicanRed
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Etiquetas rápidas:", fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickTags) { tag ->
                        AssistChip(
                            onClick = {
                                if (!comment.contains(tag)) {
                                    comment = if (comment.isEmpty()) tag else "$comment - $tag"
                                }
                            },
                            label = { Text(tag, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario o reseña *") },
                    placeholder = { Text("Describe cómo fue el trabajo realizado...") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_comment_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment.ifEmpty { "Excelente servicio y trato muy profesional." }) },
                colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary),
                modifier = Modifier.testTag("submit_review_button")
            ) {
                Text("PUBLICAR CALIFICACIÓN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DisputeReportDialog(
    chamba: Chamba,
    onDismiss: () -> Unit,
    onSubmitReport: (String, String) -> Unit,
    onSubmitDispute: (String, String) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    var motivo by remember { mutableStateOf("Incumplimiento") }
    var descripcion by remember { mutableStateOf("") }
    val motivos = listOf("Incumplimiento", "Fraude", "Comportamiento inapropiado", "Información falsa", "Otro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Soporte y Seguridad", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Reportar Usuario") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Abrir Disputa") })
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Motivo:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(motivos) { m ->
                        FilterChip(
                            selected = motivo == m,
                            onClick = { motivo = m },
                            label = { Text(m, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Detalla la situación *") },
                    placeholder = { Text("Explica detalladamente qué ocurrió...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tabIndex == 0) {
                        onSubmitReport(motivo, descripcion)
                    } else {
                        onSubmitDispute(motivo, descripcion)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DominicanRed)
            ) {
                Text(if (tabIndex == 0) "ENVIAR REPORTE" else "ABRIR DISPUTA", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun UploadReceiptDialog(
    chamba: Chamba,
    bankAccount: com.example.data.models.BankAccountConfig,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var referenceNumber by remember { mutableStateOf("REF-${(100000..999999).random()}") }
    var notes by remember { mutableStateOf("Transferencia realizada desde cuenta personal") }
    var selectedPhotoUrl by remember {
        mutableStateOf("https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600")
    }

    val sampleReceipts = listOf(
        "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600",
        "https://images.unsplash.com/photo-1554224154-26032ffc0d07?w=600",
        "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = DominicanRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subir Comprobante", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChambaNavyPrimary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Destino: ${bankAccount.bankName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Cuenta: ${bankAccount.accountNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Total a pagar: ${chamba.precioFormateado}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DominicanRed)
                    }
                }

                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = { Text("Número de Referencia Bancaria *") },
                    placeholder = { Text("Ej: REF-9283710") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_reference_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales del pago") },
                    placeholder = { Text("Ej: Banco emisor, nombre del ordenante...") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_notes_input")
                )

                Text(
                    text = "Foto o captura del comprobante:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleReceipts.forEachIndexed { index, url ->
                        val isSelected = selectedPhotoUrl == url
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, DominicanRed) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clickable { selectedPhotoUrl = url }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Comprobante ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        selectedPhotoUrl,
                        notes,
                        referenceNumber.ifEmpty { "REF-${System.currentTimeMillis()}" }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                modifier = Modifier.testTag("submit_receipt_confirm_button")
            ) {
                Text("ENVIAR COMPROBANTE DE PAGO", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
