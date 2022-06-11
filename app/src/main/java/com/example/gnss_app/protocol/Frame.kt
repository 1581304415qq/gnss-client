package com.example.gnss_app.protocol

@ExperimentalUnsignedTypes
data class Frame<T: IProtocolHead>(
    var head: T, //协议头
    var body: ByteArray, //协议体
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Frame<*>

        if (head != other.head) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = head.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }

    fun toBytes(): ByteArray {
        return head.toByteArray() + body
    }
}