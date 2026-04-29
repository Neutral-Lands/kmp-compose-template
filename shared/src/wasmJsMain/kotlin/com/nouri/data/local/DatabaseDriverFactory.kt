package com.nouri.data.local

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    // Persistent Web storage is deferred — IndexedDB/WasmSQLite integration TBD
    actual fun create(): SqlDriver = error("SQLDelight Web driver not yet implemented.")
}
