package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CustomerSupportConfig
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSupportScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val config by viewModel.customerSupportConfig.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val isAdmin = currentUser?.esAdmin == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Atención al Cliente",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "CHAMBA RD • Soporte Oficial",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("support_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("admin_edit_support_icon_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar datos de atención",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChambaNavyPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .testTag("customer_support_scroll_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ChambaNavyPrimary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = DominicanGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Centro de Ayuda CHAMBA RD",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Asistencia directa en República Dominicana",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "¿Tienes alguna duda con una chamba, pago o postulación? Contáctanos a través de nuestros canales oficiales. Estamos listos para ayudarte.",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DominicanRed.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Rol Admin: Puedes editar estos números",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    TextButton(
                                        onClick = { showEditDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Modificar", color = DominicanGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sección: Canales Oficiales de Contacto
            item {
                Text(
                    text = "Canales de Contacto Directo",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Opción 1: Teléfono de Atención (📞 Llamar a CHAMBA RD)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(ChambaNavyPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Teléfono",
                                    tint = ChambaNavyPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Llamada Telefónica",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = config.phone,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ChambaNavyPrimary
                                    )
                                )
                                Text(
                                    text = "Voz directa con soporte",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                launchPhoneDialer(context, config.cleanPhoneForDial)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChambaNavyPrimary
                            ),
                            modifier = Modifier.testTag("call_support_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Llamar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Opción 2: WhatsApp de Atención (💬 WhatsApp)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(ChambaEmeraldDark.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = ChambaEmeraldDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "WhatsApp Oficial",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = config.whatsapp,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ChambaEmeraldDark
                                    )
                                )
                                Text(
                                    text = "Chat y envío de comprobantes",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                launchWhatsAppChat(
                                    context,
                                    config.cleanWhatsappForLink,
                                    config.whatsappWelcomeMessage
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChambaEmeraldDark
                            ),
                            modifier = Modifier.testTag("whatsapp_support_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Opción 3: Correo de Atención (si corresponde)
            if (config.email.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(ChambaBlueAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Correo",
                                        tint = ChambaBlueAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Correo de Soporte",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = config.email,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Consultas formales y contratos",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { launchEmailClient(context, config.email) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("email_support_button")
                            ) {
                                Text("Escribir", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Horario de Atención
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DominicanGold.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_hours_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DominicanGold.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Horario",
                                    tint = ChambaNavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Horario Oficial de Atención",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ChambaNavyPrimary
                                    )
                                )
                                Text(
                                    text = "Zona horaria: Santo Domingo (GMT-4)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = config.businessHours,
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

            // Preguntas Frecuentes / Consejos de Seguridad
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ChambaNavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Garantía y Seguridad CHAMBA RD",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ChambaNavyPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "• Custodia de Pagos: Tu dinero se retiene de forma segura y solo se libera cuando confirmas que el trabajo fue completado satisfactoriamente.\n" +
                                "• Soporte en Disputas: Si surge un desacuerdo sobre mano de obra o materiales, contáctanos de inmediato para mediar.\n" +
                                "• Identidad Verificada: Los técnicos con insignia cuentan con Cédula Dominicana o Certificado INFOTEP validado.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            // Botón Administrativo Rápido (Solo visible si es Admin)
            if (isAdmin) {
                item {
                    Button(
                        onClick = { showEditDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DominicanRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_edit_support_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODIFICAR NÚMERO Y HORARIO (ADMIN)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showEditDialog && isAdmin) {
        EditCustomerSupportDialog(
            currentConfig = config,
            onDismiss = { showEditDialog = false },
            onSave = { newPhone, newWhatsapp, newHours, newEmail, newMsg ->
                viewModel.updateCustomerSupportConfig(
                    phone = newPhone,
                    whatsapp = newWhatsapp,
                    businessHours = newHours,
                    email = newEmail,
                    whatsappWelcomeMessage = newMsg,
                    onSuccess = {
                        showEditDialog = false
                    }
                )
            }
        )
    }
}

@Composable
fun EditCustomerSupportDialog(
    currentConfig: CustomerSupportConfig,
    onDismiss: () -> Unit,
    onSave: (phone: String, whatsapp: String, hours: String, email: String, welcomeMsg: String) -> Unit
) {
    var phone by remember { mutableStateOf(currentConfig.phone) }
    var whatsapp by remember { mutableStateOf(currentConfig.whatsapp) }
    var hours by remember { mutableStateOf(currentConfig.businessHours) }
    var email by remember { mutableStateOf(currentConfig.email) }
    var welcomeMsg by remember { mutableStateOf(currentConfig.whatsappWelcomeMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = DominicanRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configuración de Atención",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Esta información será visible para todos los clientes y técnicos sin necesidad de recompilar la aplicación.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono de Atención") },
                    placeholder = { Text("Ej: 809-555-0150") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_input_support_phone"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp de Atención") },
                    placeholder = { Text("Ej: 18095550150") },
                    leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_input_support_whatsapp"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Horario de Atención") },
                    placeholder = { Text("Ej: Lun-Vie 8AM-6PM, Sáb 9AM-1PM") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_input_support_hours"),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo de Atención") },
                    placeholder = { Text("Ej: soporte@chambard.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_input_support_email"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = welcomeMsg,
                    onValueChange = { welcomeMsg = it },
                    label = { Text("Mensaje inicial WhatsApp") },
                    placeholder = { Text("Texto que verá el usuario al abrir WhatsApp") },
                    leadingIcon = { Icon(Icons.Default.Message, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_input_support_welcome_msg"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(phone, whatsapp, hours, email, welcomeMsg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary),
                modifier = Modifier.testTag("admin_save_support_btn")
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Helpers para abrir enlaces nativos
fun launchPhoneDialer(context: Context, cleanPhone: String) {
    try {
        val numberToDial = cleanPhone.ifEmpty { "8095550150" }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$numberToDial")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir el marcador telefónico: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsAppChat(context: Context, cleanWhatsapp: String, welcomeMsg: String) {
    try {
        val number = cleanWhatsapp.ifEmpty { "18095550150" }
        val encodedMessage = Uri.encode(welcomeMsg.ifEmpty { "Hola CHAMBA RD, necesito asistencia." })
        val uri = Uri.parse("https://wa.me/$number?text=$encodedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun launchEmailClient(context: Context, email: String) {
    try {
        val mailTo = email.ifEmpty { "soporte@chambard.com" }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$mailTo?subject=${Uri.encode("Atención al Cliente - CHAMBA RD")}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir el cliente de correo: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
