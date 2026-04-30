package com.neutrallands.nouri.data.di

import com.neutrallands.nouri.data.datasource.local.ComplianceLogLocalDataSource
import com.neutrallands.nouri.data.datasource.remote.SubscriptionPlanDataSource
import com.neutrallands.nouri.data.datasource.remote.provideSupabaseClient
import com.neutrallands.nouri.data.local.DatabaseDriverFactory
import com.neutrallands.nouri.data.local.NouriDatabase
import com.neutrallands.nouri.data.repository.ComplianceRepositoryImpl
import com.neutrallands.nouri.domain.model.ConnectivityObserver
import com.neutrallands.nouri.domain.repository.ComplianceRepository
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
