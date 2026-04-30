package com.neutrallands.nouri.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.neutrallands.nouri.data.local.NouriDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver = NativeSqliteDriver(NouriDatabase.Schema, "nouri_cache.db")
}
