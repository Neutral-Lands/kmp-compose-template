package com.nouri

import com.nouri.analytics.NoOpAnalytics
import com.nouri.data.connectivity.WasmConnectivityObserver
import com.nouri.data.local.DatabaseDriverFactory
import com.nouri.di.appModule
import org.koin.core.context.startKoin

// Web credentials injected at runtime — see NEU-84 for full env var strategy
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
                connectivityObserver = WasmConnectivityObserver(),
                analytics = NoOpAnalytics,
                crashReporter = NoOpAnalytics,
            ),
        )
    }
}
