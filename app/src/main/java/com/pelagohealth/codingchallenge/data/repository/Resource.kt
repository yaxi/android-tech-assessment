package com.pelagohealth.codingchallenge.data.repository

sealed class Resource<out T> {
    data class Success<T>(val data: T): Resource<T>()
    data class Error<T>(val error: Throwable?): Resource<T>()
}