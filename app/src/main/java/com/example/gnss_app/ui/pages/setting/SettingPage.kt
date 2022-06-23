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
        Column {
            Text("Setting")
            Button(onClick = {
                viewModel.performGetAppInfo()
            }) {
                Text("获取固件信息")
            }
            NetModeConfig(viewModel.netMode, viewModel)
            GpsConfig(viewModel.gnss_state, viewModel)
        }

    }
}

@Composable
fun IPTextField() {
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
                onValueChange = { newText ->
                    ip_1 = newText
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
                onValueChange = { newText ->
                    ip_2 = newText
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
                onValueChange = { newText ->
                    ip_3 = newText
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
                onValueChange = { newText ->
                    ip_4 = newText
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
                onValueChange = { newText ->
                    port = newText
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
fun SocketConfig(name: String) {
    Text(text = name)
    Box(
        Modifier
            .height(100.dp)
            .fillMaxWidth()
    ) {
        Column {
            IPTextField()
            Button(
                onClick = { /*TODO*/ }
            ) {
                Text(
                    text = "switch",
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun NtripConfig(name: String) {
    Text(text = name)
    Box(
        Modifier
            .height(150.dp)
            .fillMaxWidth()
    ) {
        Column {
            IPTextField()
            var accont by remember { mutableStateOf("") }
            Box {
                Row {
                    BasicTextField(
                        modifier = Modifier
                            .height(50.dp)
                            .width(100.dp)
                            .absolutePadding(right = 5.dp),
                        value = accont,
                        onValueChange = { newText ->
                            accont = newText
                        }
                    )
                    var passwd by remember { mutableStateOf("") }
                    BasicTextField(
                        modifier = Modifier
                            .height(50.dp)
                            .width(100.dp)
                            .absolutePadding(right = 5.dp),
                        value = passwd,
                        onValueChange = { newText ->
                            passwd = newText
                        }
                    )
                    var mount by remember { mutableStateOf("") }
                    BasicTextField(
                        modifier = Modifier
                            .height(50.dp)
                            .width(100.dp)
                            .absolutePadding(right = 5.dp),
                        value = mount,
                        onValueChange = { newText ->
                            mount = newText
                        }
                    )
                }
            }
            Button(
                onClick = { /*TODO*/ }
            ) {
                Text(
                    text = "switch",
                    fontSize = 15.sp
                )
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
            NtripConfig(name = "ntrip")
            SocketConfig(name = "socket")
        }
    }
}