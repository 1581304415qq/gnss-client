package com.example.gnss_app.protocol

import android.util.Log
import com.example.gnss_app.network.util.*
import kotlin.math.pow

@ExperimentalUnsignedTypes
open class Protocol : IProtocol {
    val TAG = "Protocol"
    var tailing: ByteArray = ByteArray(0)
    private var _body: ByteArray = ByteArray(0)

    /*
        当读取时为数据对象转成比特流
        当写入时存到内部比特数组。读取数据对象时， 用内部存储比特数据解析
     */
    var body: ByteArray
        set(value) {
            _body = value
        }
        get() = toByteArray()
    private val head = ProtocolHead(service = 0u, dataLength = 0u)

    fun reset() {
        tailing = ByteArray(0)
        body = ByteArray(0)
        head.clear()
    }

    fun decode(ba: ByteArray): Pair<Int, Frame<ProtocolHead>?> {
        if (parserHead(ba))
            if (parseBody(ba))
                return Pair(
                    head.dataLength.toInt() + HEAD_LENGTH,
                    Frame(head.copy(), _body.clone())
                )
        return Pair(0, null)
    }

    /**
     * 数据封装
     * 数据ð为小端对齐
     */
    open fun encode(service: UShort, data: IData): ByteArray {
        val head = ProtocolHead(service = service.toLB(), dataLength = 0u)
        val bytes = data.toByteArray()
        return if (bytes == null) {
            head.toByteArray()
        } else {
            head.dataLength = bytes.size.toUInt().toLB()
            head.toByteArray() + bytes
        }
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
//        if (head.dataLength.toInt() < 1) {
//            _body= byteArrayOf()
//            return true
//            _errorProtocolDataLength++
//            reset()
//            throw Exception("parse error dataLength < 0")
//        } else
        if ((head.dataLength.toInt() + HEAD_LENGTH + op) <= ba.size) {
            // 提取帧数据
            _body = ba.copyOfRange(
                op + HEAD_LENGTH,
                op + head.dataLength.toInt() + HEAD_LENGTH
            )
            return true
        }
        return false
    }

    private fun parserHead(ba: ByteArray, op: Int = 0): Boolean {
        if (ba.size < (op + HEAD_LENGTH))
            return false
        with(head) {
            service = ba.readUInt16LB(op + 3)
            dataLength = ba.readUIntLB(op + 5)
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
         * 协议号 长度2 byte
         */
        var service: UShort,
        /**
         * 数据长度 长度4 byte
         */
        var dataLength: UInt,

        ) : IProtocol {
        override fun toByteArray(): ByteArray = byteArrayOf(
            (magic / 256u).toByte(), magic.toByte(),
            version.toByte(),
            (service / 256u).toByte(), service.toByte(),
        ) + dataLength.toByteArray()

        fun clear() {
            service = 0u
            dataLength = 0u
        }
    }

    companion object {
        val MAGIC: UShort = 0x7876u
        val VERSION: UByte = 0x01u
        const val HEAD_LENGTH = 9
    }

    override fun toByteArray(): ByteArray {
        TODO("Not yet implemented")
    }

}