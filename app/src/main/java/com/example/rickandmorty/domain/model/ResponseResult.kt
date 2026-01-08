package com.example.rickandmorty.domain.model

sealed class ResponseResult<T> (
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T?): ResponseResult<T>(data = data)
    class Error<T>(message: String?): ResponseResult<T>(message = message)
}
