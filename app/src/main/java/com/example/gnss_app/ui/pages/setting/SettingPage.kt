import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.example.gnss_app.ble.model.DeviceConfig
import com.example.gnss_app.ui.pages.setting.SettingViewModel
import com.example.gnss_app.ui.theme.GNSS_APPTheme

@Composable
fun SettingPage(navController: NavController, viewModel: SettingViewModel) {
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(10.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text("Setting")
            Button(onClick = {
                viewModel.performGetAppInfo()
            }) {
                Text("获取固件信息")
            }
            NetModeConfig(viewModel.netMode, viewModel)
            GpsConfig(viewModel.gnssState, viewModel)

            NtripConfig(viewModel)
            SocketConfig(viewModel.socketState, viewModel)
        }

    }
}

fun convertIPORT(a: String, b: String, c: String, d: String, p: String): Pair<UInt, UShort>? {
    if ((a == "") or (b == "") or (c == "") or (d == "") or (p == "")) {
        return null
    }
    val ip = a.toUInt().shl(8 * 3) or b.toUInt().shl(8 * 2) or c.toUInt().shl(8) or d.toUInt()
    val port = p.toUShort()
    return Pair(ip, port)
}

@Composable
fun IPTextField(onValueChange: (Pair<UInt, UShort>?) -> Unit) {
    var ip_1 by remember { mutableStateOf("") }
    var ip_2 by remember { mutableStateOf("") }
    var ip_3 by remember { mutableStateOf("") }
    var ip_4 by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    Box(
        Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        Row {
            BasicTextField(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                value = ip_1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip_1 = newText
                    onValueChange(convertIPORT(ip_1, ip_2, ip_3, ip_4, port))
                },
                singleLine = true
            )
            BasicTextField(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                value = ip_2,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip_2 = newText
                    onValueChange(convertIPORT(ip_1, ip_2, ip_3, ip_4, port))
                },
                singleLine = true
            )

            BasicTextField(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                value = ip_3,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip_3 = newText
                    onValueChange(convertIPORT(ip_1, ip_2, ip_3, ip_4, port))
                },
                singleLine = true
            )

            BasicTextField(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                value = ip_4,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip_4 = newText
                    onValueChange(convertIPORT(ip_1, ip_2, ip_3, ip_4, port))
                },
                singleLine = true
            )
            BasicTextField(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                value = port,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    port = newText
                    onValueChange(convertIPORT(ip_1, ip_2, ip_3, ip_4, port))
                },
                singleLine = true
            )

        }
    }
}

@Composable
fun GpsConfig(
    state: MutableState<Boolean>,
    viewModel: SettingViewModel
) {
    val switchState by remember {
        state
    }
    Row {
        var ms by remember { mutableStateOf("") }
        BasicTextField(
            modifier = Modifier
                .height(50.dp)
                .width(50.dp)
                .absolutePadding(right = 5.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp)),
            value = ms,
            onValueChange = { newText ->
                ms = newText
            },
            singleLine = true
        )

        Button(modifier = Modifier
            .absolutePadding(right = 5.dp),
            onClick = { }) {
            Text("配置")
        }
        Button(modifier = Modifier
            .absolutePadding(right = 5.dp),
            onClick = { viewModel.performOpenCloseGnss() }) {
            Text(text = if (switchState) "打开gnss" else "关闭gnss")
        }
    }
}

@Composable
fun SocketConfig(
    state: MutableState<Boolean>,
    viewModel: SettingViewModel
) {
    Text(text = "socket")
    Box(
        Modifier
            .height(100.dp)
            .fillMaxWidth()
    ) {
        Column {
            IPTextField {
                if (it != null) {
                    viewModel.server.value.ip = it.first
                    viewModel.server.value.port = it.second
                }
            }
            Row() {
                Button(
                    onClick = {
                        viewModel.performReadServerConfig()
                    }
                ) {
                    Text(
                        text = "读取配置",
                        fontSize = 15.sp
                    )
                }
                Button(
                    onClick = {
                        viewModel.performWriteServerConfig()
                    }
                ) {
                    Text(
                        text = "配置",
                        fontSize = 15.sp
                    )
                }
                Button(
                    onClick = {
                        viewModel.performOpenCloseSocket()
                    }
                ) {
                    Text(
                        text = "开关",
                        fontSize = 15.sp
                    )
                }
            }

        }
    }
}

@Composable
fun NtripConfig(
    viewModel: SettingViewModel
) {
    var account by remember { mutableStateOf("") }
    var passwd by remember { mutableStateOf("") }
    var mount by remember { mutableStateOf("") }

    Box(
        Modifier
            .height(150.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(text = "ntrip")
            // ip设置
            IPTextField{
                if (it != null) {
                    viewModel.ntrip.value.server.ip = it.first
                    viewModel.ntrip.value.server.port = it.second
                }
            }
            // 账号 密码 挂载点
            Row {
                BasicTextField(
                    modifier = Modifier
                        .height(50.dp)
                        .width(100.dp)
                        .absolutePadding(right = 5.dp)
                        .background(Color.Gray, RoundedCornerShape(8.dp)),
                    singleLine=true,
                    value = account,
                    onValueChange = { newText ->
                        account = newText
                        viewModel.ntrip.value.account.value=newText
                    }
                )
                BasicTextField(
                    modifier = Modifier
                        .height(50.dp)
                        .width(100.dp)
                        .absolutePadding(right = 5.dp)
                        .background(Color.Gray, RoundedCornerShape(8.dp)),
                    singleLine=true,
                    value = passwd,
                    onValueChange = { newText ->
                        passwd = newText
                        viewModel.ntrip.value.password.value=newText
                    }
                )
                BasicTextField(
                    modifier = Modifier
                        .height(50.dp)
                        .width(100.dp)
                        .absolutePadding(right = 5.dp)
                        .background(Color.Gray, RoundedCornerShape(8.dp)),
                    singleLine=true,
                    value = mount,
                    onValueChange = { newText ->
                        mount = newText
                        viewModel.ntrip.value.mount.value=newText
                    }
                )
            }
            // 按钮
            Row {
                Button(
                    onClick = {
                        viewModel.performReadNtripConfig()
                    }
                ) {
                    Text(
                        text = "读取配置",
                        fontSize = 15.sp
                    )
                }
                Button(
                    onClick = {
                        viewModel.performWriteNtripConfig()
                    }
                ) {
                    Text(
                        text = "配置",
                        fontSize = 15.sp
                    )
                }
                Button(
                    onClick = {
                        viewModel.performOpenCloseNtrip()
                    }
                ) {
                    Text(
                        text = "开关",
                        fontSize = 15.sp
                    )
                }
            }

        }
    }
}

@Composable
fun NetModeConfig(
    netMode: MutableState<String>,
    viewModel: SettingViewModel
) {
    var text by remember {
        netMode
    }
    Row(Modifier.height(60.dp)) {
        BasicTextField(
            modifier = Modifier
                .fillMaxHeight()
                .width(50.dp)
                .absolutePadding(right = 5.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp)),
            value = text,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            onValueChange = { newText ->
                if (newText.length <= 2) {
                    text = newText.filter { it.isDigit() }
                }
            },
            singleLine = true
        )
        Button(onClick = {
            viewModel.performReadNetModeConfig()
        }) {
            Text(text = "读取网络模式")
        }
        Button(onClick = {
            if (text != "")
                viewModel.performWriteNetModeConfig(text)
        }) {
            Text(text = "配置网络模式")
        }
    }
}

@Preview
@Composable
fun SettingPreview() {
    GNSS_APPTheme {
        Column(Modifier.padding(10.dp)) {
//            NtripConfig(name = "ntrip")
//            SocketConfig(name = "socket")
        }
    }
}