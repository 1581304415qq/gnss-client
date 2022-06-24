import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gnss_app.ui.pages.setting.SettingViewModel
import com.example.gnss_app.ui.theme.GNSS_APPTheme
import com.example.gnss_app.ui.theme.MyShapes
import com.example.gnss_app.ui.theme.MyTypography
import com.example.gnss_app.ui.theme.TextInputBackGroundColor

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
            NetModeConfig(viewModel)
            GpsConfig(viewModel)

            NtripConfig(viewModel)
            SocketConfig(viewModel)
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
            .height(50.dp)
            .padding(top = 5.dp, bottom = 5.dp)
    ) {
        Row {
            Text(
                style = MyTypography.body2,
                text="IP:")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
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
            Text(".")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
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
            Text(".")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
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
            Text(".")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
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
            Text(":")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
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
    viewModel: SettingViewModel = SettingViewModel()
) {
    val switchState by remember {
        mutableStateOf(false)
    }
    Column() {
        Text(text = "gnss模块")
        Row(
            Modifier
                .height(60.dp)
                .padding(top = 10.dp, bottom = 10.dp)
        ) {
            var ms by remember { mutableStateOf("") }
            BasicTextField(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                value = ms,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ms = newText
                },
                singleLine = true
            )

            Button(modifier = Modifier
                .absolutePadding(right = 5.dp),
                onClick = { }) {
                Text("读取")
            }
            Button(modifier = Modifier
                .absolutePadding(right = 5.dp),
                onClick = { }) {
                Text("配置")
            }
            Button(modifier = Modifier
                .absolutePadding(right = 5.dp),
                onClick = { viewModel.performOpenCloseGnss() }) {
                Text(text = if (switchState) "打开" else "关闭")
            }
        }
    }
}

@Composable
fun SocketConfig(
    viewModel: SettingViewModel = SettingViewModel()
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {

        Text(text = "socket")
        IPTextField {
            if (it != null) {
                viewModel.server.value.ip = it.first
                viewModel.server.value.port = it.second
            }
        }
        Row() {
            Button(
                modifier = Modifier
                    .absolutePadding(right = 5.dp),
                onClick = {
                    viewModel.performReadServerConfig()
                }
            ) {
                Text(
                    text = "读取",
                    fontSize = 15.sp
                )
            }
            Button(
                modifier = Modifier
                    .absolutePadding(right = 5.dp),
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

@Composable
fun NtripConfig(
    viewModel: SettingViewModel = SettingViewModel()
) {
    var account by remember { mutableStateOf("") }
    var passwd by remember { mutableStateOf("") }
    var mount by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Text(text = "ntrip")
        // ip设置
        IPTextField {
            if (it != null) {
                viewModel.ntrip.value.server.ip = it.first
                viewModel.ntrip.value.server.port = it.second
            }
        }
        // 账号 密码 挂载点
        Row (Modifier.padding(bottom = 5.dp)){
            BasicTextField(
                modifier = Modifier
                    .width(100.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                singleLine = true,
                value = account,
                onValueChange = { newText ->
                    account = newText
                    viewModel.ntrip.value.account.value = newText
                }
            )
            BasicTextField(
                modifier = Modifier
                    .width(100.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                singleLine = true,
                value = passwd,
                onValueChange = { newText ->
                    passwd = newText
                    viewModel.ntrip.value.password.value = newText
                }
            )
            BasicTextField(
                modifier = Modifier
                    .width(100.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                singleLine = true,
                value = mount,
                onValueChange = { newText ->
                    mount = newText
                    viewModel.ntrip.value.mount.value = newText
                }
            )
        }
        // 按钮
        Row {
            Button(
                modifier = Modifier
                    .absolutePadding(right = 5.dp),
                onClick = {
                    viewModel.performReadNtripConfig()
                }
            ) {
                Text(
                    style = MyTypography.button,
                    text = "读取",
                    fontSize = 15.sp
                )
            }
            Button(
                modifier = Modifier
                    .absolutePadding(right = 5.dp),
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

@Composable
fun NetModeConfig(
    viewModel: SettingViewModel = SettingViewModel(),
    modifier: Modifier = Modifier
) {
    var text by remember {
        mutableStateOf("")
    }
    Column() {
        Text("网络模式配置")
    Row(
        Modifier
            .height(60.dp)
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        BasicTextField(
            modifier = Modifier
                .fillMaxHeight()
                .width(50.dp)
                .absolutePadding(right = 5.dp)
                .background(TextInputBackGroundColor, MyShapes.small),
            value = text,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            onValueChange = { newText ->
                if (newText.length <= 2) {
                    text = newText.filter { it.isDigit() }
                }
            },
            singleLine = true,
            textStyle = MyTypography.body2
        )
        Button(
            modifier = Modifier
                .absolutePadding(right = 5.dp),
            onClick = {
                viewModel.performReadNetModeConfig()
            }) {
            Text(text = "读取")
        }
        Button(
            modifier = Modifier
                .absolutePadding(right = 5.dp),
            onClick = {
                if (text != "")
                    viewModel.performWriteNetModeConfig(text)
            }) {
            Text(text = "配置")
        }
    }}
}




@Preview
@Composable
fun SettingPreview() {
    GNSS_APPTheme {
        Column(Modifier.padding(10.dp)) {
            NetModeConfig()
            GpsConfig()
            NtripConfig()
            SocketConfig()
        }
    }
}