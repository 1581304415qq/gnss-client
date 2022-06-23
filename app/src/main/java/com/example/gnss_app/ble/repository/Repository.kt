package com.example.gnss_app.ble.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context
import com.example.gnss_app.ble.BLE
import com.example.gnss_app.ble.BLE_EVENT_TYPE
import com.example.gnss_app.ble.BleEvent
import com.example.gnss_app.ble.contance.*
import com.example.gnss_app.ble.model.DeviceConfig
import com.example.gnss_app.ble.model.DeviceInfo.AppInfo
import com.example.gnss_app.ble.model.Heart
import com.example.gnss_app.ble.model.DeviceConfig.Server
import com.example.gnss_app.ble.util.getMin
import com.example.gnss_app.ble.util.toHexString
import com.example.gnss_app.protocol.Frame
import com.example.gnss_app.protocol.IData
import com.example.gnss_app.protocol.Protocol
import com.viva.libs.event.EventDispatcher
import com.viva.libs.utils.CircleByteBuffer
import com.viva.libs.utils.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalUnsignedTypes::class)
object Repository : EventDispatcher<EventType, Event>() {
    val TAG = "BleRepository"

    private const val CASTOR_BT_SERVICE_UUID = "00002000-0000-1000-8000-00805f9b34fb"
    private const val CASTOR_BT_WRITE_UUID = "00002000-0000-1000-8000-00805f9b34fb"
    private const val CASTOR_BT_READ_UUID = "00002001-0000-1000-8000-00805f9b34fb"

    init {
        BLE.on(BLE_EVENT_TYPE.ON_CHARACTERISTIC_CHANGED, ::characteristicChangeHandle)
        BLE.once(BLE_EVENT_TYPE.ON_SERVICES_DISCOVERED) {
            startHeart()
            CoroutineScope(Dispatchers.IO).launch {
                parseDataHandle()
            }
        }
    }

    override fun destroy() {
        super.destroy()
        BLE.off(BLE_EVENT_TYPE.ON_CHARACTERISTIC_CHANGED, ::characteristicChangeHandle)
    }

    /**
     * 蓝牙串口的协议与数据缓冲
     */
    private val protocol = Protocol()
    private var buffer: CircleByteBuffer = CircleByteBuffer(10_240)

    /**
     * 接收BLE数据，存入缓存区带解析
     */
    private fun characteristicChangeHandle(e: BleEvent<*>) {
        e as BleEvent.CharacteristicChange
        //  模拟蓝牙串口数据接收
        //  CASTOR_BT_SERVICE_UUID 服务
        //  CASTOR_BT_READ_UUID 数据接收服务
        if (e.uuidS == BLE.uuid(CASTOR_BT_SERVICE_UUID) && BLE.uuid(CASTOR_BT_READ_UUID) == e.uuidC) {
            buffer.puts(e.data!!)
        }
        // 其他服务数据接收解析
        else if (e.uuidS == BLE.uuid(CASTOR_BT_SERVICE_UUID) && BLE.uuid(CASTOR_BT_WRITE_UUID) == e.uuidC) {
            Log.i(TAG, "get wifi config :${String(e.data!!)}")
        }
    }

    /**
     * 协议解析，再用消息发送出去
     * 连接成功后 用一个线程去启动
     */
    private fun parseDataHandle() {
        var tmp: ByteArray
        while (true) {
            val len = buffer.getLen()
            if (len >= Protocol.HEAD_LENGTH) {
                tmp = buffer.peeks(getMin(1024, len))
                val (dataLen, frame) = protocol.decode(tmp)
                if (dataLen > 0) {
                    Log.v(TAG,"parseDataHandle success $dataLen ${frame!!.body.toHexString()}")
                    buffer.gets(dataLen)
                    dispatchEvent(frame!!)
                }
            }
        }
    }

    private fun dispatchEvent(frame: Frame<Protocol.ProtocolHead>) {
        Log.v(TAG,"dispatchEvent ${frame.head.service}")
        val eventType = when (frame.head.service) {
            ProtocolID.APP_INFO -> EventType.ON_R_APP_INFO
            ProtocolID.SERVICE_R_NETMOD -> EventType.ON_R_NETMOD_CONFIG
            ProtocolID.SERVICE_W_NETMOD -> EventType.ON_W_NETMOD_CONFIG

            ProtocolID.SERVICE_R_SERVER_IP -> EventType.ON_R_SERVER_CONFIG


            else -> EventType.ON_NULL
        }
        dispatch(eventType, Event.Success(frame.body))
    }

    private fun sendMsg(service: UShort, data: IData) {
        val characteristic = BLE.getCharacteristic(CASTOR_BT_SERVICE_UUID, CASTOR_BT_WRITE_UUID)
        BLE.write(
            characteristic,
            protocol.encode(service, data)
        )
    }

    private fun startHeart() {
        val heart = Heart()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                sendMsg(ProtocolID.SERVICE_HEART, heart)
            }
        }, 0, 10_000)
    }

    suspend fun scan(bluetoothAdapter: BluetoothAdapter): MutableList<ScanResult> =
        suspendCoroutine {
            BLE.once(BLE_EVENT_TYPE.ON_SCAN_STOP) { event ->
                event as BleEvent.BatchScanResults
                it.resume(event.data!!)
            }
            BLE.startScan(bluetoothAdapter)
        }

    suspend fun connect(context: Context, id: Int): Boolean =
        suspendCoroutine {
            BLE.once(BLE_EVENT_TYPE.ON_CONNECT) { event ->
                when (event) {
                    is BleEvent.Success -> {
                        it.resume(true)
                    }
                    is BleEvent.Error -> {
                        it.resume(false)
                    }
                    else -> {}
                }
            }

            BLE.connect(context, BLE.devices[id].device)
        }


    suspend fun disconnect(): Boolean =
        suspendCoroutine {
            BLE.once(BLE_EVENT_TYPE.ON_DISCONNECT) { e ->
                when (e) {
                    is BleEvent.Success -> it.resume(true)
                    is BleEvent.Error -> {}
                    else -> {}
                }
            }
            BLE.disconnect()
        }

    suspend fun readAppInfo(appInfo: AppInfo): AppInfo = suspendCoroutine {
        try {
            once(EventType.ON_R_APP_INFO) { e ->
                when (e) {
                    is Event.Success -> {
                        appInfo.body = e.data!!
                        it.resume(appInfo)
                    }
                    is Event.Error -> {

                    }
                }
            }
            sendMsg(ProtocolID.APP_INFO, appInfo)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun readNetMode(data: DeviceConfig.NetMode): DeviceConfig.NetMode =
        suspendCoroutine {
            try {
                once(EventType.ON_R_NETMOD_CONFIG) { e ->
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                }
                sendMsg(ProtocolID.SERVICE_R_NETMOD, data)
            } catch (e: Exception) {
            }
        }

    suspend fun writeNetMode(data: DeviceConfig.NetMode): DeviceConfig.NetMode =
        suspendCoroutine {
            try {
                once(EventType.ON_W_NETMOD_CONFIG) { e ->
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                }
                sendMsg(ProtocolID.SERVICE_W_NETMOD, data)
            } catch (e: Exception) {

            }
        }

    suspend fun readServerConfig(data: Server): Server =
        suspendCoroutine {
            try {
                once(EventType.ON_R_SERVER_CONFIG) { e ->
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                }
                sendMsg(ProtocolID.SERVICE_R_SERVER_IP, data)
            } catch (e: Exception) {
            }
        }

    suspend fun writeServerConfig(data: Server): Server =
        suspendCoroutine {
            try {
                once(EventType.ON_W_SERVER_CONFIG) { e ->
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                }
                sendMsg(ProtocolID.SERVICE_W_SERVER_IP, data)
            } catch (e: Exception) {

            }
        }

    suspend fun readConfig(castorConfig: IData): String =
        suspendCoroutine {
            try {
                once(EventType.ON_R_CONFIG) { e ->
                    when (e) {
                        is Event.Success -> {}
                        is Event.Error -> {}
                    }
                    it.resume(String(e.data!!))
                }
                sendMsg(ProtocolID.SERVICE_READ_CONFIG, castorConfig)
            } catch (e: Exception) {

            }
        }
}