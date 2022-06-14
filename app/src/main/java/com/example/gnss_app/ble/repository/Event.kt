package com.example.gnss_app.ble.repository


sealed class Event(val data: ByteArray?, val message: String?) {
    class Success(data: ByteArray) : Event(data, null)
    class Error(message: String) : Event(null, message)
}