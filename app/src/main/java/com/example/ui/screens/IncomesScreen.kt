package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Payment
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IncomesScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPayments by viewModel.paymentRepo.paymentsState.collectAsState()
    val currentWorkerBank by viewModel.currentWorkerBankAccount.collectAsState()
    val workerId = currentUser?.uid ?: ""
    val incomeSummary = viewModel.paymentRepo.getIncomeSummaryForWorker(workerId)
    val workerPayments = allPayments.filter { it.trabajadorId == workerId }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Mis Ingresos y Finanzas",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChambaNavyPrimary
                        )
                    )
                    Text(
                        text = "Control de pagos, comisiones y balances en RD$",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("incomes_scroll_list"),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Main Total Net Earnings Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChambaNavyPrimary),
                    modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = "TOTAL GANADO (NETO)",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ChambaEmeraldLight)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = incomeSummary.netoFormateado,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = Color.White.copy(alpha = 0.2f))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Disponible para retiro", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                Text(incomeSummary.disponibleFormateado, color = ChambaEmeraldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Comisión retenida (10%)", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                Text(incomeSummary.comisionFormateado, color = ChambaAmberLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bank Account for Payouts Status Banner
            item {
                if (currentWorkerBank == null) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ChambaAmberLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("incomes_missing_bank_banner")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = ChambaAmberDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Debes registrar tus datos de pago para poder recibir tus ganancias.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ChambaNavyPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.navigateTo(Screen.Profile) },
                                colors = ButtonDefaults.buttonColors(containerColor = ChambaNavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("incomes_register_bank_button")
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REGISTRAR DATOS DE PAGO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("incomes_bank_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ChambaEmeraldLight, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ChambaEmeraldDark, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cuenta vinculada para transferencias:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${currentWorkerBank!!.bankName} • ${currentWorkerBank!!.maskedAccountNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ChambaNavyPrimary
                                )
                                Text(
                                    text = "Titular: ${currentWorkerBank!!.accountHolder} (${currentWorkerBank!!.accountType})",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                                Text("Modificar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChambaEmeraldDark)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Incomes Breakdown Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("En Proceso / Retenido", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(incomeSummary.pendienteFormateado, fontSize = 16.sp, fontWeight = FontWeight.Black, color = ChambaAmberDark)
                            Text("En custodia segura", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Pagado / Transferido", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(incomeSummary.pagadoFormateado, fontSize = 16.sp, fontWeight = FontWeight.Black, color = ChambaEmeraldDark)
                            Text("Cobrado con éxito", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Platform Policy Notice (Requirement #25 & #26)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ChambaBlueAccent.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ChambaBlueAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CHAMBA RD retiene de manera segura los pagos hasta que el cliente confirma la finalización del trabajo. Aplica una comisión fija del 10% para mantenimiento de plataforma.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // History Section Header
            item {
                Text(
                    text = "Historial de Transacciones (${workerPayments.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ChambaNavyPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (workerPayments.isEmpty()) {
                item {
                    Text(
                        text = "No tienes movimientos registrados.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(workerPayments) { pay ->
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
                                Text(
                                    text = pay.chambaTitulo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (pay.estado == "liberado") ChambaEmeraldLight else ChambaAmberLight
                                ) {
                                    Text(
                                        text = pay.estado.uppercase(),
                                        color = if (pay.estado == "liberado") ChambaEmeraldDark else ChambaAmberDark,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monto bruto: RD$ ${String.format("%,.0f", pay.monto)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Comisión (10%): -RD$ ${String.format("%,.0f", pay.montoComision)}", fontSize = 11.sp, color = DominicanRed)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ref: ${pay.referenciaExterna}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                Text("Neto: RD$ ${String.format("%,.0f", pay.montoNetoTrabajador)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ChambaEmeraldDark)
                            }
                        }
                    }
                }
            }
        }
    }
}
