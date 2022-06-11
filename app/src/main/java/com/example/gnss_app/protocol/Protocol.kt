package com.example.gnss_app.protocol

import android.util.Log
import com.example.gnss_app.network.util.readInt
import com.example.gnss_app.network.util.readInt16
import com.example.gnss_app.network.util.toByteArray
import kotlin.math.pow

@ExperimentalUnsignedTypes
open class Protocol {
    var tailing: ByteArray = ByteArray(0)
    var body: ByteArray = ByteArray(0)
    val head = ProtocolHead(service = 0u, dataLength = 0u)

    fun reset() {
        tailing = ByteArray(0)
        body = ByteArray(0)
        head.clear()
    }

    /**
     * 返回剩余数据是否还可以解析
     */
    val frames = mutableListOf<Frame<ProtocolHead>>()
    fun decode(ba: ByteArray): MutableList<Frame<ProtocolHead>> {
        frames.clear()
        tailing = ba
        var frame: Frame<ProtocolHead>? = decodeGetSingleFrame(tailing)
        while (frame != null) {
            frames.add(frame)
            frame = decodeGetSingleFrame(tailing)
        }
        return frames
    }
    open fun encode(): ByteArray {
        if (body.isNotEmpty()) {
            if (body.size > MaxDataLength) throw Error("data size is beyond max")
            head.dataLength = body.size.toUInt()
            return head.toByteArray() + body
        }
        return head.toByteArray()
    }
    fun decodeGetSingleFrame(ba: ByteArray): Frame<ProtocolHead>? {
        var frame: Frame<ProtocolHead>? = null
        watch(ba)
        val op = 0
        if (op == -1)
            reset()
        if (parserHead(ba, op)) {
            tailing = if (parseBody(ba, op)) {
                frame = Frame(head.copy(), body.clone())
                ba.copyOfRange(op + head.dataLength.toInt() + head.getSize(), ba.size)
            } else ba
        }
        return frame
    }

    private var _errorBufferDataLength = 0
    private fun watch(ba: ByteArray) {
        Log.v(
            "Protocol watch ",
            """decode state :
                |ba size: ${ba.size} 
                |ba len is out: ${ba.size > Int.MAX_VALUE}
                |head data length: ${head.dataLength}
                |head service: ${head.service}""".trimMargin()
        )
    }

    private fun watchHead(head: ProtocolHead) {
        Log.v(
            "Protocol watch ",
            """head state :
                |head data length: ${head.dataLength}
                |head service: ${head.service}""".trimMargin()
        )
        if (head.dataLength > Int.MAX_VALUE.toUInt())
            throw Exception("head data length out Int Max Value")
    }

    private var _errorProtocolDataLength = 0
    private fun parseBody(ba: ByteArray, op: Int = 0): Boolean {
        if (head.dataLength.toInt() < 1) {
            _errorProtocolDataLength++
            reset()
            throw Exception("parse error dataLength < 0")
        } else if ((head.dataLength.toInt() + head.getSize() + op) <= ba.size) {
            // 提取帧数据
            body = ba.copyOfRange(
                op + head.getSize(),
                op + head.dataLength.toInt() + head.getSize()
            )
            return true
        }
        return false
    }

    private fun parserHead(ba: ByteArray, op: Int = 0): Boolean {
        if (ba.size < (op + head.getSize()))
            return false
        val uba = ba.toUByteArray()
        with(head) {
            service = uba.readInt16(op + 3).toUShort()
            dataLength = uba.readInt(op + 7)
        }
        watchHead(head)
        return true
    }

    private val MaxDataLength = 2.0.pow(32.0) - 1 //UInt.MAX_VALUE

    data class ProtocolHead(
        /**
         * 协议魔数 长度2 byte
         */
        val magic: UShort = MAGIC,
        /**
         * 协议版本号 长度1 byte
         */
        val version: UByte = VERSION,
        /**
         * 协议复用的服务号，用于标识协议中的不同服务，比如向服务器获取get 设置set 添加add ... 都是不同服务（由我们指定）长度2 byte
         */
        var service: UShort,
        /**
         * 数据长度 长度4 byte
         */
        var dataLength: UInt,

        ): IProtocolHead {
        override fun toByteArray(): ByteArray = byteArrayOf(
            (magic / 256u).toByte(), magic.toByte(),
            version.toByte(),
            (service / 256u).toByte(), service.toByte(),
        ) + dataLength.toByteArray()

        fun clear() {
            service = 0u
            dataLength = 0u
        }

        fun getSize() = 11
    }

    companion object {
        val MAGIC: UShort = 0xFF00u
        val VERSION: UByte = 0x01u
    }

}