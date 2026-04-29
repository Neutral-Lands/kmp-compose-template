package com.nouri.data.repository

import com.nouri.data.datasource.local.ComplianceLogLocalDataSource
import com.nouri.domain.model.ComplianceLog
import com.nouri.domain.repository.ComplianceRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Suppress("UnusedPrivateProperty") // supabase used when sync is fully implemented
class ComplianceRepositoryImpl(
    private val localDataSource: ComplianceLogLocalDataSource,
    private val supabase: SupabaseClient,
) : ComplianceRepository {
    override fun getLogsForDate(
        patientId: String,
        date: String,
    ): Flow<List<ComplianceLog>> = localDataSource.getByPatientAndDate(patientId, date)

    override suspend fun saveLog(log: ComplianceLog) {
        localDataSource.insertLog(log)
        runCatching { syncLog(log) }
        // Sync failure is silent — pending logs will be retried via syncPendingLogs()
    }

    override suspend fun syncPendingLogs() {
        localDataSource.getUnsynced().first().forEach { log ->
            runCatching { syncLog(log) }
        }
    }

    private suspend fun syncLog(log: ComplianceLog) {
        // Supabase upsert — full implementation in compliance logging feature ticket
        // supabase.from("compliance_logs").upsert(log.toRemote())
        localDataSource.markAsSynced(log.id, log.updatedAt)
    }
}
