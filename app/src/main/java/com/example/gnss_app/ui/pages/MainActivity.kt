package com.example.gnss_app.ui.pages

import SettingPage
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gnss_app.ui.pages.bluetooth.BluetoothPage
import com.example.gnss_app.ui.pages.bluetooth.BluetoothViewModel
import com.example.gnss_app.ui.pages.setting.SettingViewModel
import com.example.gnss_app.ui.theme.GNSS_APPTheme
import com.example.gnss_app.utils.toastShow

class MainActivity() : ComponentActivity() {
    private val settingViewModel by lazy { SettingViewModel() }
    private val bluetoothViewModel by lazy { BluetoothViewModel(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            GNSS_APPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NavHost(navController = navController, startDestination = "MainPage") {
                        composable("MainPage") {
                            MainPage(navController = navController)
                        }
                        composable("BluetoothPage") {
                            BluetoothPage(navController = navController,bluetoothViewModel)
                        }
                        composable("SettingPage") {
                            SettingPage(navController = navController, settingViewModel)
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun MainPage(navController: NavController) {
    Text("main")
    RequestPermission {
        if (it.isEmpty())
            navController.navigate("BluetoothPage")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GNSS_APPTheme {
        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Text("GNSS")
            RequestPermission {}
        }
    }
}

/*  权限申请    */
@Composable
private fun RequestPermission(callBack: (list: List<String>) -> Unit) {
    val TAG = "MainActivity"
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val permissionList = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    //存储用户拒绝授权的权限
    val permissionTemp: ArrayList<String> = ArrayList()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            permissionTemp.clear()
            for (key in it.keys) {
                try {
                    if (!it[key]!!)
                        permissionTemp.add(key)
                    toastShow(
                        context, TAG,
                        "requestPermissions $key ${it[key]}",
                    )
                } catch (e: Exception) {
                    toastShow(
                        context, TAG,
                        "requestPermissions ERROR $key",
                    )
                }
            }
            callBack(permissionTemp.toList())
        })

    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                launcher.launch(permissionList)
            }
        }
    }

    DisposableEffect(lifecycle, lifecycleObserver) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}