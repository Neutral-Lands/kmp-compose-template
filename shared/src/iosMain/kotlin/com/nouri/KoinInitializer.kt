package com.nouri

import com.nouri.data.di.dataModule
import org.koin.core.context.startKoin

fun initKoin(supabaseUrl: String, supabaseAnonKey: String) {
    startKoin { modules(dataModule(supabaseUrl, supabaseAnonKey)) }
}
