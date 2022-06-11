package com.example.gnss_app.network

sealed class VivaEventType {
    object ERROR : VivaEventType()
    sealed class Message
    {
        object CONNECTED : VivaEventType()
        object MESSAGE : VivaEventType()
        object DISCONNECT:VivaEventType()
        object ERROR:VivaEventType()
        object RECEIVE:VivaEventType()
    }
    sealed class Video
    {
        object RECEIVE:VivaEventType()
    }
    sealed class File
    {
        object ERROR:VivaEventType()
        object RECEIVE:VivaEventType()
    }
}