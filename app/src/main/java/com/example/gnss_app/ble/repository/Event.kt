package com.example.gnss_app.ble.repository

import com.example.gnss_app.ble.model.Wireless


sealed class Event<T>(val data: T?, val message: String?){
    class WifiScanResult(data: Wireless):
        Event<Wireless>(data, null)
}