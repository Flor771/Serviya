package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.components.StateBadge
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allChambas by viewModel.chambaRepo.chambasState.collectAsState()
    val allReports by viewModel.reportDisputeRepo.reportsState.collectAsState()
    val allDisputes by viewModel.reportDisputeRepo.disputesState.collectAsState()
    val categories by viewModel.categoryRepo.categoriesState.collectAsState()
    val allUsers by viewModel.authRepo.allUsersState.collectAsState()

    val bankAccount by viewModel.bankAccountConfig.collectAsState()
    val commissionRate by viewModel.commissionRate.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val technicianPayouts by viewModel.technicianPayouts.collectAsState()
    val bankAccountAudits by viewModel.bankAccountAudits.collectAsState()
    val customerSupportConfig by viewModel.customerSupportConfig.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    var showEditBankDialog by remember { mutableStateOf(false) }
    var showEditCommissionDialog by remember { mutableStateOf(false) }
    var showEditSupportDialog by remember { mutableStateOf(false) }
    var paymentToReject by remember { mutableStateOf<Payment?>(null) }
    var payoutToProcess by remember { mutableStateOf<TechnicianPayout?>(null) }
    var receiptPhotoToView by remember { mutableStateOf<String?>(null) }

    val pendingVerifications = remember(allUsers) {
        allUsers.filter { it.esTrabajador && (it.verificacionEstado == "pendiente" || it.numeroCedula.isNotEmpty()) }
    }

    val pendingReceipts = remember(allPayments) {
        allPayments.filter { it.estado == "comprobante_subido" || it.estado == "en_revision" || (it.receiptUrl.isNotEmpty() && it.estado == "pendiente") }
    }

    val pendingPayouts = remember(technicianPayouts) {
        technicianPayouts.filter { it.status == "pendiente" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Admin Header
        Surface(
            color = ChambaNavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = DominicanRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Panel de Administración",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = "CHAMBA RD — Finanzas, moderación y control bancario",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ChambaNavyDark,
                    contentColor = Color.White,
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Finanzas & Banco", fontSize = 11.sp, color = if (selectedTab == 0) ChambaAmber else Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Validar Pagos (${pendingReceipts.size})", fontSize = 11.sp, color = if (selectedTab == 1) ChambaAmber else Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Pagos Técnicos (${pendingPayouts.size})", fontSize = 11.sp, color = if (selectedTab == 2) ChambaAmber else Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Chambas (${allChambas.size})", fontSize = 11.sp, color = if (selectedTab == 3) ChambaAmber else Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("Verificaciones (${pendingVerifications.size})", fontSize = 11.sp, color = if (selectedTab == 4) ChambaAmber else Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        text = { Text("Reportes (${allReports.size})", fontSize = 11.sp, color = if (selectedTab == 5) ChambaAmber else Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 6,
                        onClick = { selectedTab = 6 },
                        text = { Text("Atención al Cliente", fontSize = 11.sp, color = if (selectedTab == 6) ChambaAmber else Color.White) },
                        modifier = Modifier.testTag("admin_tab_support")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Finanzas, Cuenta Bancaria y Auditoría
                val totalTransacted = allPayments.sumOf { it.monto }
                val totalCommissionEarned = allPayments.filter { it.estado == "liberado" || it.estado == "retenido" }.sumOf { it.montoComision }
                val fundsInEscrow = allPayments.filter { it.estado == "retenido" }.sumOf { it.monto }
                val pendingPayoutsAmount = technicianPayouts.filter { it.status == "pendiente" }.sumOf { it.netPayout }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_finance_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Resumen Financiero
                    item {
                        Text("Resumen Financiero CHAMBA RD", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ChambaNavyPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Comisiones Ganadas",
                                value = "RD$ ${String.format("%,.0f", totalCommissionEarned)}",
                                subtitle = "Ingresos plataforma",
                                color = ChambaEmeraldDark,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Fondos en Custodia",
                                value = "RD$ ${String.format("%,.0f", fundsInEscrow)}",
                                subtitle = "En garantía activa",
                                color = ChambaBlueAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Por Pagar a Técnicos",
                                value = "RD$ ${String.format("%,.0f", pendingPayoutsAmount)}",
                                subtitle = "${pendingPayouts.size} pagos pendientes",
                                color = DominicanRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Total Transaccionado",
                                value = "RD$ ${String.format("%,.0f", totalTransacted)}",
                                subtitle = "${allPayments.size} transacciones",
                                color = ChambaNavyPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Configuración de Comisión
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
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
                                        Text("Comisión por Chamba", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Porcentaje retenido por cada trabajo completado", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ChambaNavyPrimary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "${(commissionRate * 100).toInt()}%",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ChambaNavyPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { showEditCommissionDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Modificar Porcentaje de Comisión", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Configuración de Cuenta Bancaria Oficial
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ChambaNavyPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cuenta Bancaria Oficial", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (bankAccount.isActive) ChambaEmeraldLight else Color.Gray.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (bankAccount.isActive) "ACTIVA" else "INACTIVA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (bankAccount.isActive) ChambaEmeraldDark else Color.Gray,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Banco: ${bankAccount.bankName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Titular: ${bankAccount.accountHolder}", fontSize = 12.sp)
                                Text("Tipo: ${bankAccount.accountType}", fontSize = 12.sp)
                                Text("Número de Cuenta: ${bankAccount.accountNumber}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ChambaNavyPrimary)
                                if (bankAccount.rncOrCedula.isNotEmpty()) {
                                    Text("RNC / Cédula: ${bankAccount.rncOrCedula}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (bankAccount.notes.isNotEmpty()) {
                                    Text("Notas: ${bankAccount.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showEditBankDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Editar Cuenta Bancaria Oficial", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Historial y Auditoría de Cambios
                    item {
                        Text("Historial de Auditoría Bancaria", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ChambaNavyPrimary)
                        Text("Registro inmutable de modificaciones a la cuenta oficial", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (bankAccountAudits.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No se han registrado modificaciones en la cuenta bancaria.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(bankAccountAudits) { audit ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Admin: ${audit.adminId}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(audit.fechaHora, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Cuenta Anterior: ${audit.oldData}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Nueva Cuenta: ${audit.newData}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChambaNavyPrimary)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Validar Comprobantes de Transferencia de Clientes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_payments_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Comprobantes de Pago de Clientes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ChambaNavyPrimary)
                        Text("Revisa los fondos depositados por transferencia bancaria antes de autorizar el trabajo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (allPayments.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No hay pagos registrados aún.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(allPayments) { payment ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_payment_item_${payment.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(payment.chambaTitulo, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (payment.estado) {
                                                "liberado" -> ChambaBlueAccent
                                                "retenido", "confirmado" -> ChambaEmeraldDark
                                                "comprobante_subido", "en_revision" -> ChambaAmberDark
                                                "rechazado" -> DominicanRed
                                                else -> Color.Gray
                                            }
                                        ) {
                                            Text(
                                                text = when (payment.estado) {
                                                    "liberado" -> "LIBERADO"
                                                    "retenido", "confirmado" -> "CUSTODIA CONFIRMADA"
                                                    "comprobante_subido", "en_revision" -> "REVISIÓN PENDIENTE"
                                                    "rechazado" -> "RECHAZADO"
                                                    else -> "PENDIENTE"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("👤 Cliente: ${payment.clienteNombre}", fontSize = 11.sp)
                                    Text("🛠️ Técnico: ${payment.trabajadorNombre}", fontSize = 11.sp)
                                    Text("💰 Monto Total: ${payment.montoFormateado} (Comisión 10%: ${payment.comisionFormateado})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChambaNavyPrimary)

                                    if (payment.referenciaExterna.isNotEmpty()) {
                                        Text("🔖 Referencia bancaria: ${payment.referenciaExterna}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                    if (payment.receiptNotes.isNotEmpty()) {
                                        Text("📝 Notas: ${payment.receiptNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    // Comprobante Preview
                                    if (payment.receiptUrl.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable { receiptPhotoToView = payment.receiptUrl }
                                        ) {
                                            AsyncImage(
                                                model = payment.receiptUrl,
                                                contentDescription = "Comprobante",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(60.dp, 40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Ver comprobante completo 🔍", fontSize = 11.sp, color = ChambaBlueAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (payment.estado == "comprobante_subido" || payment.estado == "en_revision" || payment.estado == "pendiente") {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.confirmBankPayment(payment.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("admin_confirm_payment_${payment.id}")
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Confirmar Pago", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { paymentToReject = payment },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DominicanRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("admin_reject_payment_${payment.id}")
                                            ) {
                                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Rechazar", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Pagos a Técnicos (Payouts)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_payouts_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Pagos a Técnicos (Liquidación)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ChambaNavyPrimary)
                        Text("Transfiere el monto neto al técnico una vez que el cliente haya aprobado el trabajo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (technicianPayouts.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No hay liquidaciones pendientes para técnicos.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(technicianPayouts) { payout ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_payout_item_${payout.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Técnico: ${payout.workerNombre}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (payout.status == "pagado") ChambaEmeraldDark else DominicanRed
                                        ) {
                                            Text(
                                                text = if (payout.status == "pagado") "PAGADO ✓" else "PENDIENTE TRANSFERIR",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("Chamba: ${payout.chambaTitulo}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("Monto Bruto: ${payout.grossAmountFormateado}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Comisión CHAMBA RD: ${payout.commissionAmountFormateado}", fontSize = 11.sp, color = DominicanRed)
                                    Text("Neto a Transferir: ${payout.netPayoutFormateado}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ChambaEmeraldDark)

                                    val clipboardManager = LocalClipboardManager.current
                                    val workerBank = viewModel.getWorkerBankAccount(payout.workerId)
                                    val bankName = workerBank?.bankName ?: payout.workerBankName
                                    val accountHolder = workerBank?.accountHolder ?: payout.workerAccountHolder
                                    val accountType = workerBank?.accountType ?: payout.workerAccountType
                                    val accountNumber = workerBank?.accountNumber ?: payout.workerAccountNumber
                                    val hasBank = bankName.isNotEmpty() && accountNumber.isNotEmpty()

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (hasBank) DominicanBlue.copy(alpha = 0.08f) else DominicanRed.copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🏦 DATOS BANCARIOS (PAGO MANUAL)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hasBank) ChambaNavyPrimary else DominicanRed
                                                )
                                                if (hasBank) {
                                                    TextButton(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(accountNumber))
                                                            viewModel.showMessage("Número de cuenta copiado: $accountNumber")
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Copiar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            if (hasBank) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Banco: $bankName", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                Text("Titular: $accountHolder", fontSize = 11.sp)
                                                Text("Tipo: $accountType", fontSize = 11.sp)
                                                Text("Cuenta: $accountNumber", fontSize = 12.sp, fontWeight = FontWeight.Black, color = ChambaNavyPrimary)
                                            } else {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "⚠️ El técnico aún no ha registrado sus datos bancarios.",
                                                    fontSize = 11.sp,
                                                    color = DominicanRed
                                                )
                                            }
                                        }
                                    }

                                    if (payout.notes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("ℹ️ ${payout.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    if (payout.status == "pagado") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Transferido: ${payout.paidAtFormatted} • Ref: ${payout.transferReference}", fontSize = 10.sp, color = ChambaEmeraldDark)
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { payoutToProcess = payout },
                                            colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("admin_mark_paid_button_${payout.id}")
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("MARCAR COMO PAGADO AL TÉCNICO", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Chambas List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(allChambas) { ch ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ch.titulo, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    StateBadge(state = ch.estadoEnum)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Cliente: ${ch.clienteNombre} • ${ch.ubicacion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Presupuesto: ${ch.precioFormateado}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DominicanRed)
                            }
                        }
                    }
                }
            }

            4 -> {
                // Verificaciones de Técnicos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Solicitudes de Verificación de Técnicos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ChambaNavyPrimary
                        )
                        Text(
                            text = "Revisa cédula de identidad y certificados técnicos antes de otorgar la insignia.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (pendingVerifications.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No hay solicitudes de verificación pendientes.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(pendingVerifications) { worker ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .testTag("admin_verification_item_${worker.uid}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(worker.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (worker.verificacionEstado) {
                                                "aprobado" -> ChambaEmeraldDark.copy(alpha = 0.15f)
                                                "rechazado" -> DominicanRed.copy(alpha = 0.15f)
                                                else -> ChambaAmberDark.copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Text(
                                                text = when (worker.verificacionEstado) {
                                                    "aprobado" -> "✓ VERIFICADO"
                                                    "rechazado" -> "RECHAZADO"
                                                    else -> "PENDIENTE REVISIÓN"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (worker.verificacionEstado) {
                                                    "aprobado" -> ChambaEmeraldDark
                                                    "rechazado" -> DominicanRed
                                                    else -> ChambaAmberDark
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("📧 ${worker.email} • 📞 ${worker.telefono}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("🇩🇴 Cédula ingresada: ${if (worker.numeroCedula.isNotEmpty()) worker.numeroCedula else '—'}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("🏅 Certificado INFOTEP: ${if (worker.certificadoInfotepNombre.isNotEmpty()) worker.certificadoInfotepNombre else '—'}", fontSize = 12.sp, fontWeight = FontWeight.Medium)

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.approveWorkerVerification(worker.uid) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("admin_approve_verification_${worker.uid}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Aprobar", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.rejectWorkerVerification(worker.uid) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DominicanRed),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("admin_reject_verification_${worker.uid}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Rechazar", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // Reports & Disputes List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text("Reportes de Usuarios", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    items(allReports) { rep ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Motivo: ${rep.motivo}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DominicanRed)
                                    Text(rep.estado.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Reportado por: ${rep.reporterNombre} a ${rep.reportedUserNombre}", fontSize = 11.sp)
                                Text("Detalle: ${rep.descripcion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Disputas de Trabajo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    items(allDisputes) { disp ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Chamba: ${disp.chambaTitulo}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Motivo: ${disp.motivo}", fontSize = 11.sp, color = DominicanRed)
                                Text("Descripción: ${disp.descripcion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            6 -> {
                // Configuración y Control de Canales de Atención al Cliente
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_support_management_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SupportAgent,
                                            contentDescription = null,
                                            tint = ChambaNavyPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Canales de Atención al Cliente",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = ChambaNavyPrimary
                                            )
                                            Text(
                                                text = "Configuración dinámica administrable",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showEditSupportDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DominicanRed),
                                        modifier = Modifier.testTag("admin_edit_support_tab_btn")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Modificar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(14.dp))

                                // Item: Teléfono
                                SupportConfigRow(
                                    label = "Número Telefónico de Atención",
                                    value = customerSupportConfig.phone,
                                    description = "Llamada directa al marcar desde la aplicación",
                                    icon = Icons.Default.Phone,
                                    iconColor = ChambaNavyPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Item: WhatsApp
                                SupportConfigRow(
                                    label = "Número de WhatsApp de Atención",
                                    value = customerSupportConfig.whatsapp,
                                    description = "Enlace oficial wa.me para soporte y comprobantes",
                                    icon = Icons.Default.Chat,
                                    iconColor = ChambaEmeraldDark
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Item: Horario
                                SupportConfigRow(
                                    label = "Horario de Atención",
                                    value = customerSupportConfig.businessHours,
                                    description = "Visible para clientes y técnicos en Centro de Ayuda",
                                    icon = Icons.Default.Schedule,
                                    iconColor = DominicanGold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Item: Correo
                                SupportConfigRow(
                                    label = "Correo de Atención",
                                    value = customerSupportConfig.email.ifEmpty { "No especificado" },
                                    description = "Consultas formales y contratos",
                                    icon = Icons.Default.Email,
                                    iconColor = ChambaBlueAccent
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Item: Mensaje de WhatsApp
                                SupportConfigRow(
                                    label = "Mensaje Inicial Preconfigurado",
                                    value = customerSupportConfig.whatsappWelcomeMessage,
                                    description = "Texto sugerido cuando el usuario abre el chat",
                                    icon = Icons.Default.Message,
                                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Última actualización: ${customerSupportConfig.fechaHoraActualizacion}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = DominicanRed.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "SOLO ADMIN",
                                            color = DominicanRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Card de Prueba Rápida (para que el admin valide el enlace en tiempo real)
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val context = LocalContext.current
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Comprobar Enlaces Directos de Atención",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ChambaNavyPrimary
                                )
                                Text(
                                    text = "Prueba el comportamiento exacto que experimentará el usuario al tocar los botones:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { launchPhoneDialer(context, customerSupportConfig.cleanPhoneForDial) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary),
                                        modifier = Modifier.weight(1f).testTag("admin_test_phone_btn")
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("📞 Llamar", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            launchWhatsAppChat(
                                                context,
                                                customerSupportConfig.cleanWhatsappForLink,
                                                customerSupportConfig.whatsappWelcomeMessage
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark),
                                        modifier = Modifier.weight(1f).testTag("admin_test_whatsapp_btn")
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("💬 WhatsApp", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Aviso de Seguridad
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ChambaBlueAccent.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ChambaNavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Seguridad: Los clientes y técnicos no pueden modificar estos datos. Cualquier cambio realizado aquí por el administrador se propaga inmediatamente sin modificar el código fuente.",
                                    fontSize = 11.sp,
                                    color = ChambaNavyPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Editar Cuenta Bancaria
    if (showEditBankDialog) {
        EditBankAccountDialog(
            currentConfig = bankAccount,
            onDismiss = { showEditBankDialog = false },
            onSubmit = { bankName, holder, type, number, notes, rnc ->
                viewModel.updateAdminBankAccount(bankName, holder, type, number, notes, rnc)
                showEditBankDialog = false
            }
        )
    }

    // Dialog: Editar Porcentaje de Comisión
    if (showEditCommissionDialog) {
        EditCommissionDialog(
            currentRate = commissionRate,
            onDismiss = { showEditCommissionDialog = false },
            onSubmit = { newRate ->
                viewModel.updateAdminCommission(newRate)
                showEditCommissionDialog = false
            }
        )
    }

    // Dialog: Editar Configuración de Atención al Cliente (Solo Admin)
    if (showEditSupportDialog) {
        EditCustomerSupportDialog(
            currentConfig = customerSupportConfig,
            onDismiss = { showEditSupportDialog = false },
            onSave = { phone, whatsapp, hours, email, welcomeMsg ->
                viewModel.updateCustomerSupportConfig(
                    phone = phone,
                    whatsapp = whatsapp,
                    businessHours = hours,
                    email = email,
                    whatsappWelcomeMessage = welcomeMsg,
                    onSuccess = {
                        showEditSupportDialog = false
                    }
                )
            }
        )
    }

    // Dialog: Rechazar Comprobante con Motivo
    if (paymentToReject != null) {
        RejectPaymentDialog(
            payment = paymentToReject!!,
            onDismiss = { paymentToReject = null },
            onSubmit = { reason ->
                viewModel.rejectBankPayment(paymentToReject!!.id, reason)
                paymentToReject = null
            }
        )
    }

    // Dialog: Marcar Pago al Técnico como Transferido
    if (payoutToProcess != null) {
        MarkPayoutPaidDialog(
            payout = payoutToProcess!!,
            onDismiss = { payoutToProcess = null },
            onSubmit = { method, reference, notes ->
                viewModel.markTechnicianPayoutPaid(payoutToProcess!!.id, method, reference, notes)
                payoutToProcess = null
            }
        )
    }

    // Dialog: Ver Comprobante Full
    if (receiptPhotoToView != null) {
        AlertDialog(
            onDismissRequest = { receiptPhotoToView = null },
            title = { Text("Comprobante de Transferencia", fontWeight = FontWeight.Bold) },
            text = {
                AsyncImage(
                    model = receiptPhotoToView,
                    contentDescription = "Comprobante",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            },
            confirmButton = {
                Button(onClick = { receiptPhotoToView = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EditBankAccountDialog(
    currentConfig: BankAccountConfig,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String) -> Unit
) {
    var bankName by remember { mutableStateOf(currentConfig.bankName) }
    var holder by remember { mutableStateOf(currentConfig.accountHolder) }
    var type by remember { mutableStateOf(currentConfig.accountType) }
    var number by remember { mutableStateOf(currentConfig.accountNumber) }
    var rnc by remember { mutableStateOf(currentConfig.rncOrCedula) }
    var notes by remember { mutableStateOf(currentConfig.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Cuenta Bancaria Oficial", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Banco *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = holder,
                    onValueChange = { holder = it },
                    label = { Text("Nombre del Titular *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipo de Cuenta *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número de Cuenta *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rnc,
                    onValueChange = { rnc = it },
                    label = { Text("RNC o Cédula") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Instrucciones o notas") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(bankName, holder, type, number, notes, rnc) },
                colors = ButtonDefaults.buttonColors(containerColor = DominicanRed)
            ) {
                Text("GUARDAR Y AUDITAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditCommissionDialog(
    currentRate: Double,
    onDismiss: () -> Unit,
    onSubmit: (Double) -> Unit
) {
    var ratePercentStr by remember { mutableStateOf((currentRate * 100).toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Comisión CHAMBA RD", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configura la comisión porcentual que la plataforma retendrá de cada chamba.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = ratePercentStr,
                    onValueChange = { ratePercentStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Porcentaje (%)") },
                    suffix = { Text("%") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = (ratePercentStr.toDoubleOrNull() ?: 10.0) / 100.0
                    onSubmit(rate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary)
            ) {
                Text("APLICAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun RejectPaymentDialog(
    payment: Payment,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var reason by remember { mutableStateOf("No se visualizan los fondos en el estado de cuenta bancario.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rechazar Comprobante de Pago", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Indica el motivo del rechazo para que el cliente pueda corregir o reintentar la transferencia.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo del rechazo *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = DominicanRed)
            ) {
                Text("CONFIRMAR RECHAZO", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun MarkPayoutPaidDialog(
    payout: TechnicianPayout,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var reference by remember { mutableStateOf("TRANSF-${(100000..999999).random()}") }
    var method by remember { mutableStateOf("Transferencia ACH / Banreservas") }
    var notes by remember { mutableStateOf("Transferencia realizada al técnico exitosamente") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Liquidación al Técnico", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChambaEmeraldLight.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Técnico: ${payout.workerNombre}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Monto Neto a Transferir: ${payout.netPayoutFormateado}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = ChambaEmeraldDark)
                        if (payout.workerBankName.isNotEmpty() || payout.workerAccountNumber.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🏦 Cuenta Destino:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChambaNavyPrimary)
                            Text("${payout.workerBankName} • ${payout.workerAccountNumber}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = ChambaNavyPrimary)
                            Text("Titular: ${payout.workerAccountHolder} (${payout.workerAccountType})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text("Método de pago / Banco") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Número de Referencia *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(method, reference, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = ChambaEmeraldDark)
            ) {
                Text("MARCAR COMO PAGADO", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SupportConfigRow(
    label: String,
    value: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
