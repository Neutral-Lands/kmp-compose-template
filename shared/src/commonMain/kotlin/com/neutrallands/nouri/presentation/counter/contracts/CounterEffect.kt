package com.neutrallands.nouri.presentation.counter.contracts

import com.neutrallands.nouri.presentation.base.Effect

sealed interface CounterEffect : Effect {
    data object LimitReached : CounterEffect
}
