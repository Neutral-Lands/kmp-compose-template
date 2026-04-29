package com.nouri

import com.nouri.data.local.DatabaseDriverFactory
import com.nouri.di.appModule
import org.koin.core.context.startKoin

fun initKoin(
    supabaseUrl: String,
    supabaseAnonKey: String,
) {
    startKoin { modules(appModule(supabaseUrl, supabaseAnonKey, DatabaseDriverFactory())) }
}
