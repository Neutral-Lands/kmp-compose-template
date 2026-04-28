package com.nouri.presentation.counter.contracts

import com.nouri.presentation.base.Effect

sealed interface CounterEffect : Effect {
    data object LimitReached : CounterEffect
}
