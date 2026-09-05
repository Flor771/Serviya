package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ChambaState(
    val key: String,
    val displayName: String,
    val badgeColor: Color,
    val description: String
) {
    BORRADOR(
        "borrador",
        "BORRADOR",
        StateBorrador,
        "Chamba en edición sin publicar aún"
    ),
    PUBLICADA(
        "publicada",
        "PUBLICADA",
        StatePublicada,
        "Publicada y visible para trabajadores"
    ),
    RECIBIENDO_POSTULACIONES(
        "recibiendo_postulaciones",
        "RECIBIENDO POSTULACIONES",
        StateRecibiendo,
        "Hay trabajadores postulándose para este trabajo"
    ),
    TRABAJADOR_SELECCIONADO(
        "trabajador_seleccionado",
        "TRABAJADOR SELECCIONADO",
        StateSeleccionado,
        "El cliente ha seleccionado a un trabajador"
    ),
    PAGO_PENDIENTE(
        "pago_pendiente",
        "PAGO PENDIENTE",
        StateEnProceso,
        "Esperando confirmación o retención de pago seguro"
    ),
    PAGO_RETENIDO(
        "pago_retenido",
        "PAGO RETENIDO EN GARANTÍA",
        StateSeleccionado,
        "Fondos retenidos con seguridad en CHAMBA RD"
    ),
    EN_PROCESO(
        "en_proceso",
        "EN PROCESO",
        StateEnProceso,
        "El trabajador ha iniciado las labores"
    ),
    TRABAJO_TERMINADO(
        "trabajo_terminado",
        "TRABAJO TERMINADO",
        StateTerminado,
        "Trabajador marcó fin de trabajo y adjuntó evidencias"
    ),
    PENDIENTE_CONFIRMACION(
        "pendiente_confirmacion",
        "PENDIENTE DE CONFIRMACIÓN",
        StateTerminado,
        "Esperando que el cliente revise y apruebe el trabajo"
    ),
    COMPLETADA(
        "completada",
        "COMPLETADA",
        StateCompletada,
        "Trabajo confirmado satisfactoriamente por el cliente"
    ),
    PAGADA(
        "pagada",
        "PAGADA",
        StatePagada,
        "Pago liberado al trabajador con éxito"
    ),
    CANCELADA(
        "cancelada",
        "CANCELADA",
        StateCancelada,
        "La chamba fue cancelada"
    ),
    EN_DISPUTA(
        "en_disputa",
        "EN DISPUTA",
        StateDisputa,
        "En revisión por soporte técnico y administración"
    );

    companion object {
        fun fromKey(key: String?): ChambaState {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: PUBLICADA
        }
    }
}
