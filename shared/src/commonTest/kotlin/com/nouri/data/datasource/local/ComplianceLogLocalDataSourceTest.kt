package com.nouri.data.datasource.local

import app.cash.turbine.test
import com.nouri.data.local.DatabaseDriverFactory
import com.nouri.data.local.NouriDatabase
import com.nouri.domain.model.ComplianceLog
import com.nouri.domain.model.ComplianceStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplianceLogLocalDataSourceTest {

    private lateinit var dataSource: ComplianceLogLocalDataSource

    @BeforeTest
    fun setup() {
        val driver = DatabaseDriverFactory().create()
        dataSource = ComplianceLogLocalDataSource(NouriDatabase(driver))
    }

    @Test
    fun `given inserted log when getByPatientAndDate then returns log`() = runTest {
        val log = makeLog(id = "log-1", patientId = "patient-1", logDate = "2026-04-29")
        dataSource.insertLog(log)

        dataSource.getByPatientAndDate("patient-1", "2026-04-29").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals(log.id, results[0].id)
            assertEquals(log.patientId, results[0].patientId)
            assertEquals(log.logDate, results[0].logDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no logs when getByPatientAndDate then returns empty list`() = runTest {
        dataSource.getByPatientAndDate("unknown-patient", "2026-04-29").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given unsynced log when getUnsynced then includes it`() = runTest {
        val log = makeLog(id = "unsynced-1", isSynced = false)
        dataSource.insertLog(log)

        dataSource.getUnsynced().test {
            val results = awaitItem()
            assertTrue(results.any { it.id == "unsynced-1" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no logs when getUnsynced then returns empty`() = runTest {
        dataSource.getUnsynced().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given unsynced log when markAsSynced then getUnsynced excludes it`() = runTest {
        val log = makeLog(id = "to-sync", isSynced = false)
        dataSource.insertLog(log)
        dataSource.markAsSynced("to-sync", "2026-04-29T12:00:00Z")

        dataSource.getUnsynced().test {
            val results = awaitItem()
            assertTrue(results.none { it.id == "to-sync" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given log with null note when retrieved then note is null`() = runTest {
        val log = makeLog(id = "note-null", note = null)
        dataSource.insertLog(log)

        dataSource.getByPatientAndDate(log.patientId, log.logDate).test {
            val result = awaitItem().first()
            assertEquals(null, result.note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given log with note when retrieved then note is preserved`() = runTest {
        val log = makeLog(id = "note-set", note = "Ate half the meal")
        dataSource.insertLog(log)

        dataSource.getByPatientAndDate(log.patientId, log.logDate).test {
            val result = awaitItem().first()
            assertEquals("Ate half the meal", result.note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given FOLLOWED status when inserted and retrieved then status is FOLLOWED`() = runTest {
        val log = makeLog(id = "status-test", status = ComplianceStatus.FOLLOWED)
        dataSource.insertLog(log)

        dataSource.getByPatientAndDate(log.patientId, log.logDate).test {
            assertEquals(ComplianceStatus.FOLLOWED, awaitItem().first().status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given MISSED status when inserted and retrieved then status is MISSED`() = runTest {
        val log = makeLog(id = "status-missed", status = ComplianceStatus.MISSED)
        dataSource.insertLog(log)

        dataSource.getByPatientAndDate(log.patientId, log.logDate).test {
            assertEquals(ComplianceStatus.MISSED, awaitItem().first().status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given PARTIAL status when inserted and retrieved then status is PARTIAL`() = runTest {
        val log = makeLog(id = "status-partial", status = ComplianceStatus.PARTIAL)
        dataSource.insertLog(log)

        dataSource.getByPatientAndDate(log.patientId, log.logDate).test {
            assertEquals(ComplianceStatus.PARTIAL, awaitItem().first().status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given multiple logs for same patient on same date when retrieved then returns all`() = runTest {
        dataSource.insertLog(makeLog(id = "multi-1", mealPlanSlotId = "slot-1"))
        dataSource.insertLog(makeLog(id = "multi-2", mealPlanSlotId = "slot-2"))

        dataSource.getByPatientAndDate("patient-1", "2026-04-29").test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun makeLog(
        id: String = "test-id",
        patientId: String = "patient-1",
        mealPlanSlotId: String = "slot-1",
        logDate: String = "2026-04-29",
        status: ComplianceStatus = ComplianceStatus.FOLLOWED,
        note: String? = null,
        isSynced: Boolean = false,
    ) = ComplianceLog(
        id = id,
        patientId = patientId,
        mealPlanSlotId = mealPlanSlotId,
        logDate = logDate,
        status = status,
        note = note,
        isSynced = isSynced,
        createdAt = "2026-04-29T10:00:00Z",
        updatedAt = "2026-04-29T10:00:00Z",
    )
}
