package com.ktoryoutube

sealed interface MainUIState<T> {
    class Loading<T> : MainUIState<T>
    data class Success<T>(val response:T) : MainUIState<T>
    data class Error<T>(val message:Exception) : MainUIState<T>
}