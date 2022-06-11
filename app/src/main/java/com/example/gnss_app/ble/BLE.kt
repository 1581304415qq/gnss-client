package com.example.gnss_app.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.gnss_app.ble.util.*
import com.viva.libs.event.EventDispatcher
import com.viva.libs.utils.CircleByteBuffer
import java.util.*

/**
 * enable -- scan -- discover -- disable
 */

@SuppressLint("MissingPermission")
object BLE : EventDispatcher<BLE_EVENT_TYPE, BleEvent<*>>() {
    private const val TAG = "BlueTooth"
    private const val SCAN_PERIOD = 2000L
    private var scanning = false
    private var scanner: BluetoothLeScanner? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    var isConnected = false
    var state: STATUS = STATUS.UNABLE
    var results = mutableListOf<ScanResult>()
    var devices = mutableListOf<BluetoothDevice>()
    var bluetoothGatt: BluetoothGatt? = null
    var currentCharacteristic: BluetoothGattCharacteristic? = null

    private fun handleError(errorCode: Int) {
        TODO("Not yet implemented")
    }

    private fun deviceFound(result: ScanResult) {
        val device = result.device
        Log.v(TAG, "onScanResult: ${device.address} - ${device.name} ${result.rssi}")
        if (!devices.contains(device)) {
            results.add(result)
            devices.add(device)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.i("BlueTooth onCharacteristicWrite", "${String(characteristic!!.value)}")
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            with(characteristic!!) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n${value.toHexString()}"
                        )
                        dispatch(
                            BLE_EVENT_TYPE.ON_CHARACTERISTIC_READ,
                            BleEvent.CharacteristicRead(value)
                        )
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic read failed for $uuid, error: $status"
                        )
                    }
                }
            }
        }

        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            gatt.printGattTable()
            //  发现服务后扫描服务 设置每个特征值的处理
            initServices()
            dispatch(
                BLE_EVENT_TYPE.ON_SERVICES_DISCOVERED,
                BleEvent.ServicesDiscovered(gatt.services)
            )
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.v(
                TAG,
                """characteristicChanged 
                    |${characteristic.service.uuid} 
                    |${characteristic.uuid} 
                    |${String(characteristic.value)} len: ${characteristic.value.size}
                    |${characteristic.value.toHexString()}""".trimMargin()
            )
            onCharacteristicChangedHandle(
                characteristic.service.uuid,
                characteristic.uuid,
                characteristic.value
            )
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            log(msg = gatt.device.address)
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(TAG, "Connection State: 1");
                bluetoothGatt = gatt
                isConnected = true
                gatt.requestMtu(128)
                dispatch(
                    BLE_EVENT_TYPE.ON_CONNECT,
                    BleEvent.State(true)
                )
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.v(TAG, "Connection State: 2");
                dispatch(
                    BLE_EVENT_TYPE.ON_DISCONNECT,
                    BleEvent.State(true)
                )
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.v(TAG, "Connection State: 3");
                gatt.disconnect();
                isConnected = false
                dispatch(
                    BLE_EVENT_TYPE.ON_CONNECT,
                    BleEvent.State(false)
                )
            }
            dispatch(
                BLE_EVENT_TYPE.ON_CONNECTION_STATE_CHANGE,
                BleEvent.ConnectionStateChange(status, newState)
            )
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i("onMtuChanged", "$status")
            gatt?.discoverServices()
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            super.onReliableWriteCompleted(gatt, status)

        }
    }

    private val scanCallback by lazy {
        object : ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.let {
                    Log.v(TAG, "onBatchScanResults ${results.size}")
                    dispatch(
                        BLE_EVENT_TYPE.ON_BATCH_SCAN_RESULTS,
                        BleEvent.BatchScanResults(results)
                    )
                }
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    deviceFound(result)
                    dispatch(BLE_EVENT_TYPE.ON_SCAN_RESULT, BleEvent.ScanResult(result))
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "onScanFailed: $errorCode")
                handleError(errorCode)
                dispatch(BLE_EVENT_TYPE.ON_SCAN_FAILED, BleEvent.ScanFailed(errorCode))
            }
        }
    }

    fun getCharacteristic(
        serviceUUID: String,
        characteristicUUID: String
    ): BluetoothGattCharacteristic {
        if (bluetoothGatt == null)
            throw Error("bluetoothGatt is null")
        val service = bluetoothGatt!!.getService(uuid(serviceUUID))
            ?: throw Error("bluetoothService is null")
        return service.getCharacteristic(uuid(characteristicUUID))
            ?: throw Error("bluetoothGatt is null")
    }

    fun startScan(bluetoothAdapter: BluetoothAdapter): Boolean {
        BLE.bluetoothAdapter = bluetoothAdapter
        val scanFilter = ScanFilter.Builder().build()
        val scanFilters = mutableListOf<ScanFilter>()
        scanFilters.add(scanFilter)
        val handler = Handler(Looper.getMainLooper())
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        if (bluetoothAdapter.isEnabled) {
            log(msg = "start scan")
            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (!scanning && scanner != null) {
                handler.postDelayed(
                    {
                        dispatch(BLE_EVENT_TYPE.ON_SCAN_STOP, BleEvent.BatchScanResults(results))
                        stopScan()
                    },
                    SCAN_PERIOD
                )
                BLE.scanner = scanner
                scanning = true
                scanner.startScan(scanFilters, scanSettings, scanCallback)
                log(msg = "Scanning")
            }
            return true
        } else log(msg = "can not scan")
        return false
    }

    fun stopScan() {
        log(msg = "stop scanning")
        scanning = false
        state = STATUS.STOP_SAN
        scanner?.stopScan(scanCallback)
    }

    fun disable() {
        state = STATUS.UNABLE
        bluetoothAdapter?.disable()
    }

    fun uuid(uuid: String): UUID = UUID.fromString(uuid)

    fun connect(context: Context, device: BluetoothDevice) {
        device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    fun read(characteristic: BluetoothGattCharacteristic) {
        Log.i(TAG, "${characteristic.uuid} characteristic readable: ${characteristic.isReadable()}")
        if (characteristic.isReadable()) bluetoothGatt?.readCharacteristic(characteristic)
    }

    fun write(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> Log.v(TAG, "Characteristic ${characteristic.uuid} cannot be written to")
        }
        Log.i(TAG, String(payload))
        bluetoothGatt?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
    }

    fun notification(characteristic: BluetoothGattCharacteristic?, enable: Boolean = true) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, enable);
    }

    fun writeCharacteristic(payload: String) {
        currentCharacteristic?.let { write(currentCharacteristic!!, payload.toByteArray()) }
    }

    fun readCharacteristic() {
        if (currentCharacteristic != null) read(currentCharacteristic!!)
        else {
            Log.v(TAG, "no characteristic")
        }
    }

    private fun initServices() {
        bluetoothGatt?.services?.forEach { service ->
            service.characteristics.forEach {
                if (it.isNotifiable()) notification(it)
            }
        }
    }

    private val buffer = mutableMapOf<UUID, MutableMap<UUID, CircleByteBuffer>>()
    private fun onCharacteristicChangedHandle(uuidS: UUID, uuidC: UUID, value: ByteArray) {
        Log.i(TAG, "val is /r/n ${value.contentEquals("\r\n".toByteArray())}")
        if (!value.contentEquals("\r\n".toByteArray())) {
            if (buffer[uuidS] == null) buffer[uuidS] = mutableMapOf()
            if (buffer[uuidS]?.get(uuidC) == null)
                buffer[uuidS]?.set(
                    uuidC,
                    CircleByteBuffer(100_000)
                )
            buffer[uuidS]!![uuidC]!!.puts(value)
        } else {
            try {
                val data = buffer[uuidS]!![uuidC]!!.getAll()
                Log.i(TAG, "receive bytes: ${String(data, Charsets.UTF_8)}")
                dispatch(
                    BLE_EVENT_TYPE.ON_CHARACTERISTIC_CHANGED,
                    BleEvent.CharacteristicChange(uuidS, uuidC, data)
                )
            } catch (e: Exception) {
                buffer[uuidS]!![uuidC]!!.clear()
                return
            }
        }
    }

    private fun log(tag: String = TAG, msg: String) = Log.v(tag, msg)
}

enum class STATUS {
    ABLE,
    UNABLE,
    STOP_SAN,
    SCANNING,
    DISCOVERED
}