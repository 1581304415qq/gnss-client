package com.example.gnss_app.ui.pages.setting

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context.BLUETOOTH_SERVICE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gnss_app.ble.repository.Repository
import com.viva.libs.utils.Log
import kotlinx.coroutines.launch

class SettingViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = "SettingViewModel"

    val ssid: MutableLiveData<String> by lazy { MutableLiveData("") }
    val password: MutableLiveData<String> by lazy { MutableLiveData("") }
    val wifiScanResult = mutableListOf<String>()



    fun performConfigWifi() {
        viewModelScope.launch {
            Log.i("ConfigViewModel", "ssid: ${ssid.value} password: ${password.value}")
            if (ssid.value != null && password.value != null
                && ssid.value != "" && password.value != ""
            ) {
                Repository.configWifi("{\"ssid\":\"${ssid.value}\",\"password\":\"${password.value}\"}".encodeToByteArray())
            }
        }
    }

    fun performWifiScan(callback: () -> Unit) {
        viewModelScope.launch {
            val result = Repository.wifiScan()
            val list = result.map { it.ssid }
            wifiScanResult.clear()
            wifiScanResult.addAll(list)
            callback()
        }
    }

    fun performWifiConnect() {
        viewModelScope.launch {
            Repository.wifiConnect()
        }
    }

    fun performGetConfig() {
        viewModelScope.launch {
            val res = Repository.readConfig()
            Log.i(TAG, res)
        }
    }


    fun performStartCameraService() {
        viewModelScope.launch {
            Repository.startCameraService()
        }
    }

    fun performStopCameraService() {
        viewModelScope.launch {
            Repository.stopCameraService()
        }
    }

    fun performSetLaser(b: Boolean) {
        viewModelScope.launch {
            Repository.setLaserState(b)
        }
    }

    fun performSetFlash(b: Boolean) {
        viewModelScope.launch {
            Repository.led(b)
        }
    }
}