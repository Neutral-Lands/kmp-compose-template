package com.neutrallands.nouri.domain.repository

import com.neutrallands.nouri.domain.model.ComplianceLog
import kotlinx.coroutines.flow.Flow

interface ComplianceRepository {
    fun getLogsForDate(
        patientId: String,
        date: String,
    ): Flow<List<ComplianceLog>>

    suspend fun saveLog(log: ComplianceLog)

    suspend fun syncPendingLogs()
}
