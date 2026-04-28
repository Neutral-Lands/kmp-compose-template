package com.nouri.presentation.counter.contracts

import com.nouri.presentation.base.State

data class CounterState(
    val count: Int = 0
) : State
