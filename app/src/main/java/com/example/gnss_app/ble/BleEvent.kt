package com.example.gnss_app.ble

import android.bluetooth.BluetoothGattService
import java.util.*

enum class BLE_EVENT_TYPE {
    ERROR,
    ON_SCAN_RESULT,
    ON_BATCH_SCAN_RESULTS,
    ON_SCAN_FAILED,
    ON_SERVICES_DISCOVERED,
    ON_CHARACTERISTIC_CHANGED,
    ON_CONNECTION_STATE_CHANGE,
    ON_CHARACTERISTIC_READ,
    ON_SCAN_STOP,
    ON_CONNECT,
    ON_DISCONNECT,
}

sealed class BleEvent<T>(val data: T?, val message: String?) {

    class Error(message: String) : BleEvent<Int>(null, message)
    class ScanFailed(code: Int) : BleEvent<Int>(null, code.toString())
    class ScanResult(data: android.bluetooth.le.ScanResult) :
        BleEvent<android.bluetooth.le.ScanResult>(data, null)

    class BatchScanResults(data: MutableList<android.bluetooth.le.ScanResult>) :
        BleEvent<MutableList<android.bluetooth.le.ScanResult>>(data, null)

    class ServicesDiscovered(data: MutableList<BluetoothGattService>) :
        BleEvent<MutableList<BluetoothGattService>>(data, null)

    class CharacteristicChange(val uuidS: UUID, val uuidC: UUID,data: ByteArray) :
        BleEvent<ByteArray>(data, null)

    class State(val result: Boolean) : BleEvent<Int>(null, null)
    class ConnectionStateChange(val status: Int, val newState: Int) :
        BleEvent<Int>(null, null)

    class CharacteristicRead(data: ByteArray) :
        BleEvent<ByteArray>(data, null)

}