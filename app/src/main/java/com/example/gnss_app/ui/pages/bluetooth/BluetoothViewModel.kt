package com.example.gnss_app.ui.pages.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
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
    val devices: MutableLiveData<List<String>> = MutableLiveData()

    fun bleScan() {
        viewModelScope.launch {
            val result = Repository.scan(btAdapter)
            val list = result.map { it.device.address }
            devices.postValue(list)
            toastShow(context, TAG, "scan result ${list.size}")
        }
    }

    fun connectDevice(id: Int) {
        viewModelScope.launch {
            val res = Repository.connect(context, id)
            Log.i(TAG, "connect res $res")
        }
    }
}