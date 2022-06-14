package com.example.gnss_app.network

import android.util.Log
import com.example.gnss_app.ble.repository.Repository
import com.example.gnss_app.ble.util.getMin
import com.example.gnss_app.protocol.Protocol.*
import com.example.gnss_app.network.constants.*
import com.example.gnss_app.protocol.Frame
import com.example.gnss_app.protocol.Protocol
import com.viva.libs.event.EventDispatcher
import com.viva.libs.event.IEventDispatcher
import com.viva.libs.net.Net
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalUnsignedTypes
class VivaClient private constructor(
    private val d: IEventDispatcher<VivaEventType, VivaEvent<*>>,
    private val protocol: Protocol
) : Net(if (IS_DEBUG) LOCAL_IP else IP, if (IS_DEBUG) LOCAL_PORT else PORT),
    IEventDispatcher<VivaEventType, VivaEvent<*>> by d {

    init {

    }
    fun reconnect() {
        stop()
        connect()
    }

    fun send(dataI: IVivaData) = send(dataI.toBytes())

    fun send(dataI: IVivaData, type: VivaEventType, callBack: (e: VivaEvent<*>) -> Unit) {
        once(type, callBack)
        send(dataI.toBytes())
    }

    fun reset() {
        bufferIn.clear()
        bufferOut = byteArrayOf()
        protocol.reset()
    }

    private fun heartHandle(frame: Frame<ProtocolHead>) {
        send(frame.toBytes())
    }

    private fun authorize() = send(KEY.toByteArray())

    override fun send(d: ByteArray) {
        if (d.size <= 10_000_000)
            super.send(d)
        else
            throw Exception("data out max size")
    }

    override fun unPackage() {
        var tmp: ByteArray
        while (true) {
            val len = bufferIn.getLen()
            if (len >= Protocol.HEAD_LENGTH) {
                tmp = bufferIn.peeks(getMin(1024, len))
                val (dataLen, frame) = protocol.decode(tmp)
                if (dataLen > 0) {
                    bufferIn.gets(dataLen)
                    frameHandle(frame!!)
                }
            }
        }
    }

    private fun frameHandle(frame: Frame<ProtocolHead>) {
        Log.d(
            "VivaClient",
            "frame body size: ${frame.body.size} service: ${frame.head.service}}"
        )
        when (frame.head.service) {
            VivaServiceID.HEART -> heartHandle(frame)
            VivaServiceID.MESSAGE -> dispatch(
                VivaEventType.Message.RECEIVE,
                VivaEvent.Success(frame)
            )
            VivaServiceID.VIDEO -> dispatch(
                VivaEventType.Video.RECEIVE,
                VivaEvent.Success(frame)
            )
            VivaServiceID.COMMAND -> {}
            VivaServiceID.VIDEO -> {}
            VivaServiceID.FILE -> dispatch(
                VivaEventType.File.RECEIVE,
                VivaEvent.Success(frame)
            )
        }
    }

    override fun onConnectSuccess() {
        Log.d("VivaClient", "onConnectSuccess")
        CoroutineScope(Dispatchers.IO).launch {
            receiveAndSendHandler()
        }
        authorize()
    }

    override fun onConnectFail(e: IOException) {
        super.onConnectFail(e)
        Log.d("VivaClient", "onConnectFail")
    }

    override fun onDisConnect() {
        Log.d("VivaClient", "onDisConnect")
    }

    override fun destroy() {
        TODO("Not yet implemented")
    }

    companion object {
        private var instance: VivaClient? = null
        private var dispatcher: IEventDispatcher<VivaEventType, VivaEvent<*>>? = null

        @ExperimentalUnsignedTypes
        fun getInstance(): VivaClient {
            if (instance == null) {
                dispatcher = EventDispatcher()
                instance = VivaClient(dispatcher!!, Protocol())
            }
            return instance!!
        }

        fun destroy() {
            instance?.destroy()
            dispatcher?.destroy()
            instance = null
            dispatcher = null
        }
    }
}