package com.viva.libs.utils

import kotlin.jvm.Throws

class CircleByteBuffer(private val size: Int) {
    private var start = 0;
    private var end = 0
    private val data = ByteArray(size)

    fun getLen(): Int = when {
        start == end -> 0
        start < end -> end - start
        else -> size - start + end
    }

    fun getFree(): Int = size - getLen()

    @Throws
    fun put(e: Byte) {
        data[end] = e
        val pos = end + 1
        end = when (pos) {
            size -> 0
            start -> throw Exception("out buffer")
            else -> pos
        }
    }

    @Throws
    fun get(): Byte {
        if (getLen() == 0)
            throw Exception("out buffer")
        val ret = data[start]
        if (++start == size)
            start = 0
        return ret
    }

    fun get(i: Int): Byte {
        if (getLen() <= i)
            throw Exception("out buffer")
        var pos = start + i
        if (pos >= size)
            pos -= size
        return data[pos]
    }

    fun puts(bts: ByteArray, ind: Int, len: Int) {
        for (i in ind until len)
            put(bts[i])
    }

    fun puts(bts: ByteArray) = puts(bts, 0, bts.size)


    fun gets(len: Int): ByteArray {
        val bts = ByteArray(len)
        for (i in 0 until len)
            bts[i] = get()
        return bts
    }

    fun getAll(): ByteArray = gets(getLen())
    fun clear() {
        start = 0
        end = 0
    }

    /**
     * 查看环形缓存区内数据，但不取出
     */
    @Throws
    fun peek(i: Int): Byte {
        if (getLen() == 0)
            throw Exception("out buffer")
        return data[start + i]
    }

    /**
     * 获取但不删除环形缓存区内数据
     */
    fun peeks(len: Int): ByteArray {
        val bts = ByteArray(len)
        for (i in 0 until len)
            bts[i] = peek(i)
        return bts
    }
}
