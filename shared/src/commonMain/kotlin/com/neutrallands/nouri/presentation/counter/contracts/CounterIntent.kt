package com.neutrallands.nouri.presentation.counter.contracts

import com.neutrallands.nouri.presentation.base.Intent

sealed interface CounterIntent : Intent {
    data object Increment : CounterIntent

    data object Decrement : CounterIntent

    data object Reset : CounterIntent
}
