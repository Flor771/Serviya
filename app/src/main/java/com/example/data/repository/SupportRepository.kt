package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.CustomerSupportConfig
import com.example.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SupportRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _supportConfigState = MutableStateFlow(loadPersistedConfig())
    val supportConfigState = _supportConfigState.asStateFlow()

    private fun loadPersistedConfig(): CustomerSupportConfig {
        val phone = prefs.getString(KEY_PHONE, "829-837-0908") ?: "829-837-0908"
        val whatsapp = prefs.getString(KEY_WHATSAPP, "18298370908") ?: "18298370908"
        val hours = prefs.getString(
            KEY_HOURS,
            "Lunes a Viernes: 8:00 AM - 6:00 PM\nSábados: 9:00 AM - 1:00 PM"
        ) ?: "Lunes a Viernes: 8:00 AM - 6:00 PM\nSábados: 9:00 AM - 1:00 PM"
        val email = prefs.getString(KEY_EMAIL, "soporte@chambard.com") ?: "soporte@chambard.com"
        val msg = prefs.getString(
            KEY_MSG,
            "Hola CHAMBA RD, necesito asistencia con la plataforma."
        ) ?: "Hola CHAMBA RD, necesito asistencia con la plataforma."
        val adminId = prefs.getString(KEY_ADMIN_ID, "admin_1") ?: "admin_1"
        val updatedAt = prefs.getLong(KEY_UPDATED_AT, System.currentTimeMillis())

        return CustomerSupportConfig(
            id = "customer_support_main",
            phone = phone,
            whatsapp = whatsapp,
            businessHours = hours,
            email = email,
            whatsappWelcomeMessage = msg,
            lastUpdatedByAdmin = adminId,
            updatedAt = updatedAt
        )
    }

    suspend fun updateSupportConfig(
        adminUser: User,
        phone: String,
        whatsapp: String,
        businessHours: String,
        email: String,
        whatsappWelcomeMessage: String = ""
    ): Result<CustomerSupportConfig> {
        // Validación estricta de seguridad: únicamente administradores pueden modificar la configuración
        if (!adminUser.esAdmin) {
            return Result.failure(
                SecurityException("Acceso no autorizado: Solo los usuarios con rol de Administrador pueden modificar el número y datos de atención.")
            )
        }

        val cleanPhone = phone.trim()
        val cleanWhatsapp = whatsapp.trim()
        val cleanHours = businessHours.trim()
        val cleanEmail = email.trim()
        val cleanMsg = whatsappWelcomeMessage.trim().ifEmpty {
            "Hola CHAMBA RD, necesito asistencia con la plataforma."
        }

        if (cleanPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("El número de teléfono de atención es obligatorio."))
        }
        if (cleanWhatsapp.isBlank()) {
            return Result.failure(IllegalArgumentException("El número de WhatsApp de atención es obligatorio."))
        }
        if (cleanHours.isBlank()) {
            return Result.failure(IllegalArgumentException("El horario de atención es obligatorio."))
        }

        val now = System.currentTimeMillis()
        val updatedConfig = CustomerSupportConfig(
            id = "customer_support_main",
            phone = cleanPhone,
            whatsapp = cleanWhatsapp,
            businessHours = cleanHours,
            email = cleanEmail,
            whatsappWelcomeMessage = cleanMsg,
            lastUpdatedByAdmin = adminUser.uid.ifEmpty { adminUser.email },
            updatedAt = now
        )

        // Persistir la configuración en almacenamiento administrable para que no dependa del código fuente
        prefs.edit()
            .putString(KEY_PHONE, cleanPhone)
            .putString(KEY_WHATSAPP, cleanWhatsapp)
            .putString(KEY_HOURS, cleanHours)
            .putString(KEY_EMAIL, cleanEmail)
            .putString(KEY_MSG, cleanMsg)
            .putString(KEY_ADMIN_ID, updatedConfig.lastUpdatedByAdmin)
            .putLong(KEY_UPDATED_AT, now)
            .apply()

        _supportConfigState.value = updatedConfig
        return Result.success(updatedConfig)
    }

    companion object {
        private const val PREFS_NAME = "chamba_support_config_store"
        private const val KEY_PHONE = "support_phone"
        private const val KEY_WHATSAPP = "support_whatsapp"
        private const val KEY_HOURS = "support_hours"
        private const val KEY_EMAIL = "support_email"
        private const val KEY_MSG = "support_whatsapp_msg"
        private const val KEY_ADMIN_ID = "support_admin_id"
        private const val KEY_UPDATED_AT = "support_updated_at"
    }
}
