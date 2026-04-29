package com.nouri.presentation.di

import com.nouri.presentation.counter.CounterViewModel
import org.koin.dsl.module

// ViewModels are registered here as screens are implemented
fun presentationModule() =
    module {
        factory { CounterViewModel() }
    }
