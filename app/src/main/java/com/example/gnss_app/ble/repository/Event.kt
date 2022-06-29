package com.example.gnss_app.ble.repository


sealed class Event(val data: ByteArray?, val message: String?) {
    /**
     * 成功返回 data不为空。如果没有携带数据 为size=0 的ByteArray
     */
    class Success(data: ByteArray) : Event(data, null)

    /**
     * 发生错误
     */
    class Error(message: String) : Event(null, message)
}