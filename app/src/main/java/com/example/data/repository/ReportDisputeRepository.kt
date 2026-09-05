package com.example.data.repository

import android.util.Log
import com.example.data.models.Dispute
import com.example.data.models.Report
import com.example.data.models.User
import com.example.data.network.RetrofitClient
import com.example.data.network.ReportCreateRequest
import com.example.data.network.DisputeCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportDisputeRepository {

    private val _reportsState = MutableStateFlow<List<Report>>(emptyList())
    val reportsState = _reportsState.asStateFlow()

    private val _disputesState = MutableStateFlow<List<Dispute>>(emptyList())
    val disputesState = _disputesState.asStateFlow()

    suspend fun createReport(
        reporter: User,
        reportedUserId: String,
        reportedUserNombre: String,
        chambaId: String,
        motivo: String,
        descripcion: String,
        evidencia: String = ""
    ): Result<Report> {
        return try {
            val req = ReportCreateRequest(
                reportedUserId = reportedUserId,
                chambaId = chambaId,
                reason = motivo,
                description = descripcion,
                evidenceUrl = evidencia
            )
            val res = RetrofitClient.reportesApi.createReport(req)
            if (res.isSuccessful) {
                // Generamos un modelo local solo para devolver la validación visual inmediata.
                val report = Report(
                    id = res.body()?.get("report_id") ?: "new",
                    reporterId = reporter.uid,
                    reporterNombre = reporter.nombre,
                    reportedUserId = reportedUserId,
                    reportedUserNombre = reportedUserNombre,
                    chambaId = chambaId,
                    motivo = motivo,
                    descripcion = descripcion,
                    evidencia = evidencia,
                    estado = "pendiente"
                )
                val list = _reportsState.value.toMutableList()
                list.add(0, report)
                _reportsState.value = list
                Result.success(report)
            } else {
                Result.failure(Exception("Error al enviar reporte"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDispute(
        creador: User,
        chambaId: String,
        chambaTitulo: String,
        motivo: String,
        descripcion: String,
        evidencia: String = ""
    ): Result<Dispute> {
        return try {
            val req = DisputeCreateRequest(
                chambaId = chambaId,
                reason = motivo,
                description = descripcion,
                evidenceUrl = evidencia
            )
            val res = RetrofitClient.reportesApi.createDispute(req)
            if (res.isSuccessful) {
                val dispute = Dispute(
                    id = res.body()?.get("dispute_id") ?: "new",
                    chambaId = chambaId,
                    chambaTitulo = chambaTitulo,
                    creadorId = creador.uid,
                    creadorNombre = creador.nombre,
                    motivo = motivo,
                    descripcion = descripcion,
                    evidencia = evidencia,
                    estado = "abierta",
                    resolucion = ""
                )
                val list = _disputesState.value.toMutableList()
                list.add(0, dispute)
                _disputesState.value = list
                Result.success(dispute)
            } else {
                Result.failure(Exception("Error al crear disputa"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
