package com.nouri.data.repository

import app.cash.turbine.test
import com.nouri.data.datasource.local.ComplianceLogLocalDataSource
import com.nouri.data.datasource.remote.provideSupabaseClient
import com.nouri.data.local.DatabaseDriverFactory
import com.nouri.data.local.NouriDatabase
import com.nouri.domain.model.ComplianceLog
import com.nouri.domain.model.ComplianceStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplianceRepositoryImplTest {

    private lateinit var repository: ComplianceRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = DatabaseDriverFactory().create()
        val localDataSource = ComplianceLogLocalDataSource(NouriDatabase(driver))
        val supabase = provideSupabaseClient("https://fake.supabase.co", "fake-anon-key")
        repository = ComplianceRepositoryImpl(localDataSource, supabase)
    }

    @Test
    fun `given saved log when getLogsForDate then returns it`() = runTest {
        val log = makeLog(id = "repo-1", patientId = "p1", logDate = "2026-04-29")
        repository.saveLog(log)

        repository.getLogsForDate("p1", "2026-04-29").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("repo-1", results[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no logs when getLogsForDate then returns empty`() = runTest {
        repository.getLogsForDate("nobody", "2026-04-29").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given unsynced log when syncPendingLogs then log becomes synced`() = runTest {
        val log = makeLog(id = "sync-me", isSynced = false)
        repository.saveLog(log)
        repository.syncPendingLogs()

        repository.getLogsForDate(log.patientId, log.logDate).test {
            val results = awaitItem()
            assertTrue(results.all { it.isSynced })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveLog marks log as synced after saving`() = runTest {
        val log = makeLog(id = "save-sync")
        repository.saveLog(log)

        repository.getLogsForDate(log.patientId, log.logDate).test {
            val result = awaitItem().first()
            assertTrue(result.isSynced)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun makeLog(
        id: String = "test-id",
        patientId: String = "patient-1",
        isSynced: Boolean = false,
        logDate: String = "2026-04-29",
    ) = ComplianceLog(
        id = id,
        patientId = patientId,
        mealPlanSlotId = "slot-1",
        logDate = logDate,
        status = ComplianceStatus.FOLLOWED,
        note = null,
        isSynced = isSynced,
        createdAt = "2026-04-29T10:00:00Z",
        updatedAt = "2026-04-29T10:00:00Z",
    )
}
