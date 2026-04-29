package com.nouri.domain.model

sealed class DomainError(
    open val cause: Throwable? = null,
) {
    data class NetworkError(
        override val cause: Throwable? = null,
    ) : DomainError(cause)

    data class AuthError(
        override val cause: Throwable? = null,
    ) : DomainError(cause)

    data class NotFoundError(
        override val cause: Throwable? = null,
    ) : DomainError(cause)

    data class UnknownError(
        override val cause: Throwable? = null,
    ) : DomainError(cause)

    companion object {
        fun from(throwable: Throwable): DomainError = UnknownError(throwable)
    }
}
