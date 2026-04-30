package com.neutrallands.nouri.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.neutrallands.nouri.data.local.NouriDatabase

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun create(): SqlDriver = AndroidSqliteDriver(NouriDatabase.Schema, context, "nouri_cache.db")
}
