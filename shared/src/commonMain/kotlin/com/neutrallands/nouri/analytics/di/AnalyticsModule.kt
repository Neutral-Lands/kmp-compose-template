package com.neutrallands.nouri.analytics.di

import com.neutrallands.nouri.analytics.CrashReporter
import com.neutrallands.nouri.analytics.NouriAnalytics
import org.koin.dsl.module

fun analyticsModule(
    analytics: NouriAnalytics,
    crashReporter: CrashReporter,
) = module {
    single<NouriAnalytics> { analytics }
    single<CrashReporter> { crashReporter }
}
