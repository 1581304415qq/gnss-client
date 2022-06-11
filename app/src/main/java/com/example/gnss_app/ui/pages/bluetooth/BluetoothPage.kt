package com.example.gnss_app.ui.pages.bluetooth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gnss_app.ui.theme.GNSS_APPTheme

@Composable
fun Scan(viewModel: BluetoothViewModel) {
    Button(onClick = {
        viewModel.bleScan()
    }) {
        Text(text = "扫描")
    }
}

@Composable
fun Connect(viewModel: BluetoothViewModel) {
    var select by remember { mutableStateOf("") }
    val devices = viewModel.devices.value
    DropdownMenu(expanded = true, onDismissRequest = {}) {
        devices?.forEach {
            DropdownMenuItem(onClick = { select = it }) {
                Text(text = it)
            }
        }
    }
    Button(onClick = {
        viewModel.connectDevice(1)
    }) {
        Text(text = "连接")
    }
}

@Composable
fun BluetoothPreview() {
    GNSS_APPTheme {
        Column(Modifier.padding(10.dp)) {
        }
    }
}