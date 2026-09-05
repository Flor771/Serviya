package com.example.data.repository

import android.util.Log
import com.example.data.models.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class PaymentRepository {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _paymentsState = MutableStateFlow<List<Payment>>(emptyList())
    val paymentsState = _paymentsState.asStateFlow()

    private val _bankAccountState = MutableStateFlow(
        BankAccountConfig(
            id = "bank_primary",
            bankName = "Banco de Reservas (Banreservas)",
            accountHolder = "CHAMBA RD S.R.L.",
            accountType = "Cuenta Corriente Empresarial",
            accountNumber = "960-123456-7",
            rncOrCedula = "1-32-45678-9",
            isActive = true,
            notes = "Cuenta oficial para transferencias y depósitos directos de CHAMBA RD."
        )
    )
    val bankAccountState = _bankAccountState.asStateFlow()

    private val _commissionRateState = MutableStateFlow(0.10) // 10% configurable
    val commissionRateState = _commissionRateState.asStateFlow()

    private val _payoutsState = MutableStateFlow<List<TechnicianPayout>>(emptyList())
    val payoutsState = _payoutsState.asStateFlow()

    private val _bankAuditsState = MutableStateFlow<List<BankAccountAudit>>(emptyList())
    val bankAuditsState = _bankAuditsState.asStateFlow()

    // Cuentas bancarias privadas de los técnicos para recibir pagos (almacenadas de forma segura)
    private val _workerBankAccountsState = MutableStateFlow<Map<String, WorkerBankAccount>>(
        mapOf(
            "demo_trabajador_1" to WorkerBankAccount(
                id = "bank_demo_trabajador_1",
                workerId = "demo_trabajador_1",
                bankName = "Banco de Reservas (Banreservas)",
                accountHolder = "Carlos Mendoza",
                accountType = "Cuenta de Ahorros",
                accountNumber = "960-445566-1"
            )
        )
    )
    val workerBankAccountsState = _workerBankAccountsState.asStateFlow()

    init {
        seedInitialPayments()
    }

    private fun seedInitialPayments() {
        val list = listOf(
            Payment(
                id = "pay_1",
                chambaId = "chamba_5",
                chambaTitulo = "Reparación de filtración de tubería en lavamanos",
                clienteId = "cliente_4",
                clienteNombre = "María Santos",
                trabajadorId = "demo_trabajador_1",
                trabajadorNombre = "Carlos Mendoza",
                monto = 3000.0,
                comision = 0.10, // 10%
                estado = "liberado",
                referenciaExterna = "RD-TRANS-88219",
                receiptUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=500",
                receiptNotes = "Transferencia realizada desde app Banreservas",
                receiptUploadedAt = System.currentTimeMillis() - 86400000L * 4,
                verifiedByAdminId = "admin_1",
                verifiedAt = System.currentTimeMillis() - 86400000L * 4,
                bankNameUsed = "Banco de Reservas (Banreservas)",
                accountNumberUsed = "960-123456-7",
                fecha = System.currentTimeMillis() - 86400000L * 4
            ),
            Payment(
                id = "pay_2",
                chambaId = "chamba_3",
                chambaTitulo = "Montaje de escritorio ejecutivo y estante modular",
                clienteId = "demo_cliente_1",
                clienteNombre = "Juan Pérez",
                trabajadorId = "demo_trabajador_1",
                trabajadorNombre = "Carlos Mendoza",
                monto = 2800.0,
                comision = 0.10,
                estado = "retenido",
                referenciaExterna = "RD-TRANS-90441",
                receiptUrl = "https://images.unsplash.com/photo-1554224154-26032ffc0d07?w=500",
                receiptNotes = "Transferencia por banca móvil Popular",
                receiptUploadedAt = System.currentTimeMillis() - 86400000L * 2,
                verifiedByAdminId = "admin_1",
                verifiedAt = System.currentTimeMillis() - 86400000L * 2,
                bankNameUsed = "Banco de Reservas (Banreservas)",
                accountNumberUsed = "960-123456-7",
                fecha = System.currentTimeMillis() - 86400000L * 2
            ),
            Payment(
                id = "pay_prev",
                chambaId = "chamba_prev_1",
                chambaTitulo = "Instalación de tomacorrientes e interruptores inteligentes",
                clienteId = "cliente_5",
                clienteNombre = "Ana Fernández",
                trabajadorId = "demo_trabajador_1",
                trabajadorNombre = "Carlos Mendoza",
                monto = 6500.0,
                comision = 0.10,
                estado = "liberado",
                referenciaExterna = "RD-TRANS-72105",
                receiptUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=500",
                receiptNotes = "Transferencia directa",
                receiptUploadedAt = System.currentTimeMillis() - 86400000L * 15,
                verifiedByAdminId = "admin_1",
                verifiedAt = System.currentTimeMillis() - 86400000L * 15,
                bankNameUsed = "Banco de Reservas (Banreservas)",
                accountNumberUsed = "960-123456-7",
                fecha = System.currentTimeMillis() - 86400000L * 15
            )
        )
        _paymentsState.value = list

        val initialPayouts = listOf(
            TechnicianPayout(
                id = "payout_1",
                paymentId = "pay_1",
                chambaId = "chamba_5",
                chambaTitulo = "Reparación de filtración de tubería en lavamanos",
                workerId = "demo_trabajador_1",
                workerNombre = "Carlos Mendoza",
                grossAmount = 3000.0,
                commissionRate = 0.10,
                commissionAmount = 300.0,
                netPayout = 2700.0,
                status = "pagado",
                paidAt = System.currentTimeMillis() - 86400000L * 3,
                paymentMethod = "Transferencia Bancaria",
                transferReference = "TRANS-BR-55410",
                processedByAdminId = "admin_1",
                notes = "Transferencia enviada a cuenta Banreservas del técnico.",
                workerBankName = "Banco de Reservas (Banreservas)",
                workerAccountHolder = "Carlos Mendoza",
                workerAccountType = "Cuenta de Ahorros",
                workerAccountNumber = "960-445566-1",
                workerHasBankAccount = true
            ),
            TechnicianPayout(
                id = "payout_prev",
                paymentId = "pay_prev",
                chambaId = "chamba_prev_1",
                chambaTitulo = "Instalación de tomacorrientes e interruptores inteligentes",
                workerId = "demo_trabajador_1",
                workerNombre = "Carlos Mendoza",
                grossAmount = 6500.0,
                commissionRate = 0.10,
                commissionAmount = 650.0,
                netPayout = 5850.0,
                status = "pagado",
                paidAt = System.currentTimeMillis() - 86400000L * 14,
                paymentMethod = "Transferencia Bancaria",
                transferReference = "TRANS-BR-49210",
                processedByAdminId = "admin_1",
                notes = "Pago procesado exitosamente.",
                workerBankName = "Banco de Reservas (Banreservas)",
                workerAccountHolder = "Carlos Mendoza",
                workerAccountType = "Cuenta de Ahorros",
                workerAccountNumber = "960-445566-1",
                workerHasBankAccount = true
            )
        )
        _payoutsState.value = initialPayouts
    }

    fun getIncomeSummaryForWorker(workerId: String): IncomeSummary {
        val workerPayments = _paymentsState.value.filter { it.trabajadorId == workerId }
        val workerPayouts = _payoutsState.value.filter { it.workerId == workerId }

        var pendiente = 0.0
        var disponible = 0.0
        var pagado = 0.0
        var comisionTotal = 0.0

        for (p in workerPayments) {
            val net = p.montoNetoTrabajador
            val fee = p.montoComision
            when (p.estado) {
                "retenido", "pendiente", "comprobante_subido", "en_revision" -> {
                    pendiente += net
                }
                "liberado" -> {
                    comisionTotal += fee
                }
            }
        }

        for (po in workerPayouts) {
            if (po.status == "pagado") {
                pagado += po.netPayout
            } else if (po.status == "pendiente") {
                disponible += po.netPayout
            }
        }

        return IncomeSummary(
            pendiente = pendiente,
            disponible = disponible,
            pagado = pagado,
            comisionTotal = comisionTotal,
            netoTotal = pagado + disponible
        )
    }

    fun getAdminFinanceSummary(): AdminFinanceSummary {
        val payments = _paymentsState.value
        val payouts = _payoutsState.value

        val validPayments = payments.filter { it.estado in listOf("retenido", "liberado", "confirmado") }
        val totalRecibido = validPayments.sumOf { it.monto }
        val comisionesGanadas = validPayments.sumOf { it.montoComision }
        val pagosEfectuados = payouts.filter { it.status == "pagado" }.sumOf { it.netPayout }
        val pagosPendientes = payouts.filter { it.status == "pendiente" }.sumOf { it.netPayout }

        return AdminFinanceSummary(
            totalRecibido = totalRecibido,
            comisionesGanadas = comisionesGanadas,
            pagosTecnicosEfectuados = pagosEfectuados,
            pagosTecnicosPendientes = pagosPendientes,
            totalTransacciones = payments.size,
            comisionPorcentaje = _commissionRateState.value
        )
    }

    suspend fun initiatePayment(
        chambaId: String,
        chambaTitulo: String,
        clienteId: String,
        clienteNombre: String,
        trabajadorId: String,
        trabajadorNombre: String,
        monto: Double
    ): Result<Payment> {
        val existing = _paymentsState.value.firstOrNull { it.chambaId == chambaId }
        val bank = _bankAccountState.value
        val currentCommission = _commissionRateState.value

        if (existing != null) {
            val updated = existing.copy(
                monto = monto,
                comision = currentCommission,
                bankNameUsed = bank.bankName,
                accountNumberUsed = bank.accountNumber
            )
            val list = _paymentsState.value.map { if (it.id == existing.id) updated else it }
            _paymentsState.value = list
            return Result.success(updated)
        }

        val id = "pay_${System.currentTimeMillis()}"
        val payment = Payment(
            id = id,
            chambaId = chambaId,
            chambaTitulo = chambaTitulo,
            clienteId = clienteId,
            clienteNombre = clienteNombre,
            trabajadorId = trabajadorId,
            trabajadorNombre = trabajadorNombre,
            monto = monto,
            comision = currentCommission,
            estado = "pendiente",
            referenciaExterna = "RD-TRANS-${System.currentTimeMillis() % 1000000}",
            bankNameUsed = bank.bankName,
            accountNumberUsed = bank.accountNumber,
            fecha = System.currentTimeMillis()
        )

        val list = _paymentsState.value.toMutableList()
        list.add(0, payment)
        _paymentsState.value = list

        return Result.success(payment)
    }

    suspend fun uploadReceipt(
        chambaId: String,
        receiptUrl: String,
        receiptNotes: String,
        transferRef: String
    ): Result<Payment> {
        val payment = _paymentsState.value.firstOrNull { it.chambaId == chambaId }
            ?: return Result.failure(Exception("Pago no encontrado para esta chamba."))

        val updated = payment.copy(
            receiptUrl = receiptUrl,
            receiptNotes = receiptNotes,
            referenciaExterna = if (transferRef.isNotBlank()) transferRef else payment.referenciaExterna,
            receiptUploadedAt = System.currentTimeMillis(),
            estado = "comprobante_subido",
            rejectionReason = ""
        )

        val list = _paymentsState.value.map { if (it.id == payment.id) updated else it }
        _paymentsState.value = list
        return Result.success(updated)
    }

    suspend fun confirmPaymentByAdmin(paymentId: String, adminId: String): Result<Payment> {
        val payment = _paymentsState.value.firstOrNull { it.id == paymentId }
            ?: return Result.failure(Exception("Pago no encontrado."))

        val updated = payment.copy(
            estado = "retenido",
            verifiedByAdminId = adminId,
            verifiedAt = System.currentTimeMillis(),
            rejectionReason = ""
        )

        val list = _paymentsState.value.map { if (it.id == paymentId) updated else it }
        _paymentsState.value = list
        return Result.success(updated)
    }

    suspend fun rejectPaymentByAdmin(paymentId: String, adminId: String, reason: String): Result<Payment> {
        val payment = _paymentsState.value.firstOrNull { it.id == paymentId }
            ?: return Result.failure(Exception("Pago no encontrado."))

        val updated = payment.copy(
            estado = "rechazado",
            verifiedByAdminId = adminId,
            verifiedAt = System.currentTimeMillis(),
            rejectionReason = if (reason.isNotBlank()) reason else "Comprobante no válido o fondos no reflejados."
        )

        val list = _paymentsState.value.map { if (it.id == paymentId) updated else it }
        _paymentsState.value = list
        return Result.success(updated)
    }

    suspend fun releasePayment(chambaId: String): Result<Unit> {
        var releasedPayment: Payment? = null
        val list = _paymentsState.value.map {
            if (it.chambaId == chambaId) {
                val rel = it.copy(estado = "liberado")
                releasedPayment = rel
                rel
            } else it
        }
        _paymentsState.value = list

        if (releasedPayment != null) {
            val pay = releasedPayment!!
            val payoutId = "payout_${System.currentTimeMillis()}"
            val workerBank = _workerBankAccountsState.value[pay.trabajadorId]
            val payout = TechnicianPayout(
                id = payoutId,
                paymentId = pay.id,
                chambaId = pay.chambaId,
                chambaTitulo = pay.chambaTitulo,
                workerId = pay.trabajadorId,
                workerNombre = pay.trabajadorNombre,
                grossAmount = pay.monto,
                commissionRate = pay.comision,
                commissionAmount = pay.montoComision,
                netPayout = pay.montoNetoTrabajador,
                status = "pendiente",
                notes = "Pago pendiente de transferencia bancaria al técnico por parte de administración de CHAMBA RD.",
                workerBankName = workerBank?.bankName ?: "",
                workerAccountHolder = workerBank?.accountHolder ?: "",
                workerAccountType = workerBank?.accountType ?: "",
                workerAccountNumber = workerBank?.accountNumber ?: "",
                workerHasBankAccount = workerBank != null
            )
            val payoutsList = _payoutsState.value.toMutableList()
            payoutsList.add(0, payout)
            _payoutsState.value = payoutsList
        }

        return Result.success(Unit)
    }

    fun getWorkerBankAccount(workerId: String): WorkerBankAccount? {
        return _workerBankAccountsState.value[workerId]
    }

    suspend fun saveWorkerBankAccount(
        workerId: String,
        bankName: String,
        accountHolder: String,
        accountType: String,
        accountNumber: String
    ): Result<WorkerBankAccount> {
        if (bankName.isBlank()) return Result.failure(Exception("Por favor selecciona o ingresa el nombre del banco."))
        if (accountHolder.isBlank()) return Result.failure(Exception("Por favor ingresa el titular de la cuenta."))
        if (accountType.isBlank()) return Result.failure(Exception("Por favor selecciona el tipo de cuenta."))
        if (accountNumber.isBlank()) return Result.failure(Exception("Por favor ingresa el número de cuenta."))

        val cleanNumber = accountNumber.trim()
        val cleanBank = bankName.trim()
        val cleanHolder = accountHolder.trim()
        val cleanType = accountType.trim()

        val existing = _workerBankAccountsState.value[workerId]
        val bankAccount = WorkerBankAccount(
            id = existing?.id ?: "bank_${workerId}_${System.currentTimeMillis()}",
            workerId = workerId,
            bankName = cleanBank,
            accountHolder = cleanHolder,
            accountType = cleanType,
            accountNumber = cleanNumber,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val updatedMap = _workerBankAccountsState.value.toMutableMap()
        updatedMap[workerId] = bankAccount
        _workerBankAccountsState.value = updatedMap

        // Sincronizar de inmediato con los pagos pendientes o registrados de este técnico
        val updatedPayouts = _payoutsState.value.map { payout ->
            if (payout.workerId == workerId) {
                payout.copy(
                    workerBankName = cleanBank,
                    workerAccountHolder = cleanHolder,
                    workerAccountType = cleanType,
                    workerAccountNumber = cleanNumber,
                    workerHasBankAccount = true
                )
            } else {
                payout
            }
        }
        _payoutsState.value = updatedPayouts

        return Result.success(bankAccount)
    }

    suspend fun markTechnicianPayoutPaid(
        payoutId: String,
        adminId: String,
        method: String,
        reference: String,
        notes: String
    ): Result<TechnicianPayout> {
        val payout = _payoutsState.value.firstOrNull { it.id == payoutId }
            ?: return Result.failure(Exception("Pago a técnico no encontrado."))

        val updated = payout.copy(
            status = "pagado",
            paidAt = System.currentTimeMillis(),
            paymentMethod = method.ifBlank { "Transferencia Bancaria" },
            transferReference = reference,
            processedByAdminId = adminId,
            notes = notes
        )

        val list = _payoutsState.value.map { if (it.id == payoutId) updated else it }
        _payoutsState.value = list
        return Result.success(updated)
    }

    suspend fun updateBankAccount(
        adminId: String,
        bankName: String,
        accountHolder: String,
        accountType: String,
        accountNumber: String,
        rncOrCedula: String = "",
        notes: String = ""
    ): Result<BankAccountConfig> {
        val oldBank = _bankAccountState.value
        val newBank = BankAccountConfig(
            id = "bank_${System.currentTimeMillis()}",
            bankName = bankName.trim(),
            accountHolder = accountHolder.trim(),
            accountType = accountType.trim(),
            accountNumber = accountNumber.trim(),
            rncOrCedula = rncOrCedula.trim(),
            isActive = true,
            notes = notes.trim(),
            createdAt = System.currentTimeMillis()
        )
        _bankAccountState.value = newBank

        val audit = BankAccountAudit(
            id = "audit_${System.currentTimeMillis()}",
            adminId = adminId,
            action = "UPDATE",
            accountId = newBank.id,
            oldData = "${oldBank.bankName} - ${oldBank.accountNumber} (${oldBank.accountHolder})",
            newData = "${newBank.bankName} - ${newBank.accountNumber} (${newBank.accountHolder})",
            fecha = System.currentTimeMillis()
        )
        val audits = _bankAuditsState.value.toMutableList()
        audits.add(0, audit)
        _bankAuditsState.value = audits

        return Result.success(newBank)
    }

    suspend fun updateCommissionRate(newRate: Double): Result<Double> {
        if (newRate < 0.0 || newRate > 0.50) {
            return Result.failure(Exception("La comisión debe estar entre 0% y 50%."))
        }
        _commissionRateState.value = newRate
        return Result.success(newRate)
    }

    fun getPaymentForChamba(chambaId: String): Payment? {
        return _paymentsState.value.firstOrNull { it.chambaId == chambaId }
    }
}
