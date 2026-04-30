package com.nouri.analytics.di

import com.nouri.analytics.CrashReporter
import com.nouri.analytics.NouriAnalytics
import org.koin.dsl.module

fun analyticsModule(
    analytics: NouriAnalytics,
    crashReporter: CrashReporter,
) = module {
    single<NouriAnalytics> { analytics }
    single<CrashReporter> { crashReporter }
}
