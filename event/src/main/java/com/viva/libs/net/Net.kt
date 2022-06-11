package com.viva.libs.net

import com.viva.libs.utils.CircleByteBuffer
import com.viva.libs.utils.Log
import java.io.*
import java.net.Socket

abstract class Net(private val ip: String, private val port: Int) {
    var isConnect: Boolean = false
        private set
    private var socket: Socket? = null
    private var din: InputStream? = null
    private var dot: OutputStream? = null
    protected var bufferOut: ByteArray? = null
    protected var bufferIn = CircleByteBuffer(BUFFER_SIZE)

    open fun connect() {
        try {
            socket = Socket(ip, port)
            socket!!.soTimeout = 20000  //设置连接超时限制
            din = socket!!.getInputStream()
            dot = socket!!.getOutputStream()
            isConnect = true
            Log.v("Net", "connect server successful")
            onConnectSuccess()
            while (isConnect) {
                receive()
                write()
            }
            stop()
            onDisConnect()
        } catch (e: IOException) {
            onConnectFail(e)
        } finally {

        }
    }

    open fun stop() {
        isConnect = false
        bufferIn.clear()
        bufferOut = null
        close()
    }

    protected open fun send(d: ByteArray) {
        bufferOut = if (bufferOut == null) d else bufferOut!! + d
    }

    private fun write() {
        try {
            if (isConnect && bufferOut != null) {
                dot!!.write(bufferOut!!)
                dot!!.flush()
                bufferOut = null
            }
            if (!isConnect) Log.v("Net", "no connect to send message")

        } catch (e: IOException) {
            Log.v("Net", "send message to client failed")
            error(e)
            stop()
        }
    }

    private fun receive() {
        if (din!!.available() > 0)
            try {
                val bytesBuff = ByteArray(10240)
                val a = din?.read(bytesBuff) ?: -1
                if (a > -1) {
                    bufferIn.puts(bytesBuff, 0, a)
                    Log.v(
                        "Net watch ",
                        """receive data length: $a
                    | bufferIn state 
                    | data len: ${bufferIn.getLen()} 
                    | data free: ${bufferIn.getFree()}
                    | """.trimMargin()
                    )
                    dataHandle()
                }
            } catch (e: IOException) {
                Log.v("Net", "接收错误")
                error(e)
                stop()
            }
    }

    private fun dataHandle() {
        unPackage()
        frameHandle()
    }

    private fun close() {
        din?.close()
        dot?.close()
        socket?.close()
    }

    protected open fun onConnectFail(e: IOException) {
        //throw e
        Log.e("NetWork Error ", e.toString())
    }

    protected abstract fun unPackage()
    protected abstract fun frameHandle()
    protected abstract fun onConnectSuccess()
    protected abstract fun onDisConnect()

    companion object {
        const val BUFFER_SIZE = 3_000_000
    }
}