package com.example.gnss_app.network

import com.viva.libs.event.Event

sealed class VivaEvent<T>(override val data: T?, val message: String?):Event<T>(data) {
    class Success<T>(data: T) : VivaEvent<T>(data, null)
    class Error<T>(message: String) : VivaEvent<T>(null, message)
}