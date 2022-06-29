package com.example.gnss_app.ble.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import com.example.gnss_app.ble.BLE
import com.example.gnss_app.ble.BLE_EVENT_TYPE
import com.example.gnss_app.ble.BleEvent
import com.example.gnss_app.ble.contance.*
import com.example.gnss_app.ble.model.ControlDevice.*
import com.example.gnss_app.ble.model.DeviceConfig.*
import com.example.gnss_app.ble.model.DeviceInfo.*
import com.example.gnss_app.ble.model.Heart
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
                try {
                    parseDataHandle()
                } catch (e: Exception) {
                }
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
                    Log.v(TAG, "parseDataHandle success $dataLen ${frame!!.body.toHexString()}")
                    buffer.gets(dataLen)
                    dispatchEvent(frame!!)
                }
            }
        }
    }

    private fun sendMsg(service: UShort, data: IData) {
        val characteristic: BluetoothGattCharacteristic? =
            BLE.getCharacteristic(CASTOR_BT_SERVICE_UUID, CASTOR_BT_WRITE_UUID)
        if (characteristic != null)
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
        }, 10_000, 30_000)
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
                try {
                    when (event) {
                        is BleEvent.Success -> {
                            it.resume(true)
                        }
                        is BleEvent.Error -> {
                            it.resume(false)
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
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

        once(EventType.ON_R_APP_INFO) { e ->
            try {
                when (e) {
                    is Event.Success -> {
                        appInfo.body = e.data!!
                        it.resume(appInfo)
                    }
                    is Event.Error -> {

                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
        sendMsg(ProtocolID.APP_INFO, appInfo)
    }

    suspend fun readNetMode(data: NetMode): NetMode =
        suspendCoroutine {
            once(EventType.ON_R_NETMOD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_NETMOD, data)
        }

    suspend fun writeNetMode(data: NetMode): NetMode =
        suspendCoroutine {
            once(EventType.ON_W_NETMOD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {

                }
            }
            sendMsg(ProtocolID.SERVICE_W_NETMOD, data)
        }

    suspend fun readIPMode(data: Mode): Mode =
        suspendCoroutine {
            once(EventType.ON_R_IPMOD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_IPMOD, data)
        }

    suspend fun writeIPMode(data: Mode): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_IPMOD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_W_IPMOD, data)
        }

    suspend fun readTel(data: BaseStringData): BaseStringData =
        suspendCoroutine {
            once(EventType.ON_R_TEL_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data!!
                            it.resume(data)
                        }
                        is Event.Error -> {
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            sendMsg(ProtocolID.SERVICE_R_TEL, data)

        }

    suspend fun writeTel(data: BaseStringData): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_TEL_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data!!
                            it.resume(data.result > 0)
                        }
                        is Event.Error -> {
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            sendMsg(ProtocolID.SERVICE_W_TEL, data)

        }

    suspend fun readSysPasswd(data: BaseStringData): BaseStringData =
        suspendCoroutine {
            once(EventType.ON_R_SYSPWD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data!!
                            it.resume(data)
                        }
                        is Event.Error -> {
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            sendMsg(ProtocolID.SERVICE_R_SYSPWD, data)

        }

    suspend fun writeSysPasswd(data: BaseStringData): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_SYSPWD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data!!
                            it.resume(data.result > 0)
                        }
                        is Event.Error -> {
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            sendMsg(ProtocolID.SERVICE_W_SYSPWD, data)

        }

    suspend fun readToken(data: BaseStringData): BaseStringData =
        suspendCoroutine {
            once(EventType.ON_R_TOKEN_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data!!
                            it.resume(data)
                        }
                        is Event.Error -> {
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            sendMsg(ProtocolID.SERVICE_R_TOKEN, data)

        }

    suspend fun writeToken(data: BaseStringData): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_TOKEN_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data!!
                            it.resume(data.result > 0)
                        }
                        is Event.Error -> {
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            sendMsg(ProtocolID.SERVICE_W_TOKEN, data)

        }


    suspend fun readWorkMode(data: Mode): Mode =
        suspendCoroutine {
            try {
                once(EventType.ON_R_WKMODE_CONFIG) { e ->
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                }
                sendMsg(ProtocolID.SERVICE_R_WKMODE, data)
            } catch (e: Exception) {
            }
        }

    suspend fun writeWorkMode(data: NetMode): NetMode =
        suspendCoroutine {
            once(EventType.ON_W_WKMODE_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {

                }
            }
            sendMsg(ProtocolID.SERVICE_W_WKMODE, data)
        }

    suspend fun readServerConfig(data: HostAddress): HostAddress =
        suspendCoroutine {
            once(EventType.ON_R_SERVER_IP_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = if (e.data!!.size < 2) null else e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_SERVER_IP, data)
        }

    suspend fun writeServerConfig(data: HostAddress): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_SERVER_IP_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {

                }
            }
            sendMsg(ProtocolID.SERVICE_W_SERVER_IP, data)
        }

    suspend fun writeServerState(data: SocketSwitch): Boolean =
        suspendCoroutine {
            once(EventType.ON_SOCKET_SWITCH) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_SOCKET_SWITCH, data)
        }

    /**
     * 配置ntrip服务
     * 包括 服务器ip port mount account password
     */
    suspend fun writeNtripIP(data: NtripServer): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_NTRIP_IP_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.server.response = e.data
                            it.resume(data.server.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_W_NTRIP_IP, data.server)
        }

    suspend fun writeNtripMount(data: NtripServer): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_NTRIP_MOUNT_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.mount.response = e.data
                            it.resume(data.mount.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_W_NTRIP_MOUNT, data.mount)
        }

    suspend fun writeNtripAccount(data: NtripServer): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_NTRIP_ACCONT_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.account.response = e.data
                            it.resume(data.account.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_W_NTRIP_ACCONT, data.account)
        }

    suspend fun writeNtripPassword(data: NtripServer): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_NTRIP_PASSWD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.password.response = e.data
                            it.resume(data.password.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_W_NTRIP_PASSWD, data.password)
        }

    suspend fun readNtripIP(data: NtripServer): NtripServer =
        suspendCoroutine {
            once(EventType.ON_R_NTRIP_IP_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.server.body = if (e.data!!.size < 2) null else e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_NTRIP_IP, data.server)
        }

    suspend fun readNtripMount(data: NtripServer): NtripServer =
        suspendCoroutine {
            once(EventType.ON_R_NTRIP_MOUNT_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.mount.body = if (e.data!!.size < 2) null else e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_NTRIP_MOUNT, data.mount)

        }

    suspend fun readNtripAccount(data: NtripServer): NtripServer =
        suspendCoroutine {
            once(EventType.ON_R_NTRIP_ACCONT_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.account.body = if (e.data!!.size < 2) null else e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_NTRIP_ACCONT, data.account)
        }

    suspend fun readNtripPassword(data: NtripServer): NtripServer =
        suspendCoroutine {
            once(EventType.ON_R_NTRIP_PASSWD_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.password.body = if (e.data!!.size < 2) null else e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_NTRIP_PASSWD, data.password)
        }


    suspend fun writeNtripConfig(data: NtripServer): Boolean =
        suspendCoroutine {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val result =
                        writeNtripIP(data) and writeNtripMount(data) and writeNtripAccount(data) and writeNtripPassword(
                            data
                        )
                    it.resume(result)
                }
            } catch (e: Exception) {
            }
        }

    suspend fun readNtripConfig(data: NtripServer): NtripServer =
        suspendCoroutine {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    readNtripIP(data)
                    readNtripMount(data)
                    readNtripAccount(data)
                    readNtripPassword(data)
                    it.resume(data)
                }
            } catch (e: Exception) {
            }
        }


    suspend fun readGnssState(data: GnssState): GnssState =
        suspendCoroutine {
            once(EventType.ON_R_GNSS_STATE_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {

                }
            }
            sendMsg(ProtocolID.SERVICE_R_GNSS_STATE, data)
        }

    suspend fun writeGnssState(data: GnssState): GnssState =
        suspendCoroutine {
            once(EventType.ON_W_GNSS_STATE_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_W_GNSS_STATE, data)
        }

    suspend fun writeNtripState(data: State): State =
        suspendCoroutine {
            try {
                once(EventType.ON_NTRIP_SWITCH) { e ->
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                }
                sendMsg(ProtocolID.SERVICE_NTRIP_SWITCH, data)
            } catch (e: Exception) {
            }
        }

    suspend fun writeBTState(data: State): State =
        suspendCoroutine {
            once(EventType.ON_BT_SWITCH) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_BT, data)
        }

    suspend fun readHbTime(data: HBTime): HBTime =
        suspendCoroutine {
            once(EventType.ON_R_HBTIME_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.body = e.data
                            it.resume(data)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_R_HBTIME, data)
        }

    suspend fun writeHbTime(data: HBTime): Boolean =
        suspendCoroutine {
            once(EventType.ON_W_HBTIME_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            data.response = e.data
                            it.resume(data.result > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_W_HBTIME, data)
        }

    suspend fun readConfig(castorConfig: IData): String =
        suspendCoroutine {
            once(EventType.ON_R_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {}
                        is Event.Error -> {}
                    }
                    it.resume(String(e.data!!))
                } catch (e: Exception) {
                }

            }
            sendMsg(ProtocolID.SERVICE_READ_CONFIG, castorConfig)
        }

    suspend fun readADCValue(adcValue: ADCValue): ADCValue =
        suspendCoroutine {
            once(EventType.ON_ADC) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            // 提交错误，无返回
                            if (e.data!!.isEmpty())
                                adcValue.body = null
                            else
                                adcValue.body = e.data
                            it.resume(adcValue)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {
                }
            }
            sendMsg(ProtocolID.SERVICE_ADC, adcValue)
        }

    suspend fun saveConfig(): Boolean =
        suspendCoroutine {
            once(EventType.ON_SAVE_CONFIG) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            it.resume(e.data!![0] > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {

                }
            }
            sendMsg(ProtocolID.SERVICE_SAVE_CONFIG, BaseStringData())
        }

    suspend fun openDebug(data: State): Boolean =
        suspendCoroutine {
            once(EventType.ON_DEBUG_UART_OUT_ENABLE) { e ->
                try {
                    when (e) {
                        is Event.Success -> {
                            it.resume(e.data!![0] > 0)
                        }
                        is Event.Error -> {}
                    }
                } catch (e: Exception) {

                }
            }
            sendMsg(ProtocolID.SERVICE_DEBUG_UART_OUT_ENABLE, data)
        }

    private fun dispatchEvent(frame: Frame<Protocol.ProtocolHead>) {
        Log.v(TAG, "dispatchEvent ${frame.head.service}")
        val eventType = when (frame.head.service) {
            ProtocolID.APP_INFO -> EventType.ON_R_APP_INFO
            ProtocolID.SERVICE_R_WKMODE -> EventType.ON_R_WKMODE_CONFIG
            ProtocolID.SERVICE_W_WKMODE -> EventType.ON_W_WKMODE_CONFIG
            ProtocolID.SERVICE_R_NETMOD -> EventType.ON_R_NETMOD_CONFIG
            ProtocolID.SERVICE_W_NETMOD -> EventType.ON_W_NETMOD_CONFIG
            ProtocolID.SERVICE_R_IPMOD -> EventType.ON_R_IPMOD_CONFIG
            ProtocolID.SERVICE_W_IPMOD -> EventType.ON_W_IPMOD_CONFIG
            ProtocolID.SERVICE_R_DBGMOD -> EventType.ON_R_DBGMOD_CONFIG
            ProtocolID.SERVICE_W_DBGMOD -> EventType.ON_W_DBGMOD_CONFIG
            ProtocolID.SERVICE_R_TEL -> EventType.ON_R_TEL_CONFIG
            ProtocolID.SERVICE_W_TEL -> EventType.ON_W_TEL_CONFIG
            ProtocolID.SERVICE_R_UART -> EventType.ON_R_UART_CONFIG
            ProtocolID.SERVICE_W_UART -> EventType.ON_W_UART_CONFIG
            ProtocolID.SERVICE_R_SYSPWD -> EventType.ON_R_SYSPWD_CONFIG
            ProtocolID.SERVICE_W_SYSPWD -> EventType.ON_W_SYSPWD_CONFIG
            ProtocolID.SERVICE_R_TOKEN -> EventType.ON_R_TOKEN_CONFIG
            ProtocolID.SERVICE_W_TOKEN -> EventType.ON_W_TOKEN_CONFIG

            ProtocolID.SERVICE_R_GNSS_STATE -> EventType.ON_R_GNSS_STATE_CONFIG
            ProtocolID.SERVICE_W_GNSS_STATE -> EventType.ON_W_GNSS_STATE_CONFIG
            ProtocolID.SERVICE_R_SERVER_IP -> EventType.ON_R_SERVER_IP_CONFIG
            ProtocolID.SERVICE_W_SERVER_IP -> EventType.ON_W_SERVER_IP_CONFIG


            ProtocolID.SERVICE_R_NTRIP_IP -> EventType.ON_R_NTRIP_IP_CONFIG
            ProtocolID.SERVICE_W_NTRIP_IP -> EventType.ON_W_NTRIP_IP_CONFIG
            ProtocolID.SERVICE_R_NTRIP_MOUNT -> EventType.ON_R_NTRIP_MOUNT_CONFIG
            ProtocolID.SERVICE_W_NTRIP_MOUNT -> EventType.ON_W_NTRIP_MOUNT_CONFIG
            ProtocolID.SERVICE_R_NTRIP_ACCONT -> EventType.ON_R_NTRIP_ACCONT_CONFIG
            ProtocolID.SERVICE_W_NTRIP_ACCONT -> EventType.ON_W_NTRIP_ACCONT_CONFIG
            ProtocolID.SERVICE_R_NTRIP_PASSWD -> EventType.ON_R_NTRIP_PASSWD_CONFIG
            ProtocolID.SERVICE_W_NTRIP_PASSWD -> EventType.ON_W_NTRIP_PASSWD_CONFIG

            ProtocolID.SERVICE_SAVE_CONFIG -> EventType.ON_SAVE_CONFIG
            ProtocolID.SERVICE_SOCKET_SWITCH -> EventType.ON_SOCKET_SWITCH
            ProtocolID.SERVICE_NTRIP_SWITCH -> EventType.ON_NTRIP_SWITCH
            ProtocolID.SERVICE_DEBUG_UART_OUT_ENABLE -> EventType.ON_DEBUG_UART_OUT_ENABLE
            ProtocolID.SERVICE_NTRIP_STATE -> EventType.ON_NTRIP_STATE
            ProtocolID.SERVICE_ADC -> EventType.ON_ADC

            else -> EventType.ON_NULL
        }
        dispatch(eventType, Event.Success(frame.body))
    }

}