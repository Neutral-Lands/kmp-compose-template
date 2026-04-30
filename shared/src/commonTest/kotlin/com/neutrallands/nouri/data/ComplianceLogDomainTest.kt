package com.neutrallands.nouri.data

import com.neutrallands.nouri.domain.model.ComplianceLog
import com.neutrallands.nouri.domain.model.ComplianceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComplianceLogDomainTest {
    @Test
    fun `given FOLLOWED status string when valueOf then returns FOLLOWED`() {
        assertEquals(ComplianceStatus.FOLLOWED, ComplianceStatus.valueOf("FOLLOWED"))
    }

    @Test
    fun `given MISSED status string when valueOf then returns MISSED`() {
        assertEquals(ComplianceStatus.MISSED, ComplianceStatus.valueOf("MISSED"))
    }

    @Test
    fun `given PARTIAL status string when valueOf then returns PARTIAL`() {
        assertEquals(ComplianceStatus.PARTIAL, ComplianceStatus.valueOf("PARTIAL"))
    }

    @Test
    fun `given unsynced log when isSynced then returns false`() {
        val log = makeLog(isSynced = false)
        assertFalse(log.isSynced)
    }

    @Test
    fun `given synced log when isSynced then returns true`() {
        val log = makeLog(isSynced = true)
        assertTrue(log.isSynced)
    }

    @Test
    fun `given log with no note when note then returns null`() {
        val log = makeLog(note = null)
        assertNull(log.note)
    }

    @Test
    fun `given log with note when note then returns note text`() {
        val log = makeLog(note = "Ate half the meal")
        assertEquals("Ate half the meal", log.note)
    }

    private fun makeLog(
        isSynced: Boolean = false,
        note: String? = null,
    ) = ComplianceLog(
        id = "test-id",
        patientId = "patient-1",
        mealPlanSlotId = "slot-1",
        logDate = "2026-04-28",
        status = ComplianceStatus.FOLLOWED,
        note = note,
        isSynced = isSynced,
        createdAt = "2026-04-28T10:00:00Z",
        updatedAt = "2026-04-28T10:00:00Z",
    )
}
