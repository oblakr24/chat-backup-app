package com.rokoblak.chatbackup.data.model

sealed interface OperationResult<out T : Any?> {
    data class Done<out T : Any>(val data: T) : OperationResult<T>
    data class Error(val msg: String) : OperationResult<Nothing>

    fun <R : Any> map(mapper: (T) -> R): OperationResult<R> = when (this) {
        is Done -> Done(mapper(data))
        is Error -> this
    }
}