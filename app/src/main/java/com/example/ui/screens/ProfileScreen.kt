package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.User
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = ChambaNavyPrimary
                    )
                )
            }
        }

        if (currentUser == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { viewModel.navigateTo(Screen.Login) }) {
                    Text("Iniciar Sesión")
                }
            }
            return
        }

        val u = currentUser!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_scroll_list"),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Profile Card
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
                        AsyncImage(
                            model = u.fotoPerfil.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400" },
                            contentDescription = u.nombre,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = u.nombre,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ChambaNavyPrimary
                            )
                        )

                        Text(
                            text = u.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (u.getRoleEnum()) {
                                UserRole.TRABAJADOR -> ChambaAmber
                                UserRole.ADMIN -> DominicanRed
                                else -> ChambaNavyPrimary
                            }
                        ) {
                            Text(
                                text = "ROL: ${u.getRoleEnum().displayName.uppercase()}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Datos del Perfil")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Mode Switcher (Cliente o Técnico)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vista Previa de Perfil", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.switchRoleForTesting(UserRole.CLIENTE) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (u.getRoleEnum() == UserRole.CLIENTE) ChambaNavyPrimary else Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("switch_to_client_btn")
                            ) {
                                Text("Modo Cliente", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.switchRoleForTesting(UserRole.TRABAJADOR) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (u.getRoleEnum() == UserRole.TRABAJADOR) ChambaAmberDark else Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("switch_to_worker_btn")
                            ) {
                                Text("Modo Técnico", fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // DATOS PARA RECIBIR PAGOS (Exclusivo para Técnicos - Privado)
            if (u.esTrabajador) {
                item {
                    TechnicianBankAccountCard(
                        viewModel = viewModel,
                        worker = u
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Menu Items
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        if (u.esTrabajador) {
                            ProfileMenuItem(
                                title = "Mis Ingresos y Finanzas",
                                subtitle = "Balance acumulado, pagos y comisiones",
                                icon = Icons.Default.AccountBalanceWallet,
                                iconColor = ChambaEmeraldDark,
                                onClick = { viewModel.navigateTo(Screen.Incomes) }
                            )
                            Divider()
                        }

                        if (u.esAdmin) {
                            ProfileMenuItem(
                                title = "Panel de Administración",
                                subtitle = "Métricas, moderación de reportes y disputas",
                                icon = Icons.Default.AdminPanelSettings,
                                iconColor = DominicanRed,
                                onClick = { viewModel.navigateTo(Screen.AdminDashboard) }
                            )
                            Divider()
                        }

                        ProfileMenuItem(
                            title = "Notificaciones",
                            subtitle = "Alertas de postulaciones y cambios de estado",
                            icon = Icons.Default.Notifications,
                            iconColor = ChambaBlueAccent,
                            onClick = { viewModel.navigateTo(Screen.Notifications) }
                        )
                        Divider()

                        ProfileMenuItem(
                            title = "Mis Chambas",
                            subtitle = "Historial y seguimiento activo",
                            icon = Icons.Default.Work,
                            iconColor = ChambaNavyPrimary,
                            onClick = { viewModel.navigateTo(Screen.MyChambas) }
                        )
                        Divider()

                        ProfileMenuItem(
                            title = "Estimador de Tarifas RD$",
                            subtitle = "Guía de precios promedio de mano de obra",
                            icon = Icons.Default.Calculate,
                            iconColor = DominicanGold,
                            onClick = { viewModel.navigateTo(Screen.PriceEstimator) }
                        )
                        Divider()
                        
                        ProfileMenuItem(
                            title = "Políticas y Reglas",
                            subtitle = "Reglas, ventajas y funcionamiento",
                            icon = Icons.Default.Gavel,
                            iconColor = ChambaNavyPrimary,
                            onClick = { viewModel.navigateTo(Screen.Policies) }
                        )
                        Divider()

                        ProfileMenuItem(
                            title = "Ayuda / Atención al Cliente",
                            subtitle = "Llamar, WhatsApp oficial y horarios de atención",
                            icon = Icons.Default.SupportAgent,
                            iconColor = ChambaEmeraldDark,
                            onClick = { viewModel.navigateTo(Screen.CustomerSupport) }
                        )
                        Divider()

                        ProfileMenuItem(
                            title = "Verificación de Identidad & INFOTEP",
                            subtitle = if (u.cedulaVerificada || u.infotepCertificado) "Insignia oficial activa (Cédula/INFOTEP)" else "Subir Cédula Dominicana y Certificado",
                            icon = Icons.Default.VerifiedUser,
                            iconColor = if (u.cedulaVerificada) ChambaEmeraldDark else ChambaAmberDark,
                            onClick = { showVerificationDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Logout Button
            item {
                Button(
                    onClick = { viewModel.logout() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DominicanRed.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = DominicanRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CERRAR SESIÓN", color = DominicanRed, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showEditDialog && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                scope.launch {
                    viewModel.authRepo.updateUserProfile(updated)
                    viewModel.showMessage("Perfil actualizado con éxito.")
                    showEditDialog = false
                }
            }
        )
    }

    if (showVerificationDialog && currentUser != null) {
        VerificationDialog(
            user = currentUser!!,
            onDismiss = { showVerificationDialog = false },
            onSubmit = { cedula, cert ->
                viewModel.submitVerification(cedula, cert) {
                    showVerificationDialog = false
                }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var telefono by remember { mutableStateOf(user.telefono) }
    var zona by remember { mutableStateOf(user.zona) }
    var descripcion by remember { mutableStateOf(user.descripcion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono / WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = zona,
                    onValueChange = { zona = it },
                    label = { Text("Zona / Sector") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        user.copy(
                            telefono = telefono,
                            zona = zona,
                            descripcion = descripcion
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary)
            ) {
                Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun VerificationDialog(
    user: User,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var cedula by remember { mutableStateOf(user.numeroCedula.ifEmpty { "402-2849102-5" }) }
    var infotepCert by remember { mutableStateOf(user.certificadoInfotepNombre.ifEmpty { "Técnico en Instalaciones Eléctricas y Climatización - INFOTEP" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🇩🇴 Verificación Oficial", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DominicanBlue.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "La verificación oficial valida tu identidad con la JCE y acredita tus títulos del INFOTEP para que los clientes contraten con máxima confianza.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = ChambaNavyPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = { Text("Número de Cédula Dominicana *") },
                    placeholder = { Text("001-0000000-0") },
                    singleLine = true,
                    leadingIcon = { Text("🇩🇴", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("cedula_input_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = infotepCert,
                    onValueChange = { infotepCert = it },
                    label = { Text("Certificado / Diploma INFOTEP (Opcional)") },
                    placeholder = { Text("Ej. Técnico en Plomería / Refrigeración") },
                    minLines = 2,
                    leadingIcon = { Text("🏅", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("infotep_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(cedula, infotepCert) },
                colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark),
                modifier = Modifier.testTag("submit_verification_btn")
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("VERIFICAR AHORA", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianBankAccountCard(
    viewModel: MainViewModel,
    worker: User
) {
    val bankAccount by viewModel.currentWorkerBankAccount.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    // Form fields state
    var selectedBank by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Cuenta de Ahorros") }
    var accountNumber by remember { mutableStateOf("") }
    var confirmAccountNumber by remember { mutableStateOf("") }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var showAccountVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val dominicanBanks = listOf(
        "Banco de Reservas (Banreservas)",
        "Banco Popular Dominicano",
        "Banco BHD",
        "Banco Santa Cruz",
        "Banco Promerica",
        "Scotiabank República Dominicana",
        "Asociación Popular de Ahorros y Préstamos (APAP)",
        "Asociación Cibao de Ahorros y Préstamos",
        "Banco Caribe",
        "Qik Banco Digital",
        "Otro Banco"
    )

    // Sync form values when bankAccount loads or changes
    LaunchedEffect(bankAccount) {
        if (bankAccount != null) {
            selectedBank = bankAccount!!.bankName
            accountHolder = bankAccount!!.accountHolder
            accountType = bankAccount!!.accountType
            accountNumber = bankAccount!!.accountNumber
            confirmAccountNumber = bankAccount!!.accountNumber
        } else {
            selectedBank = "Banco de Reservas (Banreservas)"
            accountHolder = worker.nombre
            accountType = "Cuenta de Ahorros"
            accountNumber = ""
            confirmAccountNumber = ""
            isEditing = true
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("technician_bank_data_section")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰 DATOS PARA RECIBIR PAGOS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChambaNavyPrimary
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChambaNavyPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "PRIVADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChambaNavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Registra tu cuenta bancaria personal para recibir transferencias cuando completes trabajos. Estos datos nunca serán públicos ni visibles para los clientes.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (bankAccount != null && !isEditing) {
                // Read-only Registered Account Summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ChambaEmeraldLight.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ChambaEmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cuenta Activa para Depósitos",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ChambaEmeraldDark
                                )
                            }

                            IconButton(
                                onClick = { showAccountVisible = !showAccountVisible },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (showAccountVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Mostrar número",
                                    tint = ChambaNavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = bankAccount!!.bankName,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = ChambaNavyPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Titular:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(bankAccount!!.accountHolder, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tipo de cuenta:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(bankAccount!!.accountType, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Número de cuenta:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (showAccountVisible) bankAccount!!.accountNumber else bankAccount!!.maskedAccountNumber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChambaNavyPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        isEditing = true
                        selectedBank = bankAccount!!.bankName
                        accountHolder = bankAccount!!.accountHolder
                        accountType = bankAccount!!.accountType
                        accountNumber = bankAccount!!.accountNumber
                        confirmAccountNumber = bankAccount!!.accountNumber
                        validationError = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_bank_account_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MODIFICAR DATOS BANCARIOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Form Fields
                if (bankAccount == null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ChambaAmberLight.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = ChambaAmberDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Debes registrar tus datos de pago para poder recibir tus ganancias.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChambaNavyPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 1. Banco Selector
                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedBank,
                        onValueChange = { selectedBank = it },
                        label = { Text("Banco *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("bank_name_input")
                    )
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        dominicanBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank, fontSize = 13.sp) },
                                onClick = {
                                    selectedBank = if (bank == "Otro Banco") "" else bank
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Titular de la cuenta
                OutlinedTextField(
                    value = accountHolder,
                    onValueChange = {
                        accountHolder = it
                        validationError = null
                    },
                    label = { Text("Titular de la cuenta *") },
                    placeholder = { Text("Nombre completo como aparece en el banco") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_holder_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Tipo de cuenta
                Text("Tipo de cuenta *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cuenta de Ahorros", "Cuenta Corriente").forEach { typeOption ->
                        val isSelected = accountType == typeOption
                        OutlinedButton(
                            onClick = { accountType = typeOption },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) ChambaNavyPrimary else Color.Transparent,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("account_type_${typeOption.replace(" ", "_").lowercase()}")
                        ) {
                            Text(typeOption, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Número de cuenta
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = {
                        accountNumber = it
                        validationError = null
                    },
                    label = { Text("Número de cuenta *") },
                    placeholder = { Text("Ej. 960-445566-1") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_number_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Confirmar número de cuenta
                val accountsMatch = confirmAccountNumber.isEmpty() || confirmAccountNumber.trim() == accountNumber.trim()
                OutlinedTextField(
                    value = confirmAccountNumber,
                    onValueChange = {
                        confirmAccountNumber = it
                        validationError = null
                    },
                    label = { Text("Confirmar número de cuenta *") },
                    placeholder = { Text("Reescribe exactamente el número de cuenta") },
                    singleLine = true,
                    isError = !accountsMatch,
                    supportingText = {
                        if (!accountsMatch) {
                            Text("Los dos números de cuenta no coinciden.", color = DominicanRed, fontSize = 11.sp)
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_account_number_input")
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DominicanRed.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DominicanRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(validationError!!, color = DominicanRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Security note: Rule 7 - No passwords, PIN or CVV
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChambaBlueAccent.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = ChambaBlueAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Por tu seguridad, nunca guardes contraseñas, PIN ni CVV. Solo se requiere tu número de cuenta para depósitos.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save button
                Button(
                    onClick = {
                        if (selectedBank.isBlank()) {
                            validationError = "Por favor selecciona o ingresa el nombre del banco."
                            return@Button
                        }
                        if (accountHolder.isBlank()) {
                            validationError = "Por favor ingresa el titular de la cuenta."
                            return@Button
                        }
                        if (accountNumber.isBlank()) {
                            validationError = "Por favor ingresa el número de cuenta."
                            return@Button
                        }
                        if (confirmAccountNumber.isBlank()) {
                            validationError = "Por favor confirma el número de cuenta."
                            return@Button
                        }
                        if (accountNumber.trim() != confirmAccountNumber.trim()) {
                            validationError = "Los dos números de cuenta no coinciden."
                            return@Button
                        }

                        viewModel.saveWorkerBankAccount(
                            bankName = selectedBank,
                            accountHolder = accountHolder,
                            accountType = accountType,
                            accountNumber = accountNumber,
                            confirmAccountNumber = confirmAccountNumber,
                            onSuccess = {
                                isEditing = false
                                validationError = null
                            },
                            onError = { err ->
                                validationError = err
                            }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_bank_account_button")
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GUARDAR DATOS DE PAGO", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (bankAccount != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = {
                            isEditing = false
                            validationError = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar edición", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

