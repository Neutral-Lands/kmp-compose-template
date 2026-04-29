package com.nouri.presentation.base

import app.cash.turbine.test
import com.nouri.domain.model.DomainError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

private data class TestState(val value: Int = 0) : State
private sealed interface TestIntent : Intent
private sealed interface TestAction : Action
private sealed interface TestEffect : Effect

private class TestViewModel : BaseViewModel<TestState, TestIntent, TestAction, TestEffect>(TestState()) {
    override fun handleIntent(intent: TestIntent) = Unit
    override suspend fun processAction(action: TestAction) = Unit
    fun triggerError(t: Throwable) = handleError(t)
}

class BaseViewModelErrorTest {

    @Test
    fun `when handleError called then error flow emits UnknownError`() = runTest {
        val viewModel = TestViewModel()
        val cause = RuntimeException("test error")

        viewModel.error.test {
            viewModel.triggerError(cause)
            val emitted = awaitItem()
            assertIs<DomainError.UnknownError>(emitted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when handleError called then emitted error wraps original throwable`() = runTest {
        val viewModel = TestViewModel()
        val cause = IllegalStateException("state issue")

        viewModel.error.test {
            viewModel.triggerError(cause)
            val emitted = awaitItem()
            assertSame(cause, emitted.cause)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple handleError calls emit multiple errors`() = runTest {
        val viewModel = TestViewModel()

        viewModel.error.test {
            viewModel.triggerError(RuntimeException("first"))
            viewModel.triggerError(RuntimeException("second"))
            assertIs<DomainError.UnknownError>(awaitItem())
            assertIs<DomainError.UnknownError>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
