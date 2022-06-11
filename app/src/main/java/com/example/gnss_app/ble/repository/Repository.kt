package com.example.gnss_app.ble.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context
import com.example.gnss_app.ble.BLE
import com.example.gnss_app.ble.BLE_EVENT_TYPE
import com.example.gnss_app.ble.BleEvent
import com.example.gnss_app.ble.model.Wireless
import com.google.gson.Gson
import com.viva.libs.event.EventDispatcher
import com.viva.libs.utils.Log
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

object Repository : EventDispatcher<EventType, Event<*>>() {
    val TAG = "BleRepository"

    private val COMMAND_WIFI_SCAN = byteArrayOf(0x01)
    private val COMMAND_WIFI_CONNECT = byteArrayOf(0x02)
    private val COMMAND_WIFI_SET_CONFIG = byteArrayOf(0x03)
    private val COMMAND_WIFI_GET_CONFIG = byteArrayOf(0x04)

    private val COMMAND_CAMERA_START = byteArrayOf(0x01)
    private val COMMAND_CAMERA_STOP = byteArrayOf(0x02)
    private val COMMAND_CAMERA_RESET = byteArrayOf(0x03)
    private val COMMAND_CAMERA_CAPTURE = byteArrayOf(0x04)
    private val COMMAND_CAMERA_GET_CONFIG = byteArrayOf(0x05)
    private val COMMAND_CAMERA_SET_CONFIG = byteArrayOf(0x06)
    private val COMMAND_CAMERA_FLASH_OPEN = byteArrayOf(0x07)
    private val COMMAND_CAMERA_FLASH_CLOSE = byteArrayOf(0x08)
    private val COMMAND_CAMERA_LASER_OPEN = byteArrayOf(0x09)
    private val COMMAND_CAMERA_LASER_CLOSE = byteArrayOf(0x0A)
    private val COMMAND_CAMERA_SET_MODE = byteArrayOf(0x0B)


    private val COMMAND_CAMERA_SET_FRAMESIZE = byteArrayOf(0x21)
    private val COMMAND_CAMERA_SET_SPEFFECT = byteArrayOf(0x22)
    private val COMMAND_CAMERA_SET_WHITEBALANCE = byteArrayOf(0x23)
    private val COMMAND_CAMERA_SET_SATURATION = byteArrayOf(0x24)
    private val COMMAND_CAMERA_SET_BRIGHTNESS = byteArrayOf(0x25)
    private val COMMAND_CAMERA_SET_QUALITY = byteArrayOf(0x26)
    private val COMMAND_CAMERA_INTERVAL = byteArrayOf(0x27)
    private val COMMAND_CAMERA_LASERABLE = byteArrayOf(0x28)
    private val COMMAND_CAMERA_FLASHABLE = byteArrayOf(0x29)

    private val SYSTEM_SLEEP_MODE_CHANGE = byteArrayOf(0x41)

    private const val WIFI_SERVICE_UUID = "0000281a-0000-1000-8000-00805f9b34fb"
    private const val WIFI_COMMAND_UUID = "00002A68-0000-1000-8000-00805f9b34fb"
    private const val WIFI_WRITE_UUID = "00002A78-0000-1000-8000-00805f9b34fb"
    private const val WIFI_SCAN_UUID = "00002A08-0000-1000-8000-00805f9b34fb"
    private const val WIFI_CONFIG_UUID = "00002a6e-0000-1000-8000-00805f9b34fb"
    private const val CAMERA_SERVICE_UUID = "0000181a-0000-1000-8000-00805f9b34fb"
    private const val CAMERA_READ_UUID = "00002a6e-0000-1000-8000-00805f9b34fb"
    private const val CAMERA_COMMAND_UUID = "00002a68-0000-1000-8000-00805f9b34fb"

    init {
        BLE.on(BLE_EVENT_TYPE.ON_CHARACTERISTIC_CHANGED, ::characteristicChangeHandle)
    }

    override fun destroy() {
        super.destroy()
        BLE.off(BLE_EVENT_TYPE.ON_CHARACTERISTIC_CHANGED, ::characteristicChangeHandle)
    }

    private fun characteristicChangeHandle(e: BleEvent<*>) {
        e as BleEvent.CharacteristicChange
        if (e.uuidS == BLE.uuid(WIFI_SERVICE_UUID) && BLE.uuid(WIFI_SCAN_UUID) == e.uuidC) {
            val wireless = Gson().fromJson(String(e.data!!, Charsets.UTF_8), Wireless::class.java)
            dispatch(
                EventType.ON_WIFI_SCAN_RESULT,
                Event.WifiScanResult(wireless)
            )
        } else if (e.uuidS == BLE.uuid(WIFI_SERVICE_UUID) && BLE.uuid(WIFI_CONFIG_UUID) == e.uuidC) {
            Log.i(TAG, "get wifi config :${String(e.data!!)}")
        }
    }

    suspend fun scan(bluetoothAdapter: BluetoothAdapter): MutableList<ScanResult> =
        suspendCoroutine {
            BLE.once(BLE_EVENT_TYPE.ON_SCAN_STOP) { event ->
                event as BleEvent.BatchScanResults
                it.resume(event.data!!)
            }
            BLE.startScan(bluetoothAdapter)
        }

    suspend fun configWifi(bytes: ByteArray) {
        try {
            var characteristic = BLE.getCharacteristic(WIFI_SERVICE_UUID, WIFI_COMMAND_UUID)
            BLE.write(characteristic, COMMAND_WIFI_SET_CONFIG)
            characteristic = BLE.getCharacteristic(WIFI_SERVICE_UUID, WIFI_WRITE_UUID)
            Log.i("BleRepository", "$characteristic ${String(bytes)}")
            for (b in bytes.indices step 20) {
                delay(200)
                BLE.write(
                    characteristic,
                    bytes.copyOfRange(b, min(b + 20, bytes.size))
                )
                delay(200)
            }
            BLE.write(characteristic, "\r\n".toByteArray())
        } catch (e: Exception) {

        }
    }

    suspend fun wifiScan(): Wireless = suspendCoroutine {
        try {
            val characteristic = BLE.getCharacteristic(WIFI_SERVICE_UUID, WIFI_COMMAND_UUID)
            Log.i("BleRepository", "$characteristic")
            once(EventType.ON_WIFI_SCAN_RESULT) { e ->
                e as Event.WifiScanResult
                it.resume(e.data!!)
            }
            BLE.write(characteristic, COMMAND_WIFI_SCAN)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun wifiConnect(): Wireless = suspendCoroutine {
        try {
            val characteristic = BLE.getCharacteristic(WIFI_SERVICE_UUID, WIFI_COMMAND_UUID)
            Log.i("BleRepository", "$characteristic")
            BLE.write(characteristic, COMMAND_WIFI_CONNECT)
        } catch (e: Exception) {

        }
    }

    suspend fun connect(context: Context, id: Int): Boolean =
        suspendCoroutine {
            BLE.once(BLE_EVENT_TYPE.ON_CONNECT) { event ->
                val res = event as BleEvent.State
                it.resume(res.result)
            }
            BLE.connect(context, BLE.devices[id])
        }

    suspend fun readConfig(): String =
        suspendCoroutine {
            try {
                BLE.once(BLE_EVENT_TYPE.ON_CHARACTERISTIC_CHANGED) { e ->
                    e as BleEvent.CharacteristicChange
                    it.resume(String(e.data!!))
                }
                val characteristic = BLE.getCharacteristic(WIFI_SERVICE_UUID, WIFI_COMMAND_UUID)
                BLE.write(characteristic, COMMAND_WIFI_GET_CONFIG)
            } catch (e: Exception) {

            }
        }

    fun startCameraService() {
        val characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
        BLE.write(characteristic, COMMAND_CAMERA_START)
    }

    fun stopCameraService() {
        val characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
        BLE.write(characteristic, COMMAND_CAMERA_STOP)
    }

    fun capture() {
        val characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
        BLE.write(characteristic, COMMAND_CAMERA_CAPTURE)
    }

    fun getConfigCamera() {
        val characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
        BLE.write(characteristic, COMMAND_CAMERA_GET_CONFIG)
    }

    suspend fun configCamera(bytes: ByteArray) {
        try {
            var characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
            BLE.write(characteristic, COMMAND_CAMERA_SET_CONFIG)
            characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_READ_UUID)
            Log.i("BleRepository", "$characteristic ${String(bytes)}")
            for (b in bytes.indices step 20) {
                delay(200)
                BLE.write(
                    characteristic,
                    bytes.copyOfRange(b, min(b + 20, bytes.size))
                )
                delay(200)
            }
            BLE.write(characteristic, "\r\n".toByteArray())
        } catch (e: Exception) {

        }
    }

    fun led(state: Boolean) {
        val characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
        if (state)
            BLE.write(characteristic, COMMAND_CAMERA_FLASH_OPEN)
        else
            BLE.write(characteristic, COMMAND_CAMERA_FLASH_CLOSE)
    }


    fun setLaserState(state: Boolean) {
        val characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
        if (state)
            BLE.write(characteristic, COMMAND_CAMERA_LASER_OPEN)
        else
            BLE.write(characteristic, COMMAND_CAMERA_LASER_CLOSE)
    }

    fun setting() {
        val characteristic = BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
        BLE.write(characteristic, COMMAND_CAMERA_SET_CONFIG)
    }

    private suspend fun sendValueToService(service: ByteArray, bytes: ByteArray) {
        Log.i(TAG, "$service $bytes")
        try {
            var characteristic =
                BLE.getCharacteristic(CAMERA_SERVICE_UUID, CAMERA_COMMAND_UUID)
            delay(200)
            BLE.write(characteristic, service)
            characteristic = BLE.getCharacteristic(WIFI_SERVICE_UUID, WIFI_WRITE_UUID)
            delay(200)
            BLE.write(characteristic, bytes)
            delay(200)
            BLE.write(characteristic, "\r\n".toByteArray())
        } catch (e: Exception) {
        }
    }

    suspend fun cameraQuality(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_SET_QUALITY, bytes)
    }

    suspend fun cameraFramesize(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_SET_FRAMESIZE, bytes)
    }

    suspend fun cameraBrightness(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_SET_BRIGHTNESS, bytes)
    }

    suspend fun cameraSetLaser(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_LASERABLE, bytes)
    }

    suspend fun cameraSetFlash(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_FLASHABLE, bytes)
    }

    suspend fun cameraSaturation(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_SET_SATURATION, bytes)
    }

    suspend fun cameraWhitebalance(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_SET_WHITEBALANCE, bytes)
    }

    suspend fun cameraSpeffect(bytes: ByteArray) {
        sendValueToService(COMMAND_CAMERA_SET_SPEFFECT, bytes)
    }

    suspend fun systemSleepModel(bytes: ByteArray) {
        sendValueToService(SYSTEM_SLEEP_MODE_CHANGE, bytes)
    }

}