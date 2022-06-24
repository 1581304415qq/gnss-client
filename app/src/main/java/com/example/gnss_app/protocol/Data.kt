package com.example.gnss_app.protocol

abstract class Data : IData {
    /**
     * 存储接收的数据bit
     */
    var body: ByteArray? = null
    abstract override fun toByteArray(): ByteArray?
    fun clear() {
        body = ByteArray(0)
    }
}