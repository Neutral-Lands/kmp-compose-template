package com.neutrallands.nouri.presentation.counter.contracts

import com.neutrallands.nouri.presentation.base.State

data class CounterState(
    val count: Int = 0,
) : State
