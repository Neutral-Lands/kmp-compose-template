package com.neutrallands.nouri.analytics

interface CrashReporter {
    fun setUserId(userId: String)

    fun recordException(throwable: Throwable)
}
