package com.rokoblak.chatbackup.data.model

interface RootError

sealed interface OperationResult<out T : Any?, out E: RootError> {
    data class Done<out T : Any, out E: RootError>(val data: T) : OperationResult<T, E>
    data class Error<out E: RootError>(val error: E) : OperationResult<Nothing, E>

    fun <R : Any> map(mapper: (T) -> R): OperationResult<R, E> = when (this) {
        is Done -> Done(mapper(data))
        is Error -> this
    }
}