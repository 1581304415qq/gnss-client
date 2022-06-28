package com.example.gnss_app.ui.pages.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.example.gnss_app.ui.theme.GNSS_APPTheme
import com.example.gnss_app.utils.toastShow

@Composable
fun BluetoothPage(navController: NavController, viewModel: BluetoothViewModel) {
    val context = LocalContext.current
    val btIsEnable = remember {
        mutableStateOf(viewModel.btAdapter.isEnabled)
    }
    GNSS_APPTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
//                .size(100.dp, 100.dp)
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.status.value != BLE_CONNECT_STATUS.CONNECTED)
                Connect(viewModel.devices) {
                    viewModel.connectDevice(it) { ret ->
                        if (ret) navController.navigate("SettingPage")
                    }
                    toastShow(context, "bluetooth page", "select $it")
                }
            else {
                Box(Modifier.height(100.dp)) {
                    Button(onClick = {
                        viewModel.disconnectDevice()
                        viewModel.setStatus(BLE_CONNECT_STATUS.DISCONNECT)
                    }) {
                        Text("断开连接")
                    }
                }
            }
            if (btIsEnable.value) {
                Scan {
                    viewModel.bleScan(it)
                }
            } else {
                OpenBlueTooth {
                    btIsEnable.value = it
                }
            }
        }
    }
}

@Composable
fun OpenBlueTooth(callBack: (b: Boolean) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            when (it.resultCode) {
                ComponentActivity.RESULT_OK -> {
                    toastShow(context, msg = "bluetooth is ready")
                    callBack(true)
                }
                else -> {
                    toastShow(context, msg = "cant not turn on bluetooth")
                    callBack(false)
                }
            }
        })
    Button(onClick = { launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }) {
        Text(text = "打开蓝牙")
    }
}

@Composable
fun Scan(function: (cb: () -> Unit) -> Unit) {
    var stopCircular by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.1f) }
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    ).value
    if (!stopCircular)
        CircularProgressIndicator()
    Button(onClick = {
        function {
            stopCircular = true
        }
        stopCircular = false
    }) {
        Text(text = "扫描")
    }
}

@Composable
fun Connect(list: LiveData<List<String>>, function: (i: Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = {
            expanded = list.value?.isNotEmpty() ?: false
        }) {
            Text(text = "连接")
        }
        DropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }) {
            list.value?.forEachIndexed { index, s ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        function(index)
                        expanded = false
                    }) {
                    if (s.indexOf("Castor_BT") > -1) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "")
                    }
                    Text(text = s, modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun BluetoothPreview() {
    GNSS_APPTheme {
        Column(Modifier.padding(10.dp)) {
            Connect(MutableLiveData(listOf("1", "2", "3", "4"))) {}
            Scan {}
        }
    }
}