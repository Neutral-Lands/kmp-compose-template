package com.nouri.presentation.counter.contracts

import com.nouri.presentation.base.Intent

sealed interface CounterIntent : Intent {
    data object Increment : CounterIntent
    data object Decrement : CounterIntent
    data object Reset : CounterIntent
}
