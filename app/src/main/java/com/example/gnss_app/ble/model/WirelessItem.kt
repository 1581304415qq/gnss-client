package com.example.gnss_app.ble.model

data class WirelessItem(
    val RSSI: Int,
    val authmode: Int,
    val bssid: String,
    val channel: Int,
    val hidden: Boolean,
    val ssid: String
)