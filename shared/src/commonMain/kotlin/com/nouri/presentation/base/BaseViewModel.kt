package com.nouri.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : State, I : Intent, A : Action, E : Effect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = Channel<E>(Channel.BUFFERED)
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    val currentState: S
        get() = _uiState.value

    // Subclass maps intent to action by calling handleAction(...)
    abstract fun handleIntent(intent: I)

    // Non-suspend bridge called from handleIntent; launches coroutine for processAction
    protected fun handleAction(action: A) {
        viewModelScope.launch { processAction(action) }
    }

    // Suspend — subclass implements actual business logic here
    protected abstract suspend fun processAction(action: A)

    protected fun updateState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }

    protected suspend fun emitEffect(effect: E) {
        _uiEffect.send(effect)
    }

    protected open fun handleError(throwable: Throwable) {
        // NEU-68: global error handling strategy
    }
}
