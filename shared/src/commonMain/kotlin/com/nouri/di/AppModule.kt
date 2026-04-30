package com.nouri.di

import com.nouri.analytics.CrashReporter
import com.nouri.analytics.NouriAnalytics
import com.nouri.analytics.di.analyticsModule
import com.nouri.data.di.dataModule
import com.nouri.data.local.DatabaseDriverFactory
import com.nouri.domain.di.domainModule
import com.nouri.domain.model.ConnectivityObserver
import com.nouri.presentation.di.presentationModule

fun appModule(
    supabaseUrl: String,
    supabaseAnonKey: String,
    driverFactory: DatabaseDriverFactory,
    connectivityObserver: ConnectivityObserver,
    analytics: NouriAnalytics,
    crashReporter: CrashReporter,
) = listOf(
    dataModule(supabaseUrl, supabaseAnonKey, driverFactory, connectivityObserver),
    domainModule(),
    presentationModule(),
    analyticsModule(analytics, crashReporter),
)
