import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gnss_app.ui.pages.setting.SettingViewModel
import com.example.gnss_app.ui.theme.GNSS_APPTheme
import com.example.gnss_app.utils.toastShow

@Composable
fun SettingPage() {

    Column {
        Button(onClick = { /*TODO*/ }) {
            Text("打开蓝牙")
        }
    }
}
@Composable
fun OpenBlueTooth() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            when (it.resultCode) {
                ComponentActivity.RESULT_OK -> {
                    toastShow(context, msg = "bluetooth is ready")
                }
                else ->
                    toastShow(context, msg = "cant not turn on bluetooth")
            }
        })
    Button(onClick = { launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }) {
        Text(text = "打开蓝牙")
    }
}



@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
    Button(onClick = { /*TODO*/ }) {
        Text(text = "switch")
    }
    var text by remember { mutableStateOf("") }
    BasicTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
        }
    )
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
fun GpsGreeting(name: String) {
    Text(text = name)
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
            onClick = { /*TODO*/ }) {
            Text("switch")
        }
    }
}

@Composable
fun SocketGreeting(name: String) {
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
fun NtripGreeting(name: String) {
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
fun SettingPreview() {
    GNSS_APPTheme {
        Column(Modifier.padding(10.dp)) {
        }
    }
}