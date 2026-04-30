package com.neutrallands.nouri

import com.neutrallands.nouri.analytics.IosNouriAnalytics
import com.neutrallands.nouri.data.connectivity.IosConnectivityObserver
import com.neutrallands.nouri.data.local.DatabaseDriverFactory
import com.neutrallands.nouri.di.appModule
import org.koin.core.context.startKoin

fun initKoin(
    supabaseUrl: String,
    supabaseAnonKey: String,
) {
    startKoin {
        modules(
            appModule(
                supabaseUrl = supabaseUrl,
                supabaseAnonKey = supabaseAnonKey,
                driverFactory = DatabaseDriverFactory(),
                connectivityObserver = IosConnectivityObserver(),
                analytics = IosNouriAnalytics,
                crashReporter = IosNouriAnalytics,
            ),
        )
    }
}
