package com.example.gnss_app.ble.model

import com.example.gnss_app.protocol.Data
import com.example.gnss_app.utils.readInt8
import com.example.gnss_app.utils.toByteArray


sealed class Server : Data() {
    protected var _id: Int = 0
    var id: Int
        get() = body?.readInt8()?.toInt() ?: 0
        set(value) {
            _id = value
        }
    var ip: Int = 0
    var port: Int = 0

    class Read(value: Int = 0) : Server() {
        init {
            id = value
        }
        override fun toByteArray(): ByteArray? {
            return byteArrayOf(_id.toByte())
        }

    }

    class Write : Server() {
        override fun toByteArray(): ByteArray? {
            return byteArrayOf(id.toByte()) + ip.toByteArray() + byteArrayOf(
                ((port and 0x0F) shl 8).toByte(),
                (port shr 8 and 0x0F).toByte()
            )
        }

    }
}