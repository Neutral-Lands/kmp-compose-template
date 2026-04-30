package com.neutrallands.nouri.presentation.counter.contracts

import com.neutrallands.nouri.presentation.base.Action

sealed interface CounterAction : Action {
    data object Increment : CounterAction

    data object Decrement : CounterAction

    data object Reset : CounterAction
}
