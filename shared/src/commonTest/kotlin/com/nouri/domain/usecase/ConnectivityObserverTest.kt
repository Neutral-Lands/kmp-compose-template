package com.nouri.domain.usecase

import com.nouri.domain.model.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeConnectivityObserver(
    initiallyOnline: Boolean = true,
) : ConnectivityObserver {
    private val _isOnline = MutableStateFlow(initiallyOnline)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}

class ConnectivityObserverTest {
    @Test
    fun `given default observer when created then isOnline is true`() =
        runTest {
            val observer = FakeConnectivityObserver()
            assertTrue(observer.isOnline.first())
        }

    @Test
    fun `given observer online when set offline then isOnline emits false`() =
        runTest {
            val observer = FakeConnectivityObserver(initiallyOnline = true)
            observer.setOnline(false)
            assertFalse(observer.isOnline.first())
        }

    @Test
    fun `given observer offline when set online then isOnline emits true`() =
        runTest {
            val observer = FakeConnectivityObserver(initiallyOnline = false)
            observer.setOnline(true)
            assertTrue(observer.isOnline.first())
        }
}
