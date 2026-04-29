package com.nouri.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nouri.domain.model.DomainError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : State, I : Intent, A : Action, E : Effect>(
    initialState: S,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = Channel<E>(Channel.BUFFERED)
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    private val _error = Channel<DomainError>(Channel.BUFFERED)
    val error: Flow<DomainError> = _error.receiveAsFlow()

    val currentState: S
        get() = _uiState.value

    abstract fun handleIntent(intent: I)

    protected fun handleAction(action: A) {
        viewModelScope.launch { processAction(action) }
    }

    protected abstract suspend fun processAction(action: A)

    protected fun updateState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }

    protected suspend fun emitEffect(effect: E) {
        _uiEffect.send(effect)
    }

    protected open fun handleError(throwable: Throwable) {
        val error = DomainError.from(throwable)
        viewModelScope.launch { _error.send(error) }
    }
}
