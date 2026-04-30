package com.neutrallands.nouri.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class DomainErrorTest {
    @Test
    fun `given throwable when from then returns UnknownError with cause`() {
        val cause = RuntimeException("boom")
        val error = DomainError.from(cause)
        assertIs<DomainError.UnknownError>(error)
        assertSame(cause, error.cause)
    }

    @Test
    fun `NetworkError default cause is null`() {
        val error = DomainError.NetworkError()
        assertNull(error.cause)
    }

    @Test
    fun `NetworkError stores cause`() {
        val cause = RuntimeException("network")
        val error = DomainError.NetworkError(cause)
        assertSame(cause, error.cause)
    }

    @Test
    fun `AuthError default cause is null`() {
        assertNull(DomainError.AuthError().cause)
    }

    @Test
    fun `AuthError stores cause`() {
        val cause = RuntimeException("auth")
        assertSame(cause, DomainError.AuthError(cause).cause)
    }

    @Test
    fun `NotFoundError default cause is null`() {
        assertNull(DomainError.NotFoundError().cause)
    }

    @Test
    fun `NotFoundError stores cause`() {
        val cause = RuntimeException("not found")
        assertSame(cause, DomainError.NotFoundError(cause).cause)
    }

    @Test
    fun `UnknownError default cause is null`() {
        assertNull(DomainError.UnknownError().cause)
    }

    @Test
    fun `two NetworkErrors with same cause are equal`() {
        val cause = RuntimeException("net")
        assertEquals(DomainError.NetworkError(cause), DomainError.NetworkError(cause))
    }
}
