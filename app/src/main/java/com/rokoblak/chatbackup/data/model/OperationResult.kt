package com.rokoblak.chatbackup.data.model

interface RootError

sealed interface OperationResult<out T : Any?, out E : RootError> {
    data class Done<out T : Any, out E : RootError>(val data: T) : OperationResult<T, E>
    data class Error<out E : RootError>(val error: E) : OperationResult<Nothing, E>

    fun <R : Any> map(mapper: (T) -> R): OperationResult<R, E> = when (this) {
        is Done -> Done(mapper(data))
        is Error -> this
    }

    fun optValue() = when (this) {
        is Done -> data
        is Error -> null
    }

    fun doOnError(block: (error: Error<E>) -> Unit): OperationResult<T, E> {
        when (this) {
            is Done -> Unit
            is Error -> block(this)
        }
        return this
    }

    fun doOnSuccess(block: (data: T) -> Unit): OperationResult<T, E> {
        when (this) {
            is Done -> block(data)
            is Error -> Unit
        }
        return this
    }
}