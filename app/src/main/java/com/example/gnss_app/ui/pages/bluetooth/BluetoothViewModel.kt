package com.example.gnss_app.ui.pages.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gnss_app.ble.repository.Repository
import com.example.gnss_app.utils.toastShow
import com.viva.libs.utils.Log
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class BluetoothViewModel(private val context: Context) : ViewModel() {
    val TAG = "BluetoothViewModel"

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val btAdapter: BluetoothAdapter = bluetoothManager.adapter
    val devices = MutableLiveData(listOf<String>())
    val status = mutableStateOf(BLE_CONNECT_STATUS.DISCONNECT)

    fun setStatus(value: BLE_CONNECT_STATUS) {
        if (value != status.value)
            status.value = value
    }

    @SuppressLint("MissingPermission")
    fun bleScan() {
//        setStatus(BLE_CONNECT_STATUS.SCANNING)
        viewModelScope.launch {
            val result = Repository.scan(btAdapter)
            val list = result.map {
//                toastShow(context, TAG, "scan result ${it.device.address}")
                "${it.device.name}\n ${it.device.address}"
            }
            devices.postValue(list)
            toastShow(context, TAG, "scan result ${list.size}")
//            setStatus(BLE_CONNECT_STATUS.SCANNED)
        }
    }

    fun connectDevice(id: Int,callback:(result:Boolean)->Unit) {
        viewModelScope.launch {
            setStatus(BLE_CONNECT_STATUS.CONNECTING)
            val res = Repository.connect(context, id)
            Log.i(TAG, "connect res $res")
            callback(res)
            if (res)
                setStatus(BLE_CONNECT_STATUS.CONNECTED)
            else
                setStatus(BLE_CONNECT_STATUS.DISCONNECT)
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
//            setStatus(BLE_CONNECT_STATUS.CONNECTING)
            val res = Repository.disconnect()
            Log.i(TAG, "connect res $res")
//            if (res)
//                setStatus(BLE_CONNECT_STATUS.DISCONNECT)
        }
    }
}