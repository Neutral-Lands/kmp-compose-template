package com.nouri.di

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
) = listOf(
    dataModule(supabaseUrl, supabaseAnonKey, driverFactory, connectivityObserver),
    domainModule(),
    presentationModule(),
)
