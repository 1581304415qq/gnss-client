package com.example.gnss_app.protocol

abstract class Data : IData {
    private var _body: ByteArray = ByteArray(0)

    /*
        当读取时为数据对象转成比特流
        当写入时存到内部比特数组。读取数据对象时， 用内部存储比特数据解析
     */
    var body: ByteArray?
        set(value) {
            _body = value!!
        }
        get() = toByteArray()

    abstract override fun toByteArray(): ByteArray?
    fun clear() {
        _body = ByteArray(0)
    }
}