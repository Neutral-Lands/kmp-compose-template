package com.nouri.presentation.counter.contracts

import com.nouri.presentation.base.Action

sealed interface CounterAction : Action {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data object Reset : CounterAction
}
