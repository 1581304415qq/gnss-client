package com.example.gnss_app.ui.pages.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.example.gnss_app.ui.theme.GNSS_APPTheme
import com.example.gnss_app.utils.toastShow

@Composable
fun BluetoothPage(navController: NavController, viewModel: BluetoothViewModel) {
    GNSS_APPTheme {
        val btIsEnable = remember {
            mutableStateOf(viewModel.btAdapter.isEnabled)
        }
        Column(Modifier.padding(10.dp)) {
            Connect(viewModel.devices) { viewModel.connectDevice(it) }
            if (btIsEnable.value) {
                Scan {
                    viewModel.bleScan()
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
fun Scan(function: () -> Unit) {
    Button(onClick = function) {
        Text(text = "扫描")
    }
}

@Composable
fun Connect(list: LiveData<List<String>>, function: (i: Int) -> Unit) {
    var select by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(text = "连接")
        }
        DropdownMenu(
            modifier = Modifier
                .width(100.dp)
                .height(20.dp),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }) {
            list.value?.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    function(index)
                    select = index
                }) {
                    Text(text = s)
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
//            Connect(listOf("1", "2", "3", "4")) {}
            Scan {}
        }
    }
}