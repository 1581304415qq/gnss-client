package com.example.gnss_app.ui.pages.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gnss_app.ble.repository.Repository
import com.viva.libs.utils.Log
import kotlinx.coroutines.launch

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = "BluetoothViewModel"

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager: BluetoothManager =
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var btAdapter: BluetoothAdapter = bluetoothManager.adapter
    val devices = MutableLiveData<List<String>>()

    fun bleScan() {
        viewModelScope.launch {
            val result = Repository.scan(btAdapter)
            val list = result.map { it.device.address }
            devices.postValue(list)
        }
    }

    fun connectDevice(id: Int) {
        viewModelScope.launch {
            val res = Repository.connect(context, id)
            Log.i(TAG, "connect res $res")
        }
    }
}