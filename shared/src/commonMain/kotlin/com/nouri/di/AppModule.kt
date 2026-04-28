package com.nouri.di

import com.nouri.data.di.dataModule
import com.nouri.domain.di.domainModule
import com.nouri.presentation.di.presentationModule

fun appModule(supabaseUrl: String, supabaseAnonKey: String) = listOf(
    dataModule(supabaseUrl, supabaseAnonKey),
    domainModule(),
    presentationModule()
)
