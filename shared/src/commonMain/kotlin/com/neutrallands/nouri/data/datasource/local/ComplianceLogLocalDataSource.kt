package com.neutrallands.nouri.data.datasource.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.neutrallands.nouri.data.local.NouriDatabase
import com.neutrallands.nouri.domain.model.ComplianceLog
import com.neutrallands.nouri.domain.model.ComplianceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ComplianceLogLocalDataSource(
    private val database: NouriDatabase,
) {
    fun getByPatientAndDate(
        patientId: String,
        date: String,
    ): Flow<List<ComplianceLog>> =
        database.complianceLogQueries
            .selectByPatientAndDate(patientId, date)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    fun getUnsynced(): Flow<List<ComplianceLog>> =
        database.complianceLogQueries
            .selectUnsynced()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    suspend fun insertLog(log: ComplianceLog) {
        database.complianceLogQueries.insertLog(
            id = log.id,
            patient_id = log.patientId,
            meal_plan_slot_id = log.mealPlanSlotId,
            log_date = log.logDate,
            status = log.status.name,
            note = log.note,
            created_at = log.createdAt,
            updated_at = log.updatedAt,
        )
    }

    suspend fun markAsSynced(
        id: String,
        updatedAt: String,
    ) {
        database.complianceLogQueries.markAsSynced(updated_at = updatedAt, id = id)
    }

    private fun com.nouri.data.local.ComplianceLog.toDomain() =
        ComplianceLog(
            id = id,
            patientId = patient_id,
            mealPlanSlotId = meal_plan_slot_id,
            logDate = log_date,
            status = ComplianceStatus.valueOf(status),
            note = note,
            isSynced = is_synced == 1L,
            createdAt = created_at,
            updatedAt = updated_at,
        )
}
