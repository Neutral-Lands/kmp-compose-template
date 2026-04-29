package com.nouri.domain.repository

import com.nouri.domain.model.ComplianceLog
import kotlinx.coroutines.flow.Flow

interface ComplianceRepository {
    fun getLogsForDate(
        patientId: String,
        date: String,
    ): Flow<List<ComplianceLog>>

    suspend fun saveLog(log: ComplianceLog)

    suspend fun syncPendingLogs()
}
