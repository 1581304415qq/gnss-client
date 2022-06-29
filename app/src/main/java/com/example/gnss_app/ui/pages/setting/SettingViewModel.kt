package com.example.gnss_app.ui.pages.setting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gnss_app.ble.model.ControlDevice.*
import com.example.gnss_app.ble.model.DeviceConfig.*
import com.example.gnss_app.ble.model.DeviceInfo.*
import com.example.gnss_app.ble.repository.EventType
import com.example.gnss_app.ble.repository.Repository
import com.viva.libs.utils.Log
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {
    val TAG = "SettingViewModel"

    init {
        Repository.on(EventType.ON_NTRIP_STATE) {
            // TODO
            _ntripState.body = it.data
        }
    }

    private val _appInfo = AppInfo()
    val appInfo = mutableStateOf("")
    fun performGetAppInfo() {
        viewModelScope.launch {
            val res = Repository.readAppInfo(_appInfo)
            appInfo.value = res.info
            Log.i(TAG, "getAppInfo res  ${res.info}")
        }
    }

    private val _sysPwd = BaseStringData()
    val sysPwd = mutableStateOf("")
    fun performReadSysPwd() {
        viewModelScope.launch {
            val res = Repository.readSysPasswd(_sysPwd)
            sysPwd.value = res.value
            Log.i(TAG, "get sys password res ${res.value}")
        }
    }

    fun performWriteSysPwd(value: String) {
        viewModelScope.launch {
            _sysPwd.value = value
            val res = Repository.writeSysPasswd(_sysPwd)
            Log.i(TAG, "get sys password  res ${res}")
        }
    }

    private val _phoneNum = BaseStringData()
    val phoneNum = mutableStateOf("")
    fun performReadTel() {
        viewModelScope.launch {
            val res = Repository.readTel(_phoneNum)
            phoneNum.value = res.value
            Log.i(TAG, "get phoneNum res ${res.value}")
        }
    }

    fun performWriteTel(value: String) {
        viewModelScope.launch {
            _phoneNum.value = value
            val res = Repository.writeTel(_phoneNum)
            Log.i(TAG, "get phoneNum res ${res}")
        }
    }


    val netMode = mutableStateOf("")
    private val _netMode = NetMode()
    fun performReadNetModeConfig() {
        viewModelScope.launch {
            val res = Repository.readNetMode(_netMode)
            netMode.value = res.value.toString()
            Log.i(TAG, "read netmode res ${res.value}")
        }
    }

    fun performWriteNetModeConfig(value: String) {
        viewModelScope.launch {
            _netMode.value = value.toInt()
            val res = Repository.writeNetMode(_netMode)
            Log.i(TAG, "write netmode res ${res.result}")
        }
    }

    val ipMode = mutableStateOf("")
    private val _ipMode = Mode()
    fun performReadIPModeConfig() {
        viewModelScope.launch {
            val res = Repository.readIPMode(_ipMode)
            ipMode.value = res.value.toString()
            Log.i(TAG, "read ipmode res ${res.value}")
        }
    }

    fun performWriteIPModeConfig(value: String) {
        viewModelScope.launch {
            _ipMode.value = value.toInt()
            val res = Repository.writeIPMode(_ipMode)
            Log.i(TAG, "write ipmode res ${res}")
        }
    }

    private val _server = HostAddress()
    val server = mutableStateOf(_server)
    val socketIP = mutableStateOf(0u)
    val socketPort = mutableStateOf<UShort>(0u)
    fun performReadServerConfig() {
        viewModelScope.launch {
            val res = Repository.readServerConfig(_server)
            socketIP.value = res.ip
            socketPort.value = res.port
            Log.i(TAG, "read server res id:${res.id} ip:${res.ip} port:${res.port}")
        }
    }

    fun performWriteServerConfig() {
        viewModelScope.launch {
            val res = Repository.writeServerConfig(_server)
            Log.i(TAG, "write server res ${res}")
        }
    }

    private val _socketState = SocketSwitch()
    val socketState = mutableStateOf(false)
    fun performOpenCloseSocket() {
        _socketState.value = if (!socketState.value) 0 else 1
        viewModelScope.launch {
            val res = Repository.writeServerState(_socketState)
            socketState.value = !socketState.value
            Log.i(TAG, "write server res ${res}")
        }
    }

    private val _gnssState = GnssState()
    val gnssState = mutableStateOf(false)
    fun performOpenCloseGnss() {
        viewModelScope.launch {
            _gnssState.value = if (!gnssState.value) 0 else 1
            val res = Repository.writeGnssState(_gnssState)
//            if (res.result > 0)
            gnssState.value = !gnssState.value
            Log.i(TAG, "write gnss state res ${res.value}")
        }
    }

    private val _ntrip = NtripServer()
    val ntrip = mutableStateOf(_ntrip)
    val ntripIP = mutableStateOf(0u)
    val ntripPort = mutableStateOf<UShort>(0u)

    val ntripAccount = mutableStateOf("")
    val ntripPasswd = mutableStateOf("")
    val ntripMount = mutableStateOf("")
    fun performWriteNtripConfig() {
        viewModelScope.launch {
            val res = Repository.writeNtripConfig(_ntrip)
            Log.i(TAG, "read ntrip res ${res}")
        }
    }

    fun performReadNtripConfig() {
        viewModelScope.launch {
            val res = Repository.readNtripConfig(_ntrip)
            ntripIP.value = res.server.ip
            ntripPort.value = res.server.port
            ntripAccount.value = res.account.value
            ntripPasswd.value = res.password.value
            ntripMount.value = res.mount.value
            Log.i(
                TAG,
                "read ntrip res id:${res.server.id} ip:${res.server.ip} port:${res.server.port}"
            )
        }
    }

    private val _ntripState = State()
    val ntripState = mutableStateOf(false)
    fun performOpenCloseNtrip() {
        viewModelScope.launch {
            _ntripState.value = if (ntripState.value) 0 else 1
            val res = Repository.writeNtripState(_ntripState)
//            if (res.result > 0)
            ntripState.value = !ntripState.value
            Log.i(TAG, "write ntrip state res ${res.value}")
        }
    }


    fun performSaveConfig() {
        viewModelScope.launch {
            val res = Repository.saveConfig()
        }
    }

    private val _debugState = State()
    fun performOpenDebug() {
        viewModelScope.launch {
            val res = Repository.openDebug(_debugState)
            _debugState.value = if (_debugState.value > 0) 0 else 11
        }
    }

    private val _adcValue = ADCValue()
    val adcID = mutableStateOf("")
    val adcValue = mutableStateOf("")

    fun performGetADCValue(id:String) {
        viewModelScope.launch {
            _adcValue.id = id.toInt().toByte()
            val res = Repository.readADCValue(_adcValue)
            adcValue.value = res.value.toString()
        }
    }
}