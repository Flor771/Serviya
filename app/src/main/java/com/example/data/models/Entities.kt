package com.example.data.models

data class ChatMessage(
    val id: String = "",
    val chambaId: String = "",
    val senderId: String = "",
    val senderNombre: String = "",
    val receiverId: String = "",
    val mensaje: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val leido: Boolean = false
)

data class ChatRoom(
    val id: String = "",
    val chambaId: String = "",
    val chambaTitulo: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val trabajadorId: String = "",
    val trabajadorNombre: String = "",
    val ultimoMensaje: String = "",
    val ultimaFecha: Long = System.currentTimeMillis(),
    val noLeidosCliente: Int = 0,
    val noLeidosTrabajador: Int = 0
)

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "info", // postulacion, seleccion, estado, chat, review, sistema
    val chambaId: String = "",
    val leida: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)

data class Review(
    val id: String = "",
    val chambaId: String = "",
    val chambaTitulo: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val autorFoto: String = "",
    val autorRol: String = "cliente",
    val receptorId: String = "",
    val puntuacion: Double = 5.0, // 1 to 5
    val comentario: String = "",
    val fecha: Long = System.currentTimeMillis()
)

data class Report(
    val id: String = "",
    val reporterId: String = "",
    val reporterNombre: String = "",
    val reportedUserId: String = "",
    val reportedUserNombre: String = "",
    val chambaId: String = "",
    val motivo: String = "Incumplimiento", // Fraude, Estafa, Incumplimiento, Comportamiento inapropiado, Información falsa, Spam, Otro
    val descripcion: String = "",
    val evidencia: String = "",
    val estado: String = "pendiente", // pendiente, en_revision, resuelto, desestimado
    val fecha: Long = System.currentTimeMillis()
)

data class Dispute(
    val id: String = "",
    val chambaId: String = "",
    val chambaTitulo: String = "",
    val creadorId: String = "",
    val creadorNombre: String = "",
    val motivo: String = "",
    val descripcion: String = "",
    val evidencia: String = "",
    val estado: String = "abierta", // abierta, en_revision, resuelta, cerrada
    val resolucion: String = "",
    val fecha: Long = System.currentTimeMillis()
)

data class BankAccountConfig(
    val id: String = "",
    val bankName: String = "Banco de Reservas (Banreservas)",
    val accountHolder: String = "CHAMBA RD S.R.L.",
    val accountType: String = "Cuenta Corriente Empresarial",
    val accountNumber: String = "960-123456-7",
    val rncOrCedula: String = "1-32-45678-9",
    val isActive: Boolean = true,
    val notes: String = "Cuenta oficial para transferencias de CHAMBA RD.",
    val createdAt: Long = System.currentTimeMillis()
)

data class BankAccountAudit(
    val id: String = "",
    val adminId: String = "",
    val action: String = "UPDATE",
    val accountId: String = "",
    val oldData: String = "",
    val newData: String = "",
    val fecha: Long = System.currentTimeMillis()
) {
    val fechaHora: String get() {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(fecha))
    }
}

data class WorkerBankAccount(
    val id: String = "",
    val workerId: String = "",
    val bankName: String = "",
    val accountHolder: String = "",
    val accountType: String = "Cuenta de Ahorros",
    val accountNumber: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val maskedAccountNumber: String get() {
        if (accountNumber.length <= 4) return accountNumber
        val last4 = accountNumber.takeLast(4)
        return "•••• •••• $last4"
    }
}

data class TechnicianPayout(
    val id: String = "",
    val paymentId: String = "",
    val chambaId: String = "",
    val chambaTitulo: String = "",
    val workerId: String = "",
    val workerNombre: String = "",
    val grossAmount: Double = 0.0,
    val commissionRate: Double = 0.10,
    val commissionAmount: Double = 0.0,
    val netPayout: Double = 0.0,
    val status: String = "pendiente", // pendiente, pagado, rechazado
    val paidAt: Long? = null,
    val paymentMethod: String = "Transferencia Bancaria",
    val transferReference: String = "",
    val processedByAdminId: String = "",
    val payoutReceiptUrl: String = "",
    val notes: String = "",
    val workerBankName: String = "",
    val workerAccountHolder: String = "",
    val workerAccountType: String = "",
    val workerAccountNumber: String = "",
    val workerHasBankAccount: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val netPayoutFormateado: String get() = "RD$" + String.format("%,.0f", netPayout)
    val grossAmountFormateado: String get() = "RD$" + String.format("%,.0f", grossAmount)
    val commissionAmountFormateado: String get() = "RD$" + String.format("%,.0f", commissionAmount)
    val estado: String get() = status
    val paidAtFormatted: String get() {
        if (paidAt == null) return "—"
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(paidAt))
    }
}

data class Payment(
    val id: String = "",
    val chambaId: String = "",
    val chambaTitulo: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val trabajadorId: String = "",
    val trabajadorNombre: String = "",
    val monto: Double = 0.0, // Precio del trabajo acordado
    val comision: Double = 0.10, // 10% configurable (cobrada adicionalmente al cliente)
    val estado: String = "pendiente", // pendiente, comprobante_subido, en_revision, confirmado, retenido, liberado, rechazado, reembolsado, en_disputa
    val referenciaExterna: String = "",
    val receiptUrl: String = "",
    val receiptNotes: String = "",
    val receiptUploadedAt: Long? = null,
    val verifiedByAdminId: String = "",
    val verifiedAt: Long? = null,
    val rejectionReason: String = "",
    val bankNameUsed: String = "Banco de Reservas (Banreservas)",
    val accountNumberUsed: String = "960-123456-7",
    val fecha: Long = System.currentTimeMillis()
) {
    // Cálculo oficial CHAMBA RD:
    // comision = precio_trabajo * comision_porcentaje
    // total_cliente = precio_trabajo + comision (cliente paga precio + comisión)
    // ganancia_tecnico = precio_trabajo (técnico recibe 100% del precio acordado)
    val precioTrabajo: Double get() = monto
    val montoComision: Double get() = precioTrabajo * comision
    val totalCliente: Double get() = precioTrabajo + montoComision
    val montoNetoTrabajador: Double get() = precioTrabajo
    val montoFormateado: String get() = "RD$" + String.format("%,.0f", totalCliente)
    val precioTrabajoFormateado: String get() = "RD$" + String.format("%,.0f", precioTrabajo)
    val comisionFormateado: String get() = "RD$" + String.format("%,.0f", montoComision)
    val netoFormateado: String get() = "RD$" + String.format("%,.0f", montoNetoTrabajador)
}

data class IncomeSummary(
    val pendiente: Double = 0.0,
    val disponible: Double = 0.0,
    val pagado: Double = 0.0,
    val comisionTotal: Double = 0.0,
    val netoTotal: Double = 0.0
) {
    val pendienteFormateado: String get() = "RD$" + String.format("%,.0f", pendiente)
    val disponibleFormateado: String get() = "RD$" + String.format("%,.0f", disponible)
    val pagadoFormateado: String get() = "RD$" + String.format("%,.0f", pagado)
    val comisionFormateado: String get() = "RD$" + String.format("%,.0f", comisionTotal)
    val netoFormateado: String get() = "RD$" + String.format("%,.0f", netoTotal)
}

data class AdminFinanceSummary(
    val totalRecibido: Double = 0.0,
    val comisionesGanadas: Double = 0.0,
    val pagosTecnicosEfectuados: Double = 0.0,
    val pagosTecnicosPendientes: Double = 0.0,
    val totalTransacciones: Int = 0,
    val comisionPorcentaje: Double = 0.10
) {
    val totalRecibidoFormateado: String get() = "RD$" + String.format("%,.0f", totalRecibido)
    val comisionesGanadasFormateado: String get() = "RD$" + String.format("%,.0f", comisionesGanadas)
    val pagosEfectuadosFormateado: String get() = "RD$" + String.format("%,.0f", pagosTecnicosEfectuados)
    val pagosPendientesFormateado: String get() = "RD$" + String.format("%,.0f", pagosTecnicosPendientes)
}

data class CustomerSupportConfig(
    val id: String = "customer_support_main",
    val phone: String = "829-837-0908",
    val whatsapp: String = "18298370908",
    val businessHours: String = "Lunes a Viernes: 8:00 AM - 6:00 PM\nSábados: 9:00 AM - 1:00 PM",
    val email: String = "soporte@chambard.com",
    val whatsappWelcomeMessage: String = "Hola CHAMBA RD, necesito asistencia con la plataforma.",
    val lastUpdatedByAdmin: String = "admin_1",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val cleanPhoneForDial: String
        get() = phone.replace(Regex("[^0-9+]"), "")

    val cleanWhatsappForLink: String
        get() {
            val digits = whatsapp.replace(Regex("[^0-9]"), "")
            return if (digits.length == 10 && !digits.startsWith("1")) "1$digits" else digits
        }

    val fechaHoraActualizacion: String
        get() {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(updatedAt))
        }
}
