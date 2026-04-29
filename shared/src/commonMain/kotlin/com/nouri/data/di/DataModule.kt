package com.nouri.data.di

import com.nouri.data.datasource.local.ComplianceLogLocalDataSource
import com.nouri.data.datasource.remote.SubscriptionPlanDataSource
import com.nouri.data.datasource.remote.provideSupabaseClient
import com.nouri.data.local.DatabaseDriverFactory
import com.nouri.data.local.NouriDatabase
import com.nouri.data.repository.ComplianceRepositoryImpl
import com.nouri.domain.model.ConnectivityObserver
import com.nouri.domain.repository.ComplianceRepository
import org.koin.dsl.module

fun dataModule(
    supabaseUrl: String,
    supabaseAnonKey: String,
    driverFactory: DatabaseDriverFactory,
    connectivityObserver: ConnectivityObserver,
) = module {
    single<ConnectivityObserver> { connectivityObserver }
    single { provideSupabaseClient(supabaseUrl, supabaseAnonKey) }
    single { SubscriptionPlanDataSource(get()) }
    single { driverFactory.create() }
    single { NouriDatabase(get()) }
    single { ComplianceLogLocalDataSource(get()) }
    single<ComplianceRepository> { ComplianceRepositoryImpl(get(), get()) }
}
