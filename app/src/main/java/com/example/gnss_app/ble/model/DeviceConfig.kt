package com.example.gnss_app.ble.model

import com.example.gnss_app.network.util.*
import com.example.gnss_app.protocol.Data

sealed class DeviceConfig : Data() {
    var response: ByteArray? = null

    val result: Int
        get() = response!![0].toInt()

    class WorkMode() : DeviceConfig() {
        private var _value: Int = -1
        var value: Int
            set(v) {
                _value = v
            }
            get() = if (body != null) body!![0].toInt() else -1

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_value.toByte())
        }
    }

    class NetMode() : DeviceConfig() {
        private var _value: Int = -1
        var value: Int
            set(v) {
                _value = v
            }
            get() = if (body != null) body!![0].toInt() else -1

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_value.toByte())
        }
    }

    class BaseStringData() : DeviceConfig() {
        var _value: String = ""
        var value: String
            set(v) {
                _value = v
            }
            get() = if(body==null) "" else String(body!!)

        override fun toByteArray(): ByteArray? {
            return _value.toByteArray()
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

    class HostAddress() : DeviceConfig() {
        private var _id: Int = 0
        var id: Int
            get() = body?.readInt8()?.toInt() ?: 0
            set(value) {
                _id = value
            }
        private var _ip: UInt = 0u
        var ip: UInt
            get() = body?.readUInt(1) ?: 0u
            set(value) {
                _ip = value
            }

        private var _port: UShort = 0u
        var port: UShort
            get() = body?.readInt16LB(5) ?: 0u
            set(value) {
                _port = value
            }

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_id.toByte()) + _ip.toByteArray() + _port.toLB().toByteArray()
        }
    }

    class NtripServer() : DeviceConfig() {
        val server = HostAddress()
        val mount = BaseStringData()
        val account = BaseStringData()
        val password = BaseStringData()
        override fun toByteArray(): ByteArray? {
            throw  Exception("base object")
        }
    }

}