package com.example.gnss_app.network

object VivaServiceID {
    val HEART: UShort by lazy { 0xFF00u }
    val MESSAGE: UShort by lazy { 0xFF01u }
    val VIDEO: UShort by lazy { 0xFF02u }
    val MEDIA: UShort by lazy { 0xFF03u }
    val COMMAND: UShort by lazy { 0xFF04u }
    val FILE: UShort by lazy { 0xFF05u }
}