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

    private val _server = HostAddress()
    val server = mutableStateOf(_server)
    fun performReadServerConfig() {
        viewModelScope.launch {
            val res = Repository.readServerConfig(_server)
            Log.i(TAG, "read server res ${res.id} ${res.ip}")
        }
    }

    fun performWriteServerConfig() {
        viewModelScope.launch {
            val res = Repository.writeServerConfig(_server)
            Log.i(TAG, "write server res ${res}")
        }
    }

    private val _socketState = SocketSwitch()
    val socketState=mutableStateOf(false)
    fun performOpenCloseSocket(){
        _socketState.value = if (gnssState.value) 0 else 1
        viewModelScope.launch {
            val res = Repository.writeServerState(_socketState)
            Log.i(TAG, "write server res ${res}")
        }
    }

    private val _gnssState = GnssState()
    val gnssState = mutableStateOf(false)
    fun performOpenCloseGnss() {
        viewModelScope.launch {
            _gnssState.value = if (gnssState.value) 0 else 1
            val res = Repository.writeGnssState(_gnssState)
            if (res.result > 0) gnssState.value = !gnssState.value
            Log.i(TAG, "write gnss state res ${res.value}")
        }
    }

    private val _ntrip = NtripServer()
    val ntrip = mutableStateOf(_ntrip)
    fun performWriteNtripConfig() {
        viewModelScope.launch {
            val res = Repository.writeNtripConfig(_ntrip)
            Log.i(TAG, "read server res ${res}")
        }
    }
    fun performReadNtripConfig() {
        viewModelScope.launch {
            val res = Repository.readNtripConfig(_ntrip)
            Log.i(TAG, "read server res ${res}")
        }
    }
    private val _ntripState = BaseState()
    val ntripState = mutableStateOf(false)
    fun performOpenCloseNtrip() {
        viewModelScope.launch {
            _ntripState.value = if (ntripState.value) 0 else 1
            val res = Repository.writeNtripState(_ntripState)
            if (res.result > 0) ntripState.value = !ntripState.value
            Log.i(TAG, "write gnss state res ${res.value}")
        }
    }


}