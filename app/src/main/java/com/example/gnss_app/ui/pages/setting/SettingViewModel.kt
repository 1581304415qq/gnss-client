package com.example.gnss_app.ui.pages.setting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gnss_app.ble.model.ControlDevice.*
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

    val appInfo = mutableStateOf("")
    private val _appInfo = AppInfo()
    fun performGetAppInfo() {
        viewModelScope.launch {
            val res = Repository.readAppInfo(_appInfo)
            Log.i(TAG, "getAppInfo res ${res.info}".trimMargin())
        }
    }

    val netMode = mutableStateOf("")
    private val _netMode = NetMode()
    fun performReadNetModeConfig() {
        viewModelScope.launch {
            val res = Repository.readNetMode(_netMode)
            Log.i(TAG, "read netmode res ${res.value}")
            netMode.value = res.value.toString()
        }
    }

    fun performWriteNetModeConfig(value: String) {
        _netMode.value = value.toInt()
        viewModelScope.launch {
            val res = Repository.writeNetMode(_netMode)
            Log.i(TAG, "write netmode res ${res.result}")
        }
    }

    private val _server = Server()
    val server = mutableStateOf(_server)
    fun performReadServerConfig() {
        viewModelScope.launch {
            val res = Repository.readServerConfig(_server)
            Log.i(TAG, "read server res ${res.id} ${res.ip}")
        }
    }

    private val _gnssState = GnssState()
    val gnss_state = mutableStateOf(false)
    fun performOpenCloseGnss() {
        viewModelScope.launch {
            _gnssState.value = if (gnss_state.value) 0 else 1
            val res = Repository.writeGnssState(_gnssState)
            if (res.result > 0) gnss_state.value = !gnss_state.value
            Log.i(TAG, "write gnss state res ${res.value}")
        }
    }


}