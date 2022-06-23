package com.example.gnss_app.network.util

import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object Util {
    fun thread(
        start: Boolean = true,
        isDaemon: Boolean = false,
        contextClassLoader: ClassLoader? = null,
        name: String? = null,
        priority: Int = -1,
        block: () -> Unit
    ): Thread {
        val thread = object : Thread() {
            override fun run() {
                block()
            }
        }
        if (isDaemon) thread.isDaemon = true
        if (priority > 0) thread.priority = priority
        if (name != null) thread.name = name
        if (contextClassLoader != null) thread.contextClassLoader = contextClassLoader
        if (start) thread.start()
        return thread
    }

    fun Log.v(tag: String, msg: String) = Log.v(tag, msg)

}

fun ByteArray.findFirst(sequence: ByteArray, startFrom: Int = 0): Int {
    if (sequence.isEmpty()) throw IllegalArgumentException("non-empty byte sequence is required")
    if (startFrom < 0) throw IllegalArgumentException("startFrom must be non-negative")
    var matchOffset = 0
    var start = startFrom
    var offset = startFrom
    while (offset < size) {
        if (this[offset] == sequence[matchOffset]) {
            if (matchOffset++ == 0) start = offset
            if (matchOffset == sequence.size) return start
        } else
            matchOffset = 0
        offset++
    }
    return -1
}

fun UByteArray.readInt8(startFrom: Int = 0): UInt {
    return this[startFrom].toUInt()
}

fun ByteArray.readInt16(startFrom: Int = 0): UInt {
    return this[startFrom].toUInt().shl(8) or this[startFrom + 1].toUInt()
}

fun ByteArray.readInt16LB(startFrom: Int = 0): UShort {
    return (this[startFrom].toUInt() or this[startFrom + 1].toUInt().shl(8)).toUShort()
}

fun UByteArray.readUInt(startFrom: Int = 0): UInt {
    var rs = 0u
    for (i in startFrom..startFrom + 3) {
        rs = rs shl 8 or this[i].toUInt()
    }
    return rs
}

fun ByteArray.readInt8(startFrom: Int = 0): UInt {
    return this[startFrom].toUInt()
}

fun ByteArray.readUInt(startFrom: Int = 0): UInt {
    var rs = 0u
    for (i in startFrom..startFrom + 3) {
        rs = rs shl 8 or this[i].toUInt()
    }
    return rs
}

fun ByteArray.readUIntLB(startFrom: Int = 0): UInt {
    var rs = 0u
    for (i in startFrom..startFrom + 3) {
        rs = rs or this[i].toUInt().shl((i - startFrom) * 8)
    }
    return rs
}

fun UShort.toLB(): UShort = ((this.toInt() and 0x00FF).shl(8) or this.toInt().shr(8)).toUShort()
fun UInt.toLB(): UInt =
    this.shl(8 * 3) or (this and 65280u).shl(8) or (this and 16711680u).shr(8) or this.shr(8 * 3)

fun UInt.toByteArray(): ByteArray {
    val ar = ByteArray(4)
    val offset: UInt = 255u
    for (s in 0..24 step 8) {
        ar[3 - s / 8] = (this.shr(s) and offset).toByte()
    }
    return ar
}
fun Int.toByteArray(): ByteArray {
    val ar = ByteArray(4)
    val offset: Int = 255
    for (s in 0..24 step 8) {
        ar[3 - s / 8] = (this.shr(s) and offset).toByte()
    }
    return ar
}

fun UShort.toByteArray(): ByteArray {
    return byteArrayOf((this / 256u).toByte(), this.toByte())
}

@Throws(IOException::class)
fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}