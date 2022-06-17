package com.example.gnss_app.ui.pages.setting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gnss_app.ble.model.AppInfo
import com.example.gnss_app.ble.model.Server
import com.example.gnss_app.ble.repository.Repository
import com.example.gnss_app.ui.pages.bluetooth.BLE_CONNECT_STATUS
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
            Log.i(TAG, "getAppInfo res $res")
        }
    }

    fun performReadServerConfig(){
        viewModelScope.launch {
            val res = Repository.readServerConfig(Server.Read(0))
            Log.i(TAG, "read server res $res")
        }
    }
}