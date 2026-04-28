package com.nouri.data.di

import com.nouri.data.datasource.remote.SubscriptionPlanDataSource
import com.nouri.data.datasource.remote.provideSupabaseClient
import org.koin.dsl.module

fun dataModule(supabaseUrl: String, supabaseAnonKey: String) = module {
    single { provideSupabaseClient(supabaseUrl, supabaseAnonKey) }
    single { SubscriptionPlanDataSource(get()) }
}
