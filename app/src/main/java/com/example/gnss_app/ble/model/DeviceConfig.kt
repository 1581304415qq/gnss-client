package com.example.gnss_app.ble.model

import com.example.gnss_app.protocol.Data
import com.example.gnss_app.utils.readInt8

sealed class DeviceConfig : Data() {
    var response: ByteArray? = null

    val result: Int
        get() = response!![0].toInt()

    class NetMode() : DeviceConfig() {
        private var _value: Int = -1
        var value: Int
            set(v) {
                _value = v
            }
            get() = body!![0].toInt()

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_value.toByte())
        }
    }

    class Token() : DeviceConfig() {
        var _value: String = ""
        var value: String
            set(v) {
                _value = v
            }
            get() = String(body!!)

        override fun toByteArray(): ByteArray? {
            return _value.toByteArray()
        }
    }

    class SysPWD() : DeviceConfig() {
        private var _value: String? = null
        var value: String
            set(v) {
                _value = v
            }
            get() = String(body!!)

        override fun toByteArray(): ByteArray? {
            return _value?.toByteArray()
        }
    }

    class Tel() : DeviceConfig() {
        var _value: String = ""
        var value: String
            set(v) {
                _value = v
            }
            get() = String(body!!)

        override fun toByteArray(): ByteArray? {
            return _value.toByteArray()
        }
    }

    class Server() : DeviceConfig() {
        private var _id: Int = 0
        var id: Int
            get() = body?.readInt8()?.toInt() ?: 0
            set(value) {
                _id = value
            }
        var ip: Int = 0
        var port: Int = 0

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_id.toByte())
        }

    }

}