package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data object Search : Screen()
    data object PublishChamba : Screen()
    data class ChambaDetail(val chambaId: String) : Screen()
    data class WorkerProfile(val workerId: String) : Screen()
    data object MyChambas : Screen()
    data object Messages : Screen()
    data class ChatConversation(val chambaId: String, val otherUserId: String, val otherUserName: String) : Screen()
    data object Notifications : Screen()
    data object Incomes : Screen()
    data object AdminDashboard : Screen()
    data object PriceEstimator : Screen()
    data object CustomerSupport : Screen()
    data object Policies : Screen()
    data object Profile : Screen()
    data object Login : Screen()
    data object Register : Screen()
    data object ForgotPassword : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val authRepo = AuthRepository()
    val chambaRepo = ChambaRepository()
    val notificationRepo = NotificationRepository()
    val postulacionRepo = PostulacionRepository(chambaRepo, notificationRepo)
    val chatRepo = ChatRepository()
    val reviewRepo = ReviewRepository(authRepo)
    val reportDisputeRepo = ReportDisputeRepository()
    val paymentRepo = PaymentRepository()
    val categoryRepo = CategoryRepository()
    val supportRepo = SupportRepository(application)

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen = _currentScreen.asStateFlow()

    // UI Feedback Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    // Search and Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _selectedProvince = MutableStateFlow<String?>("Todas")
    val selectedProvince = _selectedProvince.asStateFlow()

    private val _maxPriceFilter = MutableStateFlow<Double?>(null)
    val maxPriceFilter = _maxPriceFilter.asStateFlow()

    // Current User
    val currentUser = authRepo.currentUserState

    // Filtered Chambas
    val filteredChambas: StateFlow<List<Chamba>> = combine(
        chambaRepo.chambasState,
        _searchQuery,
        _selectedCategoryId,
        _selectedProvince,
        _maxPriceFilter
    ) { list, query, catId, province, maxPrice ->
        list.filter { chamba ->
            val matchesQuery = query.isEmpty() ||
                chamba.titulo.contains(query, ignoreCase = true) ||
                chamba.descripcion.contains(query, ignoreCase = true) ||
                chamba.ubicacion.contains(query, ignoreCase = true) ||
                chamba.categoriaNombre.contains(query, ignoreCase = true)

            val matchesCategory = catId == null || chamba.categoriaId.equals(catId, ignoreCase = true)
            val matchesPrice = maxPrice == null || chamba.precio <= maxPrice
            val matchesProvince = province == null || province == "Todas" || 
                chamba.ubicacion.contains(province.replace(" (D.N.)", "").replace(" (SDE)", "").replace(" (SDN)", "").replace(" (SDO)", "").replace(" (Santiago)", ""), ignoreCase = true)

            matchesQuery && matchesCategory && matchesPrice && matchesProvince
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unread Notifications Count
    val unreadNotificationsCount: StateFlow<Int> = notificationRepo.notificationsState.map { list ->
        val uid = currentUser.value?.uid ?: ""
        list.count { !it.leida && (it.userId == uid || it.userId.isEmpty()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(catId: String?) {
        _selectedCategoryId.value = catId
    }

    fun setSelectedProvince(province: String?) {
        _selectedProvince.value = province
    }

    fun setMaxPrice(max: Double?) {
        _maxPriceFilter.value = max
    }

    fun switchRoleForTesting(role: UserRole) {
        authRepo.switchRoleForTesting(role)
        showMessage("Cambiado a vista de ${role.displayName}")
    }

    fun register(
        nombre: String,
        email: String,
        telefono: String,
        pass: String,
        confirmPass: String,
        rol: UserRole,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepo.register(nombre, email, telefono, pass, confirmPass, rol)
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("¡Bienvenido a CHAMBA RD, ${it.nombre}!")
                _currentScreen.value = Screen.Home
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al registrarse.")
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepo.login(email, pass)
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Sesión iniciada con éxito.")
                _currentScreen.value = Screen.Home
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al iniciar sesión.")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            authRepo.sendPasswordReset(email)
            showMessage("Si el correo está registrado, recibirás un enlace de recuperación.")
        }
    }

    fun logout() {
        authRepo.logout()
        _currentScreen.value = Screen.Login
        showMessage("Has cerrado sesión.")
    }

    fun publishChamba(
        titulo: String,
        descripcion: String,
        categoriaId: String,
        categoriaNombre: String,
        ubicacion: String,
        fecha: String,
        hora: String,
        precio: Double,
        materialesResponsable: String,
        costoMateriales: Double,
        fotos: List<String>,
        onSuccess: (String) -> Unit
    ) {
        val user = currentUser.value
        if (user == null) {
            showMessage("Debes iniciar sesión para publicar una chamba.")
            return
        }
        viewModelScope.launch {
            val result = chambaRepo.createChamba(
                cliente = user,
                titulo = titulo,
                descripcion = descripcion,
                categoriaId = categoriaId,
                categoriaNombre = categoriaNombre,
                ubicacion = ubicacion,
                fechaTrabajo = fecha,
                horaTrabajo = hora,
                precio = precio,
                materialesResponsable = materialesResponsable,
                costoMateriales = costoMateriales,
                fotos = fotos
            )
            if (result.isSuccess) { val created = result.getOrNull()!!
                paymentRepo.initiatePayment(
                    chambaId = created.id,
                    chambaTitulo = created.titulo,
                    clienteId = user.uid,
                    clienteNombre = user.nombre,
                    trabajadorId = "",
                    trabajadorNombre = "",
                    monto = created.precio
                )
                showMessage("¡Chamba publicada con éxito!")
                onSuccess(created.id)
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al publicar la chamba.")
            }
        }
    }

    fun applyToChamba(
        chamba: Chamba,
        mensaje: String,
        precioPropuesto: Double,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value
        if (user == null) {
            showMessage("Debes iniciar sesión para postularte.")
            return
        }
        viewModelScope.launch {
            val result = postulacionRepo.createPostulacion(
                chamba = chamba,
                trabajador = user,
                mensaje = mensaje,
                precioPropuesto = precioPropuesto
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("¡Te has postulado con éxito a la chamba!")
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al postularse.")
            }
        }
    }

    fun selectWorker(
        chamba: Chamba,
        postulacion: Postulacion,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = postulacionRepo.selectTrabajador(chamba, postulacion)
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("¡Has seleccionado a ${postulacion.trabajadorNombre}!")
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al seleccionar trabajador.")
            }
        }
    }

    fun startWork(chambaId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = chambaRepo.updateChambaState(
                chambaId = chambaId,
                newState = ChambaState.EN_PROCESO
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                val chamba = chambaRepo.getChambaById(chambaId)
                if (chamba != null) {
                    notificationRepo.sendNotification(
                        userId = chamba.clienteId,
                        titulo = "Trabajo iniciado",
                        mensaje = "${user.nombre} ha iniciado las labores en tu chamba «${chamba.titulo}».",
                        tipo = "estado",
                        chambaId = chambaId
                    )
                }
                showMessage("¡Has iniciado la chamba! Trabaja con calidad y seguridad.")
            }
        }
    }

    fun finishWork(chambaId: String, notas: String, evidenciaFotos: List<String>) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = chambaRepo.updateChambaState(
                chambaId = chambaId,
                newState = ChambaState.TRABAJO_TERMINADO,
                notasTerminado = notas,
                evidenciaFotos = evidenciaFotos
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                val chamba = chambaRepo.getChambaById(chambaId)
                if (chamba != null) {
                    notificationRepo.sendNotification(
                        userId = chamba.clienteId,
                        titulo = "Trabajo terminado - Revisión pendiente",
                        mensaje = "${user.nombre} ha finalizado la chamba «${chamba.titulo}» y subió evidencias fotográficas.",
                        tipo = "estado",
                        chambaId = chambaId
                    )
                }
                showMessage("¡Trabajo marcado como terminado! El cliente ha recibido la notificación de revisión.")
            }
        }
    }

    fun confirmWork(chambaId: String) {
        viewModelScope.launch {
            val result = chambaRepo.updateChambaState(
                chambaId = chambaId,
                newState = ChambaState.COMPLETADA
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                paymentRepo.releasePayment(chambaId)
                val chamba = chambaRepo.getChambaById(chambaId)
                if (chamba != null && chamba.trabajadorSeleccionadoId.isNotEmpty()) {
                    notificationRepo.sendNotification(
                        userId = chamba.trabajadorSeleccionadoId,
                        titulo = "¡Trabajo confirmado y pago liberado!",
                        mensaje = "El cliente ha aprobado el trabajo de «${chamba.titulo}». Tu pago ha sido liberado.",
                        tipo = "estado",
                        chambaId = chambaId
                    )
                }
                showMessage("¡Trabajo confirmado satisfactoriamente! Fondos liberados.")
            }
        }
    }

    fun sendChatMessage(chambaId: String, receiverId: String, mensaje: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = chatRepo.sendMessage(
                chambaId = chambaId,
                senderId = user.uid,
                senderNombre = user.nombre,
                receiverId = receiverId,
                mensaje = mensaje
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                notificationRepo.sendNotification(
                    userId = receiverId,
                    titulo = "Nuevo mensaje de ${user.nombre}",
                    mensaje = mensaje.take(60),
                    tipo = "chat",
                    chambaId = chambaId
                )
            }
        }
    }

    fun submitReview(
        chambaId: String,
        chambaTitulo: String,
        receptorId: String,
        puntuacion: Double,
        comentario: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = reviewRepo.createReview(
                chambaId = chambaId,
                chambaTitulo = chambaTitulo,
                autor = user,
                receptorId = receptorId,
                puntuacion = puntuacion,
                comentario = comentario
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("¡Gracias por calificar el trabajo!")
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al enviar la calificación.")
            }
        }
    }

    fun reportUser(
        reportedUserId: String,
        reportedUserNombre: String,
        chambaId: String,
        motivo: String,
        descripcion: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = reportDisputeRepo.createReport(
                reporter = user,
                reportedUserId = reportedUserId,
                reportedUserNombre = reportedUserNombre,
                chambaId = chambaId,
                motivo = motivo,
                descripcion = descripcion
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Reporte enviado a administración para investigación.")
                onSuccess()
            }
        }
    }

    fun openDispute(
        chambaId: String,
        chambaTitulo: String,
        motivo: String,
        descripcion: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = reportDisputeRepo.createDispute(
                creador = user,
                chambaId = chambaId,
                chambaTitulo = chambaTitulo,
                motivo = motivo,
                descripcion = descripcion
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                chambaRepo.updateChambaState(chambaId, ChambaState.EN_DISPUTA)
                showMessage("Disputa abierta. El equipo de CHAMBA RD intervendrá.")
                onSuccess()
            }
        }
    }

    fun submitVerification(
        cedula: String,
        infotepCert: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = authRepo.submitVerificationRequest(user.uid, cedula, infotepCert)
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Solicitud enviada exitosamente. El equipo de administración revisará tus documentos.")
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al enviar solicitud de verificación.")
            }
        }
    }

    fun approveWorkerVerification(workerId: String) {
        viewModelScope.launch {
            val result = authRepo.approveWorkerVerification(workerId)
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Técnico verificado y acreditado oficialmente.")
            } else { val it = result.exceptionOrNull()!!
                showMessage("Error al verificar técnico.")
            }
        }
    }

    fun rejectWorkerVerification(workerId: String) {
        viewModelScope.launch {
            val result = authRepo.rejectWorkerVerification(workerId)
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Solicitud de verificación rechazada.")
            } else { val it = result.exceptionOrNull()!!
                showMessage("Error al procesar solicitud.")
            }
        }
    }

    // --- SISTEMA DE PAGOS POR TRANSFERENCIA BANCARIA Y COMISIONES ---

    val bankAccountConfig = paymentRepo.bankAccountState
    val commissionRate = paymentRepo.commissionRateState
    val allPayments = paymentRepo.paymentsState
    val technicianPayouts = paymentRepo.payoutsState
    val bankAccountAudits = paymentRepo.bankAuditsState

    fun initiateChambaPayment(chamba: Chamba, totalMonto: Double, onSuccess: () -> Unit = {}) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = paymentRepo.initiatePayment(
                chambaId = chamba.id,
                chambaTitulo = chamba.titulo,
                clienteId = user.uid,
                clienteNombre = user.nombre,
                trabajadorId = chamba.trabajadorSeleccionadoId,
                trabajadorNombre = chamba.trabajadorSeleccionadoNombre,
                monto = totalMonto
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                onSuccess()
            }
        }
    }

    fun uploadPaymentReceipt(
        chambaId: String,
        receiptUrl: String,
        receiptNotes: String,
        transferRef: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = paymentRepo.uploadReceipt(
                chambaId = chambaId,
                receiptUrl = receiptUrl,
                receiptNotes = receiptNotes,
                transferRef = transferRef
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                val chamba = chambaRepo.getChambaById(chambaId)
                notificationRepo.sendNotification(
                    userId = "admin_1",
                    titulo = "📥 Nuevo comprobante de transferencia",
                    mensaje = "El cliente ${user.nombre} ha subido el comprobante para «${chamba?.titulo ?: "la chamba"}».",
                    tipo = "pago",
                    chambaId = chambaId
                )
                showMessage("¡Comprobante enviado a revisión! El equipo de CHAMBA RD validará los fondos.")
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al subir comprobante.")
            }
        }
    }

    fun confirmBankPayment(paymentId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = paymentRepo.confirmPaymentByAdmin(paymentId, user.uid)
            if (result.isSuccess) { val payment = result.getOrNull()!!
                chambaRepo.updateChambaState(payment.chambaId, ChambaState.EN_PROCESO)
                notificationRepo.sendNotification(
                    userId = payment.clienteId,
                    titulo = "✅ Transferencia Confirmada",
                    mensaje = "Tu pago por ${payment.montoFormateado} ha sido verificado con éxito y está en custodia segura.",
                    tipo = "pago",
                    chambaId = payment.chambaId
                )
                notificationRepo.sendNotification(
                    userId = payment.trabajadorId,
                    titulo = "🚀 Chamba en Progreso",
                    mensaje = "El pago de «${payment.chambaTitulo}» fue confirmado en custodia. Ya puedes realizar el trabajo.",
                    tipo = "pago",
                    chambaId = payment.chambaId
                )
                showMessage("Pago verificado y confirmado. La chamba está en proceso.")
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al confirmar pago.")
            }
        }
    }

    fun rejectBankPayment(paymentId: String, reason: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = paymentRepo.rejectPaymentByAdmin(paymentId, user.uid, reason)
            if (result.isSuccess) { val payment = result.getOrNull()!!
                notificationRepo.sendNotification(
                    userId = payment.clienteId,
                    titulo = "⚠️ Comprobante Rechazado",
                    mensaje = "Tu comprobante fue rechazado: ${payment.rejectionReason}. Por favor realiza la transferencia y sube un nuevo comprobante.",
                    tipo = "pago",
                    chambaId = payment.chambaId
                )
                showMessage("Comprobante rechazado y cliente notificado.")
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al rechazar pago.")
            }
        }
    }

    fun markTechnicianPayoutPaid(
        payoutId: String,
        method: String,
        reference: String,
        notes: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = paymentRepo.markTechnicianPayoutPaid(
                payoutId = payoutId,
                adminId = user.uid,
                method = method,
                reference = reference,
                notes = notes
            )
            if (result.isSuccess) { val payout = result.getOrNull()!!
                notificationRepo.sendNotification(
                    userId = payout.workerId,
                    titulo = "🎉 Pago Transferido a tu Cuenta",
                    mensaje = "Se ha transferido ${payout.netPayoutFormateado} a tu cuenta. Ref: $reference.",
                    tipo = "pago",
                    chambaId = payout.chambaId
                )
                showMessage("Pago al técnico marcado como transferido exitosamente.")
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al procesar pago al técnico.")
            }
        }
    }

    fun updateAdminBankAccount(
        bankName: String,
        holder: String,
        type: String,
        number: String,
        notes: String,
        rnc: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = paymentRepo.updateBankAccount(
                adminId = user.uid,
                bankName = bankName,
                accountHolder = holder,
                accountType = type,
                accountNumber = number,
                rncOrCedula = rnc,
                notes = notes
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Datos bancarios oficiales actualizados y auditados con éxito.")
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al actualizar cuenta bancaria.")
            }
        }
    }

    fun updateAdminCommission(newRate: Double) {
        viewModelScope.launch {
            val result = paymentRepo.updateCommissionRate(newRate)
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Comisión actualizada a ${(newRate * 100).toInt()}%.")
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al actualizar comisión.")
            }
        }
    }

    // --- ATENCIÓN AL CLIENTE / SOPORTE OFICIAL CHAMBA RD ---

    val customerSupportConfig = supportRepo.supportConfigState

    // --- DATOS BANCARIOS DEL TÉCNICO PARA RECIBIR PAGOS ---

    val workerBankAccountsState = paymentRepo.workerBankAccountsState

    val currentWorkerBankAccount: StateFlow<WorkerBankAccount?> = combine(
        authRepo.currentUserState,
        paymentRepo.workerBankAccountsState
    ) { user, bankAccounts ->
        if (user != null && user.esTrabajador) {
            bankAccounts[user.uid]
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getWorkerBankAccount(workerId: String): WorkerBankAccount? {
        return paymentRepo.getWorkerBankAccount(workerId)
    }

    fun hasWorkerBankAccount(workerId: String): Boolean {
        return paymentRepo.getWorkerBankAccount(workerId) != null
    }

    fun saveWorkerBankAccount(
        bankName: String,
        accountHolder: String,
        accountType: String,
        accountNumber: String,
        confirmAccountNumber: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = currentUser.value
        if (user == null || !user.esTrabajador) {
            val err = "Acceso denegado: Esta sección es exclusiva para técnicos."
            showMessage(err)
            onError(err)
            return
        }

        if (bankName.isBlank()) {
            val err = "Por favor selecciona o escribe el nombre del banco."
            showMessage(err)
            onError(err)
            return
        }
        if (accountHolder.isBlank()) {
            val err = "Por favor ingresa el titular de la cuenta."
            showMessage(err)
            onError(err)
            return
        }
        if (accountType.isBlank()) {
            val err = "Por favor selecciona el tipo de cuenta."
            showMessage(err)
            onError(err)
            return
        }
        if (accountNumber.isBlank()) {
            val err = "Por favor ingresa el número de cuenta."
            showMessage(err)
            onError(err)
            return
        }
        if (accountNumber.trim() != confirmAccountNumber.trim()) {
            val err = "Los dos números de cuenta no coinciden. Por favor verifícalos."
            showMessage(err)
            onError(err)
            return
        }

        // Validar que no se guarden PIN, CVV o contraseñas
        val lowerNum = accountNumber.lowercase()
        val lowerBank = bankName.lowercase()
        if (lowerNum.contains("pin") || lowerNum.contains("cvv") || lowerBank.contains("pin") || lowerBank.contains("cvv")) {
            val err = "Por seguridad no debes ingresar contraseñas, PIN ni CVV."
            showMessage(err)
            onError(err)
            return
        }

        viewModelScope.launch {
            val result = paymentRepo.saveWorkerBankAccount(
                workerId = user.uid,
                bankName = bankName.trim(),
                accountHolder = accountHolder.trim(),
                accountType = accountType.trim(),
                accountNumber = accountNumber.trim()
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Datos de pago guardados exitosamente.")
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                val err = it.message ?: "Error al guardar datos bancarios."
                showMessage(err)
                onError(err)
            }
        }
    }

    fun updateCustomerSupportConfig(
        phone: String,
        whatsapp: String,
        businessHours: String,
        email: String,
        whatsappWelcomeMessage: String = "",
        onSuccess: () -> Unit = {}
    ) {
        val user = currentUser.value
        if (user == null || !user.esAdmin) {
            showMessage("Acceso denegado: Solo administradores pueden modificar los canales de atención.")
            return
        }

        viewModelScope.launch {
            val result = supportRepo.updateSupportConfig(
                adminUser = user,
                phone = phone,
                whatsapp = whatsapp,
                businessHours = businessHours,
                email = email,
                whatsappWelcomeMessage = whatsappWelcomeMessage
            )
            if (result.isSuccess) { val it = result.getOrNull()!!
                showMessage("Canales de atención al cliente actualizados exitosamente.")
                onSuccess()
            } else { val it = result.exceptionOrNull()!!
                showMessage(it.message ?: "Error al actualizar la configuración de soporte.")
            }
        }
    }
}
