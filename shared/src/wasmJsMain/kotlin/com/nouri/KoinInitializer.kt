package com.nouri

import com.nouri.data.di.dataModule
import org.koin.core.context.startKoin

// Web credentials injected at runtime — see NEU-84 for full env var strategy
fun initKoin(supabaseUrl: String, supabaseAnonKey: String) {
    startKoin { modules(dataModule(supabaseUrl, supabaseAnonKey)) }
}
