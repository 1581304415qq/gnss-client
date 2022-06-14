package com.example.gnss_app.ble.model

import com.example.gnss_app.protocol.Data
import com.example.gnss_app.utils.readUInt

class AppInfo() : Data() {
    val appVersion: UInt
        get() = body?.readUInt() ?: 0u

    override fun toByteArray(): ByteArray {
        return ByteArray(0)
    }
}