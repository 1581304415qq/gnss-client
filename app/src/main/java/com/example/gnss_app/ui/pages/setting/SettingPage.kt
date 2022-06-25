import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
        Column(
            Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Setting")
            AppInfo()
            SysPasswd()
            Tel()
            NetModeConfig(viewModel)
            GpsConfig(viewModel)

            NtripConfig(viewModel)
            SocketConfig(viewModel)
            Button(onClick = {
                viewModel.performSaveConfig()
            }) {
                Text(text = "保存配置")
            }
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
fun IPTextField(
    _ip: MutableState<UInt>,
    _port: MutableState<UShort>,
    onValueChange: (Pair<UInt, UShort>?) -> Unit
) {
    val ip by remember { _ip }
    val port by remember { _port }
    var ip1 by remember {
     mutableStateOf("")
    }
    var ip2 by remember {
        mutableStateOf("")
    }
    var ip3 by remember {
        mutableStateOf("")
    }
    var ip4 by remember {
        mutableStateOf("")
    }
    var tmpPort by remember {
        mutableStateOf("")
    }
    Column(
        Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 5.dp, bottom = 5.dp)
    ) {
        Text(text = "${ip.shr(24).toUByte()}.${ip.shr(16).toUByte()}.${ip.shr(8).toUByte()}.${ip.toUByte()}:${port}")
        Row {
            Text(
                style = MyTypography.body2,
                text = "IP:"
            )
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                value = ip1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip1 = newText
                    onValueChange(convertIPORT(ip1, ip2, ip3, ip4, tmpPort))
                },
                singleLine = true
            )
            Text(".")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                value = ip2,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip2 = newText
                    onValueChange(convertIPORT(ip1, ip2, ip3, ip4, tmpPort))
                },
                singleLine = true
            )
            Text(".")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                value = ip3,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip3 = newText
                    onValueChange(convertIPORT(ip1, ip2, ip3, ip4, tmpPort))
                },
                singleLine = true
            )
            Text(".")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                value = ip4,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    ip4 = newText
                    onValueChange(convertIPORT(ip1, ip2, ip3, ip4, tmpPort))
                },
                singleLine = true
            )
            Text(":")
            BasicTextField(
                modifier = Modifier

                    .width(50.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                value = tmpPort,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = { newText ->
                    tmpPort = newText
                    onValueChange(convertIPORT(ip1, ip2, ip3, ip4, tmpPort))
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
        viewModel.gnssState
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
        IPTextField(
            viewModel.socketIP,
            viewModel.socketPort
        ) {
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
    var account by remember { viewModel.ntripAccount}
    var passwd by remember { viewModel.ntripPasswd}
    var mount by remember { viewModel.ntripMount }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Text(text = "ntrip")
        // ip设置
        IPTextField(viewModel.ntripIP, viewModel.ntripPort) {
            if (it != null) {
                viewModel.ntrip.value.server.ip = it.first
                viewModel.ntrip.value.server.port = it.second
            }
        }
        // 账号 密码 挂载点
        Row(
            Modifier
                .padding(bottom = 5.dp)
                .height(30.dp)
        ) {
            BasicTextField(
                modifier = Modifier
                    .width(100.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
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
        viewModel.netMode
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
        }
    }
}

@Composable
fun AppInfo(
    viewModel: SettingViewModel = SettingViewModel()
) {
    val text by remember {
        viewModel.appInfo
    }
    val scroll = rememberScrollState(0)
    Column() {
        Text(text = "系统信息")
        Row(
            Modifier
                .height(60.dp)
        ) {
            Text(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .absolutePadding(right = 5.dp)
                    .verticalScroll(scroll)
                    .background(TextInputBackGroundColor, MyShapes.small),
                text = text,
            )
            Button(onClick = {
                viewModel.performGetAppInfo()
            }) {
                Text(text = "读取")
            }
        }
    }
}

@Composable
fun SysPasswd(
    viewModel: SettingViewModel = SettingViewModel()
) {
    val passwd by remember {
        viewModel.sysPwd
    }
    Column() {
        Text(text = "系统密码")

        Row() {
            BasicTextField(
                modifier = Modifier
                    .height(40.dp)
                    .width(120.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                singleLine = true,
                value = passwd,
                onValueChange = {
                    viewModel.sysPwd.value = it
                })
            Button(modifier = Modifier
                .padding(end = 5.dp),
                onClick = {
                    viewModel.performReadSysPwd()
                }) {
                Text(text = "读取")
            }
            Button(onClick = {
                viewModel.performWriteSysPwd(viewModel.sysPwd.value)
            }) {
                Text(text = "配置")
            }
        }
    }
}

@Composable
fun Tel(
    viewModel: SettingViewModel = SettingViewModel()
) {
    val phoneNumber by remember {
        viewModel.phoneNum
    }
    Column() {
        Text(text = "电话")

        Row() {
            BasicTextField(
                modifier = Modifier
                    .height(40.dp)
                    .width(120.dp)
                    .absolutePadding(right = 5.dp)
                    .background(TextInputBackGroundColor, MyShapes.small),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                value = phoneNumber,
                onValueChange = {
                    viewModel.phoneNum.value = it
                })
            Button(modifier = Modifier
                .padding(end = 5.dp),
                onClick = {
                    viewModel.performReadTel()
                }) {
                Text(text = "读取")
            }
            Button(onClick = {
                viewModel.performWriteTel(viewModel.phoneNum.value)
            }) {
                Text(text = "配置")
            }
        }
    }
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