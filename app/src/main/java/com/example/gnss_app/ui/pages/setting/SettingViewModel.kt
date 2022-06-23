package com.example.gnss_app.ui.pages.setting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gnss_app.ble.model.DeviceConfig.*
import com.example.gnss_app.ble.model.DeviceInfo.*
import com.example.gnss_app.ble.repository.Repository
import com.viva.libs.utils.Log
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {
    val TAG = "SettingViewModel"

    val ssid: MutableLiveData<String> by lazy { MutableLiveData("") }
    val password: MutableLiveData<String> by lazy { MutableLiveData("") }
    val wifiScanResult = mutableListOf<String>()


    val appInfo= AppInfo()
    fun performGetAppInfo(){
        viewModelScope.launch {
            val res = Repository.readAppInfo(appInfo)
            Log.i(TAG, "getAppInfo res ${res.info}".trimMargin())
        }
    }
    val netMode= mutableStateOf(0)
    val _netMode = NetMode()
    fun performReadNetModeConfig(){
        viewModelScope.launch {
            val res = Repository.readNetMode(_netMode)
            Log.i(TAG, "read netmode res ${res.value}")
            netMode.value=res.value
        }
    }
    fun performWriteNetModeConfig(){
        viewModelScope.launch {
            val res = Repository.writeNetMode(_netMode)
            Log.i(TAG, "write netmode res ${res.result}")
        }
    }


    fun performReadServerConfig(){
        viewModelScope.launch {
            val res = Repository.readServerConfig(Server())
            Log.i(TAG, "read server res $res")
        }
    }
}