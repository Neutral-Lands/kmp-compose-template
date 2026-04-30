package com.neutrallands.nouri.presentation.counter

import com.neutrallands.nouri.presentation.base.BaseViewModel
import com.neutrallands.nouri.presentation.counter.contracts.CounterAction
import com.neutrallands.nouri.presentation.counter.contracts.CounterEffect
import com.neutrallands.nouri.presentation.counter.contracts.CounterIntent
import com.neutrallands.nouri.presentation.counter.contracts.CounterState

private const val COUNT_LIMIT = 10

class CounterViewModel :
    BaseViewModel<CounterState, CounterIntent, CounterAction, CounterEffect>(
        initialState = CounterState(),
    ) {
    override fun handleIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> handleAction(CounterAction.Increment)
            CounterIntent.Decrement -> handleAction(CounterAction.Decrement)
            CounterIntent.Reset -> handleAction(CounterAction.Reset)
        }
    }

    override suspend fun processAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment ->
                if (currentState.count >= COUNT_LIMIT) {
                    emitEffect(CounterEffect.LimitReached)
                } else {
                    updateState { copy(count = count + 1) }
                }
            CounterAction.Decrement -> updateState { copy(count = (count - 1).coerceAtLeast(0)) }
            CounterAction.Reset -> updateState { copy(count = 0) }
        }
    }
}
