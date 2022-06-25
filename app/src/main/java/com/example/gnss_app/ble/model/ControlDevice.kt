package com.example.gnss_app.ble.model

import com.example.gnss_app.network.util.readInt16
import com.example.gnss_app.network.util.readUInt
import com.example.gnss_app.network.util.toByteArray
import com.example.gnss_app.protocol.Data

sealed class ControlDevice : Data() {

    var response: ByteArray? = null

    val result: Int
        get() = response!![0].toInt()

    class State() : ControlDevice() {
        var _value: Int = 0
        var value: Int
            set(v) {
                _value = if (v > 0) 1 else 0
            }
            get() = body?.get(0)?.toInt() ?: 0

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_value.toByte())
        }
    }

    /**
     *     GNSS模块状态
     *    POWER_OFF =0,
     *    POWER_ON =1,
     *    OFF =2,
     *    ON =3,
     *
     *    控制模块状态暂时只有0 关闭 1 打开
     * */
    class GnssState() : ControlDevice() {
        var _value: Int = 0
        var value: Int
            set(v) {
                _value = if (v > 0) 1 else 0
            }
            get() = body?.get(0)?.toInt() ?: 0

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_value.toByte())
        }
    }

    /**
     * 控制设备与服务器连接的开关
     * 0 关闭 1 打开
     */
    class SocketSwitch() : ControlDevice() {
        var _value: Int = 0
        var value: Int
            set(v) {
                _value = if (v > 0) 1 else 0
            }
            get() = body?.get(0)?.toInt() ?: 0

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_value.toByte())
        }
    }

    class CheckUpgrade() : ControlDevice() {
        val value: Int
            get() = body?.get(0)?.toInt() ?: 0

        override fun toByteArray(): ByteArray? {
            return null
        }
    }

    class ADC() : ControlDevice() {
        var id: Int = 0;
        val value: UInt?
            get() = body?.readUInt()

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(id.toByte())
        }
    }

    class HBTime() : ControlDevice() {
        private var _value: UShort = 0u
        var value: UShort?
            set(value) {
                _value = value ?: 0u
            }
            get() = body?.readInt16()?.toUShort()

        override fun toByteArray(): ByteArray? {
            return _value.toByteArray()
        }
    }

    class DebugSwitch() : ControlDevice() {
        var _value: Int = 0
        var value: Int
            set(v) {
                _value = if (v > 0) 1 else 0
            }
            get() = body?.get(0)?.toInt() ?: 0

        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_value.toByte())
        }
    }

    class PrintDeviceConfig() : ControlDevice() {
        override fun toByteArray(): ByteArray? = null
    }
}