package com.neutrallands.nouri.di

import com.neutrallands.nouri.analytics.CrashReporter
import com.neutrallands.nouri.analytics.NouriAnalytics
import com.neutrallands.nouri.analytics.di.analyticsModule
import com.neutrallands.nouri.data.di.dataModule
import com.neutrallands.nouri.data.local.DatabaseDriverFactory
import com.neutrallands.nouri.domain.di.domainModule
import com.neutrallands.nouri.domain.model.ConnectivityObserver
import com.neutrallands.nouri.presentation.di.presentationModule

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
