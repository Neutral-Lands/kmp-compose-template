package com.neutrallands.nouri.presentation.di

import com.neutrallands.nouri.presentation.counter.CounterViewModel
import org.koin.dsl.module

// ViewModels are registered here as screens are implemented
fun presentationModule() =
    module {
        factory { CounterViewModel() }
    }
