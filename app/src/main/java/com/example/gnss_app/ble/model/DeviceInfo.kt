package com.example.gnss_app.ble.model

import com.example.gnss_app.protocol.Data
import com.example.gnss_app.utils.readUInt

sealed class DeviceInfo : Data() {

    class AppInfo() : DeviceInfo() {
        val appVersion: UInt
            get() = body?.readUInt() ?: 0u

        val info: String
            get() = String(body!!)
    }

    class CSQ() : DeviceInfo() {
        val rssi: Int
            get() = body?.readUInt()?.toInt() ?: 0
    }

    /**
     * 设备与服务器的连接状态
     * 接收一个UINT32 拆分4 Byte 每一个代表一个socket连接状态
     */
    class GSTCDE() : DeviceInfo() {
        val connectState1: Int
            get() = body?.get(0)?.toInt() ?: 0
        val connectState2: Int
            get() = body?.get(1)?.toInt() ?: 0
        val connectState3: Int
            get() = body?.get(2)?.toInt() ?: 0
        val connectState4: Int
            get() = body?.get(3)?.toInt() ?: 0

    }

    class DeviceIP() : DeviceInfo() {
        val ip: String
            get() = if (body != null) String(body!!) else ""
    }

    class DeviceCCID() : DeviceInfo() {
        val ccid: String
            get() = if (body != null) String(body!!) else ""
    }

    class DeviceIMEI() : DeviceInfo() {
        val imei: String
            get() = if (body != null) String(body!!) else ""
    }


    override fun toByteArray(): ByteArray? = null
}