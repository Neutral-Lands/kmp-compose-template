package com.nouri.domain.model

data class ComplianceLog(
    val id: String,
    val patientId: String,
    val mealPlanSlotId: String,
    val logDate: String,
    val status: ComplianceStatus,
    val note: String?,
    val isSynced: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
