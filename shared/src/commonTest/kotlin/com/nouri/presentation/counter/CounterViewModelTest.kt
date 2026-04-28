package com.nouri.presentation.counter

import app.cash.turbine.test
import com.nouri.presentation.counter.contracts.CounterEffect
import com.nouri.presentation.counter.contracts.CounterIntent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CounterViewModelTest {

    @Test
    fun `initial state has count zero`() {
        val viewModel = CounterViewModel()
        assertEquals(0, viewModel.currentState.count)
    }

    @Test
    fun `given count is zero when increment then count is one`() = runTest {
        val viewModel = CounterViewModel()
        viewModel.handleIntent(CounterIntent.Increment)
        viewModel.uiState.test {
            assertEquals(1, awaitItem().count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given count is zero when decrement then count stays zero`() = runTest {
        val viewModel = CounterViewModel()
        viewModel.handleIntent(CounterIntent.Decrement)
        viewModel.uiState.test {
            assertEquals(0, awaitItem().count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given count is above zero when reset then count is zero`() = runTest {
        val viewModel = CounterViewModel()
        repeat(3) { viewModel.handleIntent(CounterIntent.Increment) }
        viewModel.handleIntent(CounterIntent.Reset)
        viewModel.uiState.test {
            assertEquals(0, awaitItem().count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given count is at limit when increment then emits LimitReached effect`() = runTest {
        val viewModel = CounterViewModel()
        repeat(10) { viewModel.handleIntent(CounterIntent.Increment) }
        viewModel.uiEffect.test {
            viewModel.handleIntent(CounterIntent.Increment)
            assertEquals(CounterEffect.LimitReached, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given count is at limit when increment then count stays at limit`() = runTest {
        val viewModel = CounterViewModel()
        repeat(10) { viewModel.handleIntent(CounterIntent.Increment) }
        viewModel.handleIntent(CounterIntent.Increment)
        viewModel.uiState.test {
            assertEquals(10, awaitItem().count)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
